package main

import (
  "fmt" // TODO, get rid of this and use variable binding
  "log"
  // goinstall -v -dashboard=true github.com/kuroneko/gosqlite3
  "github.com/kuroneko/gosqlite3"
)

/* initialization */
func init() {
  buildGeoQueries()
  buildGeoTree()
  go submitListener()
  /*go nodeSubscriber()*/
  /*go clientNotifier()*/
}

type GeoNode struct {
  Nombre string // Lugar
  Tipo   string // Alcance
  Contenedor *GeoNode
  Subscriptions map[*Subscription]bool
}


/* registers or cancels suscriptions */
func nodeSubscriber() {
  for s := range subscriptionChannel {
    if s.Subscribe {
      /* lookup geonode by alcance, lugar */
      s.Node = Geos[s.Request.Alcance][
                geo2id[s.Request.Alcance][s.Request.Lugar]]
      /* register client's request to it's list */
      ClientSubscriptions[s.Conn][s.Ref] = s
      /* suscribe client to geonode */
      s.Node.Subscriptions[s] = true, true
      /* push newdata */
      voteupdateChannel <- s.Node
    } else {
      /* remove reference from geonode, remove from client's requests */
      s := ClientSubscriptions[s.Conn][s.Ref]
      s.Node.Subscriptions[s] = false, false
      ClientSubscriptions[s.Conn][s.Ref] = nil, false
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
    for s, _ := range node.Subscriptions {
      // TODO: this code sucks, rethink data structures
      sql := fmt.Sprintf(*sqlNivel[s.Request.Nivel],
                         geo2id[node.Tipo][node.Nombre],
                         s.Request.Puesto)

      if st, err := db.Prepare(sql); err == nil {
        for st.Step() != nil {
          /*newDataBody{Mesa: "", Local: "", Seccional: "", Localidad: "",*/
                      /*Departamento: "", Provincia: "", Candidato: "",*/
                      /*Partido: "", Puesto: "", Cantidad: 0 }*/
        }

        mess := encodeNewData()
        if _, err := s.Conn.Write(mess); err != nil {
          s.Conn.Close() // TODO: handle properly a write error
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
  *sqlNivel["mesa"] = `
   select g0.nombre as Mesa,
          g1.nombre as Local,
          g2.nombre as Seccional,
          g3.nombre as Localidad,
          g4.nombre as Departamento,
          g5.nombre as Provincia,
          c.nombre as Candidato,
          p.nombre as Partido,
          c.puesto as Puesto
          sum(v.votos) as Cantidad
     from geo g0, geo g1, geo g2, geo g3, geo g4, geo g5,
          candidatos c, partidos p, votos v
    where v.candidato_id = c.id and c.partido_id = p.id
      and v.mesa_id = g0.id
      and g0.contenedor_id = g1.id
      and g1.contenedor_id = g2.id
      and g2.contenedor_id = g3.id
      and g3.contenedor_id = g4.id
      and g4.contenedor_id = g5.id
      and g0.id = %v and c.puesto = '%v'
 group by g5.id, g4.id, g3.id, g2.id, g1.id, g0.id,
          c.nombre, p.nombre, c.puesto
  `
  *sqlNivel["local"] = `
   select g1.nombre as Local,
          g2.nombre as Seccional,
          g3.nombre as Localidad,
          g4.nombre as Departamento,
          g5.nombre as Provincia,
          c.nombre as Candidato,
          p.nombre as Partido,
          c.puesto as Puesto
          sum(v.votos) as Cantidad
     from geo g0, geo g1, geo g2, geo g3, geo g4, geo g5,
          candidatos c, partidos p, votos v
    where v.candidato_id = c.id and c.partido_id = p.id
      and v.mesa_id = g0.id
      and g0.contenedor_id = g1.id
      and g1.contenedor_id = g2.id
      and g2.contenedor_id = g3.id
      and g3.contenedor_id = g4.id
      and g4.contenedor_id = g5.id
      and g1.id = %v and c.puesto = '%v'
 group by g5.id, g4.id, g3.id, g2.id, g1.id,
          c.nombre, p.nombre, c.puesto
  `
  *sqlNivel["seccional"] = `
   select g2.nombre as Seccional,
          g3.nombre as Localidad,
          g4.nombre as Departamento,
          g5.nombre as Provincia,
          c.nombre as Candidato,
          p.nombre as Partido,
          c.puesto as Puesto
          sum(v.votos) as Cantidad
     from geo g0, geo g1, geo g2, geo g3, geo g4, geo g5,
          candidatos c, partidos p, votos v
    where v.candidato_id = c.id and c.partido_id = p.id
      and v.mesa_id = g0.id
      and g0.contenedor_id = g1.id
      and g1.contenedor_id = g2.id
      and g2.contenedor_id = g3.id
      and g3.contenedor_id = g4.id
      and g4.contenedor_id = g5.id
      and g2.id = %v and c.puesto = '%v'
 group by g5.id, g4.id, g3.id, g2.id,
          c.nombre, p.nombre, c.puesto
  `
  *sqlNivel["localidad"] = `
   select g3.nombre as Localidad,
          g4.nombre as Departamento,
          g5.nombre as Provincia,
          c.nombre as Candidato,
          p.nombre as Partido,
          c.puesto as Puesto
          sum(v.votos) as Cantidad
     from geo g0, geo g1, geo g2, geo g3, geo g4, geo g5,
          candidatos c, partidos p, votos v
    where v.candidato_id = c.id and c.partido_id = p.id
      and v.mesa_id = g0.id
      and g0.contenedor_id = g1.id
      and g1.contenedor_id = g2.id
      and g2.contenedor_id = g3.id
      and g3.contenedor_id = g4.id
      and g4.contenedor_id = g5.id
      and g3.id = %v and c.puesto = '%v'
 group by g5.id, g4.id, g3.id,
          c.nombre, p.nombre, c.puesto
  `
  *sqlNivel["departamento"] = `
   select g4.nombre as Departamento,
          g5.nombre as Provincia,
          c.nombre as Candidato,
          p.nombre as Partido,
          c.puesto as Puesto
          sum(v.votos) as Cantidad
     from geo g0, geo g1, geo g2, geo g3, geo g4, geo g5,
          candidatos c, partidos p, votos v
    where v.candidato_id = c.id and c.partido_id = p.id
      and v.mesa_id = g0.id
      and g0.contenedor_id = g1.id
      and g1.contenedor_id = g2.id
      and g2.contenedor_id = g3.id
      and g3.contenedor_id = g4.id
      and g4.contenedor_id = g5.id
      and g0.id = %v and c.puesto = '%v'
 group by g4.id, g4.id,
          c.nombre, p.nombre, c.puesto
  `
  *sqlNivel["provincia"] = `
   select g5.nombre as Provincia,
          c.nombre as Candidato,
          p.nombre as Partido,
          c.puesto as Puesto
          sum(v.votos) as Cantidad
     from geo g0, geo g1, geo g2, geo g3, geo g4, geo g5,
          candidatos c, partidos p, votos v
    where v.candidato_id = c.id and c.partido_id = p.id
      and v.mesa_id = g0.id
      and g0.contenedor_id = g1.id
      and g1.contenedor_id = g2.id
      and g2.contenedor_id = g3.id
      and g3.contenedor_id = g4.id
      and g4.contenedor_id = g5.id
      and g5.id = %v and c.puesto = '%v'
 group by g5.id,
          c.nombre, p.nombre, c.puesto
  `
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
          Subscriptions: make(map[*Subscription]bool) }
        /* keep a mapping from name to id */
        geo2id[geotype][st.Column(1).(string)] = st.Column(0).(int64)
      }
      st.Finalize()
    } else {
      log.Fatalf("GeoTree construction failed. %v", err)
    }
    prevLevel = geotype
  }
}


/* update vote count on DB and notify clients */
func submitListener() {
  db, err := sqlite3.Open("db.sqlite")
  if err != nil {
    log.Fatalln("Failed to connect to DB.")
  }
  defer db.Close()

  /* listen on chanel for updates */
  for s := range dbupdateChannel {
    for _, v := range s.Votos {
      // TODO: tune this, and use 'partido' ?
      sql := fmt.Sprintf(`
        insert into votos select %v, c.id, %v from candidatos c
                           where c.nombre = '%v' and c.puesto = '%v'
      `, geo2id["mesa"][s.Mesa], v.Cantidad, v.Candidato, v.Puesto)

      log.Println(sql)

      if _, err := db.Execute(sql); err != nil {
        log.Fatalf("Submission to DB failed. %v", err)
      } else {
        log.Println("Loaded a vote")
      }
      /* mark the tree dirty propagating upwards */
      n := Geos["mesa"][ geo2id["mesa"][s.Mesa] ]
      for n != nil {
        // TODO: avoid crawling upwards here, do it in notificator ?
        voteupdateChannel <- n
        n = n.Contenedor
      }
    }
  }
}
