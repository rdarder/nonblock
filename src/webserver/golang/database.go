package main

import (
  "fmt" // TODO, get rid of this and use variable binding
  "log"
  // goinstall -v -dashboard=true github.com/feyeleanor/gosqlite3
  "github.com/feyeleanor/gosqlite3"
)

/* initialization */
func init() {
  buildGeoQueries()
  buildGeoTree()
  go submitListener()
  go nodeSubscriber()
  go clientNotifier()
}

type GeoNode struct {
  Nombre string // Lugar
  Tipo   string // Alcance
  Contenedor *GeoNode
  // subscriptions by Nivel
  Subscriptions map[string]map[*Subscription]bool
}

/* registers or cancels suscriptions */
func nodeSubscriber() {
  for s := range subscriptionChannel {
    if s.Subscribe {
      log.Printf("Suscribing: %v\n", s.Request)
      /* lookup geonode by alcance, lugar */
      s.Node = Geos[s.Request.Alcance][
                geo2id[s.Request.Alcance][s.Request.Lugar]]
      /* suscribe client to geonode */
      if s.Node.Subscriptions[s.Request.Nivel] == nil {
        s.Node.Subscriptions[s.Request.Nivel] = map[*Subscription]bool{}
      }
      s.Node.Subscriptions[s.Request.Nivel][s] = true, true
      /* push newdata */
      voteupdateChannel <- s.Node
    } else {
      /* remove reference from geonode, remove from client's requests */
      s.Node.Subscriptions[s.Request.Nivel][s] = false, false
    }
  }
}

/* a channel to signal Updates */
var voteupdateChannel = make(chan *GeoNode, 128)

/* Client notificator */
func clientNotifier() {
  db, err := sqlite3.Open("db.sqlite")
  if err != nil {
    log.Fatalln("Failed to connect to DB.")
  }
  defer db.Close()

  // TODO: delay response awaiting for a new notification ?
  for node := range voteupdateChannel {
    for nivel, subsmap := range node.Subscriptions {
      for s, _ := range subsmap {
        // TODO: this code sucks, rethink data structures
        sql := fmt.Sprintf(*sqlNivel[nivel], node.Tipo,
                           geo2id[node.Tipo][node.Nombre],
                           s.Request.Puesto)

        var data []*newDataBody

        if st, err := db.Prepare(sql); err == nil {
          log.Println(sql)
          for st.Step() != nil {
            data = append(data, &newDataBody{
                            Mesa: st.Column(0).(string),
                            Local: st.Column(1).(string),
                            Seccional: st.Column(2).(string),
                            Localidad: st.Column(3).(string),
                            Departamento: st.Column(4).(string),
                            Provincia: st.Column(5).(string),
                            Candidato: st.Column(6).(string),
                            Partido: st.Column(7).(string),
                            Puesto: st.Column(8).(string),
                            Cantidad: st.Column(9).(int64) })
          }
          m := Message{Name: "newdata", Id: "481234", Ref: s.Ref}
          mess := m.encodeNewData(data)
          if _, err := s.Conn.Write(mess); err != nil {
            s.Conn.Close() // TODO: handle properly a write error
          } else {
            log.Println("Message sent.")
          }
          data = nil
        }
      }
    }
  }
}


/* queries by level */
var sqlNivel = map[string]*string{
  "provincia"    : new(string),
  "departamento" : new(string),
  "localidad"    : new(string),
  "seccional"    : new(string),
  "local"        : new(string),
  "mesa"         : new(string) }

func buildGeoQueries() {
  // TODO: create indices and auto build this

  geotypes := map[string]int{
    "provincia":5, "departamento":4, "localidad":3,
    "seccional":2, "local":1, "mesa":0}

  for geotype, rank := range geotypes {

    var g0, g1, g2, g3, g4, g5 string
    if rank > geotypes["mesa"]         { g0 = "''" } else { g0 = "mesa.nombre"}
    if rank > geotypes["local"]        { g1 = "''" } else { g1 = "local.nombre"}
    if rank > geotypes["seccional"]    { g2 = "''" } else { g2 = "seccional.nombre"}
    if rank > geotypes["localidad"]    { g3 = "''" } else { g3 = "localidad.nombre"}
    if rank > geotypes["departamento"] { g4 = "''" } else { g4 = "departamento.nombre"}
    if rank > geotypes["provincia"]    { g5 = "''" } else { g5 = "provincia.nombre"}

    *sqlNivel[geotype] = fmt.Sprintf(`
     select %v as Mesa,
            %v as Local,
            %v as Seccional,
            %v as Localidad,
            %v as Departamento,
            %v as Provincia,
            c.nombre as Candidato,
            p.nombre as Partido,
            c.puesto as Puesto,
            sum(v.votos) as Cantidad
       from geo mesa, geo local, geo seccional,
            geo localidad, geo departamento, geo provincia,
            candidatos c, partidos p, votos v
      where v.candidato_id = c.id and c.partido_id = p.id
        and v.mesa_id = mesa.id
        and mesa.contenedor_id = local.id
        and local.contenedor_id = seccional.id
        and seccional.contenedor_id = localidad.id
        and localidad.contenedor_id = departamento.id
        and departamento.contenedor_id = provincia.id
        and %%v.id = %%v and c.puesto = '%%v'
   group by Provincia, Departamento, Localidad, Seccional,
            Local, Mesa, Candidato, Partido, Puesto
    `, g0, g1, g2, g3, g4, g5)

    log.Println(*sqlNivel[geotype])
  }
}

/* map Geo ids to tree nodes */
var Geos = map[string]map[int64]*GeoNode{
  "provincia"    : make(map[int64]*GeoNode),
  "departamento" : make(map[int64]*GeoNode),
  "localidad"    : make(map[int64]*GeoNode),
  "seccional"    : make(map[int64]*GeoNode),
  "local"        : make(map[int64]*GeoNode),
  "mesa"         : make(map[int64]*GeoNode) }

/* map geo names to ids */
var geo2id = map[string]map[string]int64{
  "provincia"    : make(map[string]int64),
  "departamento" : make(map[string]int64),
  "localidad"    : make(map[string]int64),
  "seccional"    : make(map[string]int64),
  "local"        : make(map[string]int64),
  "mesa"         : make(map[string]int64) }


func buildGeoTree() {
  db, err := sqlite3.Open("db.sqlite")
  if err != nil {
    log.Fatalln("Failed to connect to DB.")
  }
  defer db.Close()

  // maps are a reference type
  var prevLevel string = ""

  for _, geotype := range []string{
    "provincia", "departamento", "localidad",
    "seccional", "local", "mesa"} {

    sql := fmt.Sprintf(`
      select g.id, g.nombre, g.contenedor_id
        from geo g
       where g.tipo = '%v'
    `, geotype)

    if st, err := db.Prepare(sql); err == nil {
      for st.Step() != nil {
        /* get the parent */
        var container *GeoNode = nil
        if prevLevel != "" {
          container = Geos[prevLevel][st.Column(2).(int64)]
        }
        /* create the node */
        Geos[geotype][st.Column(0).(int64)] = &GeoNode{
          Nombre: st.Column(1).(string),
          Tipo: geotype,
          Contenedor: container,
          Subscriptions: map[string]map[*Subscription]bool{} }
        /* keep a mapping from name to id */
        geo2id[geotype][st.Column(1).(string)] = st.Column(0).(int64)
      }
      st.Finalize()
    } else {
      log.Fatalf("GeoTree construction failed. %v", err)
    }
    /* keep a reference to upper level */
    prevLevel = geotype
  }
  log.Println("GeoTree built.")
}


/* update vote count on DB and notify clients */
func submitListener() {
  db, err := sqlite3.Open("db.sqlite")
  if err != nil {
    log.Fatalln("Failed to connect to DB.")
  }
  defer db.Close()

  log.Println("Now listening for new votes.")
  /* listen on chanel for updates */
  for s := range dbupdateChannel {
    for _, v := range s.Votos {
      // TODO: tune this, and use 'partido' ?
      sql := fmt.Sprintf(`
        insert into votos select %v, c.id, %v from candidatos c
                           where c.nombre = '%v' and c.puesto = '%v'
      `, geo2id["mesa"][s.Mesa], v.Cantidad, v.Candidato, v.Puesto)

      // TODO: this driver sucks and doesn't catch errors correctly
      // duplicate votes will fail silently but will send an update message
      if st, err := db.Prepare(sql); err == nil {
        st.Step()
        st.Finalize()
        log.Println("Submited a vote")
      } else {
        log.Fatalf("Submission to DB failed. %v", err)
      }

      /* signal an update on the node */
      n := Geos["mesa"][ geo2id["mesa"][s.Mesa] ]
      for n != nil {
        voteupdateChannel <- n
        n = n.Contenedor
      }
    }
  }
}
