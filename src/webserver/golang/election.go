package main

import (
  "fmt"
  "log"
  "sync"
  "time"
)

type GeoNode struct {
  Nombre string // Lugar
  Tipo   string // Alcance
  Contenedor *GeoNode
  Subscriptions map[string]map[*Subscription]bool // subscriptions by Nivel
  Dirty  bool
  VoteAttrib []string // logging messages
  Mutex  *sync.RWMutex
}

type GeoTree struct {
  Provincias      map[int64]*GeoNode
  Departamentos   map[int64]*GeoNode
  Localidades     map[int64]*GeoNode
  Seccionales     map[int64]*GeoNode
  Locales         map[int64]*GeoNode
  Mesas           map[int64]*GeoNode
  Geos            map[string]map[int64]*GeoNode

  IDProvincias    map[string]int64
  IDDepartamentos map[string]int64
  IDLocalidades   map[string]int64
  IDSeccionales   map[string]int64
  IDLocales       map[string]int64
  IDMesas         map[string]int64
  IDs             map[string]map[string]int64

  GeoContainer    map[string]string
  /* queries indexable by alcance, nivel */
  VoteQueries     map[string]string
}


// TODO: Singleton
func NewGeoTree() (t GeoTree) {
  t.Provincias       = make(map[int64]*GeoNode)
  t.Departamentos    = make(map[int64]*GeoNode)
  t.Localidades      = make(map[int64]*GeoNode)
  t.Seccionales      = make(map[int64]*GeoNode)
  t.Locales          = make(map[int64]*GeoNode)
  t.Mesas            = make(map[int64]*GeoNode)

  t.Geos = map[string]map[int64]*GeoNode{
    "provincia"    : t.Provincias,
    "departamento" : t.Departamentos,
    "localidad"    : t.Localidades,
    "seccional"    : t.Seccionales,
    "local"        : t.Locales,
    "mesa"         : t.Mesas,
  }

  t.IDProvincias     = make(map[string]int64)
  t.IDDepartamentos  = make(map[string]int64)
  t.IDLocalidades    = make(map[string]int64)
  t.IDSeccionales    = make(map[string]int64)
  t.IDLocales        = make(map[string]int64)
  t.IDMesas          = make(map[string]int64)

  t.IDs = map[string]map[string]int64{
    "provincia"    : t.IDProvincias,
    "departamento" : t.IDDepartamentos,
    "localidad"    : t.IDLocalidades,
    "seccional"    : t.IDSeccionales,
    "local"        : t.IDLocales,
    "mesa"         : t.IDMesas,
  }

  t.GeoContainer = map[string]string{
    "provincia"    : "",
    "departamento" : "provincia",
    "localidad"    : "departamento",
    "seccional"    : "localidad",
    "local"        : "seccional",
    "mesa"         : "local",
  }

  t.VoteQueries = map[string]string{}

  t.writeQueries()
  t.build()
  return
}


/* query the database for node structure and build each one */
func (t *GeoTree) build() {
  for _, geotype := range []string{
    "provincia", "departamento", "localidad",
    "seccional", "local", "mesa"} {

    var resultset = make(chan []interface{})

    dbWorkChannel <- &DBJob{
      SQL: fmt.Sprintf(`
              select g.id, g.nombre, g.contenedor_id
                from geo g
               where g.tipo = '%v'
            `, geotype),
      Result: resultset }

    for row := range resultset {
      if idcont, ok := row[2].(int64); ok {
        t.makeNode(idcont, row[0].(int64), row[1].(string), geotype)
      } else {
        t.makeNode(-1, row[0].(int64), row[1].(string), geotype)
      }
    }
  }
  log.Println("Geotree builder: GeoTree built.")
}

func (t *GeoTree) makeNode(parent_id int64, id int64,
                           nombre string, tipo string) {
  /* resolv the container geo node */
  var container *GeoNode = nil
  if t.GeoContainer[tipo] != "" && parent_id != -1 {
    container = t.Geos[t.GeoContainer[tipo]][parent_id]
  }
  /* create the geo node an hook it to the tree */
  t.Geos[tipo][id] =
    &GeoNode{
      Nombre: nombre,
      Tipo: tipo,
      Contenedor: container,
      Subscriptions: map[string]map[*Subscription]bool{},
      Mutex: &sync.RWMutex{},
      Dirty: false,
    }
  /* keep a mapping from name to id */
  t.IDs[tipo][nombre] = id
}

/* queries to obtain votes aggregated by 'Alcance' for each 'Nivel' */
func (t *GeoTree) writeQueries() {
  georanks := map[string]int{
    "provincia":5, "departamento":4, "localidad":3,
    "seccional":2, "local":1, "mesa":0}

  for nivel, rank := range georanks {
    var g0, g1, g2, g3, g4, g5 string
    if rank > georanks["mesa"]         { g0 = "''" } else { g0 = "mesa.nombre"}
    if rank > georanks["local"]        { g1 = "''" } else { g1 = "local.nombre"}
    if rank > georanks["seccional"]    { g2 = "''" } else { g2 = "seccional.nombre"}
    if rank > georanks["localidad"]    { g3 = "''" } else { g3 = "localidad.nombre"}
    if rank > georanks["departamento"] { g4 = "''" } else { g4 = "departamento.nombre"}
    if rank > georanks["provincia"]    { g5 = "''" } else { g5 = "provincia.nombre"}

    for alcance, _ := range georanks {
      t.VoteQueries[nivel + alcance] = fmt.Sprintf(`
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
          and %v.id = ? and c.puesto = ?
     group by Provincia, Departamento, Localidad, Seccional,
              Local, Mesa, Candidato, Partido, Puesto
      `, g0, g1, g2, g3, g4, g5, alcance)
    }
  }
}

func (t *GeoTree) geoID(tipo string, nombre string) int64 {
  if t.IDs[tipo] != nil {
    return t.IDs[tipo][nombre]
  }
  return -1
}

func (t *GeoTree) getNode(alcance string, nombre string) *GeoNode {
  if t.Geos[alcance] != nil {
    return t.Geos[alcance][t.geoID(alcance, nombre)]
  }
  return nil
}

func (t *GeoTree) geoSQL(nivel string, alcance string) string {
  return t.VoteQueries[nivel + alcance]
}

func (n *GeoNode) subscribe(nivel string, subscription *Subscription) {
  // TODO: validate level
  n.Mutex.Lock()
  if n.Subscriptions[nivel] == nil {
    n.Subscriptions[nivel] = map[*Subscription]bool{}
  }
  n.Subscriptions[nivel][subscription] = true, true
  n.Mutex.Unlock()
}

func (n *GeoNode) unsubscribe(nivel string, subscription *Subscription) {
  n.Mutex.Lock()
  n.Subscriptions[nivel][subscription] = false, false
  n.Mutex.Unlock()
}

func (n *GeoNode) markDirty() {
  for n != nil {
    n.Dirty = true
    n = n.Contenedor
  }
}

func (n *GeoNode) unmarkDirty() {
  n.Dirty = false
}

/* centralized tree listener for subscriptions */
func (t *GeoTree) hooker() {
  for s := range subscriptionChannel {
    if s.Subscribe {
      s.Node = t.getNode(s.Request.Alcance, s.Request.Lugar)
      if s.Node != nil {
        s.Node.subscribe(s.Request.Nivel, s)
        // TODO: need to notify just this client, not the whole node
        s.Node.markDirty()
      } else {
        s.Conn.Close()
        log.Printf("Node subscriber: bad geo %v\n", s.Request)
      }
    } else {
      s.Node.unsubscribe(s.Request.Nivel, s)
    }
  }
}


/* walk the tree searching for dirty nodes */
func (t *GeoTree) scanDirty(alcance string) {
  var msgid = 0
  for {
    for _, node := range t.Geos[alcance] {
      if node.Dirty == false {
        time.Sleep(1e+8)
        continue
      }

      /*myTransLog.Printf("transaction:%v:%v::%v\n", time.Nanoseconds(),*/
                        /*txid, s.Ref)*/

      for _, subs := range node.Subscriptions {
        for s, _ := range subs {
          var data []*NewdataBody
          var resultset = make(chan []interface{})

          voteQueryChannel <- &VoteQuery{
            SubscribeBody: s.Request,
            Result: resultset,
          }
          for row := range resultset {
            data = append(data, &NewdataBody{
              Mesa: row[0].(string),
              Local: row[1].(string),
              Seccional: row[2].(string),
              Localidad: row[3].(string),
              Departamento: row[4].(string),
              Provincia: row[5].(string),
              Candidato: row[6].(string),
              Partido: row[7].(string),
              Puesto: row[8].(string),
              Cantidad: row[9].(int64) })
          }

          var m = Message{Name: "newdata", Id: string(msgid), Ref: s.Ref}
          if _, e := s.Conn.Write(m.encodeNewData(data)); e != nil {
            log.Println("Client notifier: send error.")
            s.Conn.Close()
          } else {
            /*myLogger.Printf("newDataSend:%v:%v::%v\n", time.Nanoseconds(),*/
                            /*txid, s.Ref)*/
          }
          msgid++
          data = nil
        }
      }
      node.Dirty = false
    }
  }
}


/* read only structure: safe to use unlocked */
type Candidato struct {
  Id        int64
  Nombre    string
  Puesto    string
  Partido   string
  Geo       int64
}

type Election struct {
  Candidatos  map[string]*Candidato
}

func NewElection() (e Election) {
  e.Candidatos = make(map[string]*Candidato)
  e.loadCandidates()
  return
}

func (e *Election) loadCandidates() {
  var resultset = make(chan []interface{})
  dbWorkChannel <- &DBJob{
    SQL: fmt.Sprintf(`
      select c.id, c.nombre, c.puesto, p.nombre, c.contenedor_id
        from candidatos c, partidos p
       where c.partido_id = p.id`),
    Result: resultset }

  for row := range resultset {
    id := row[0].(int64)
    nombre := row[1].(string)
    puesto := row[2].(string)
    partido := row[3].(string)
    geoid := row[4].(int64)
    e.Candidatos[nombre] =
      &Candidato{
        Id:       id,
        Nombre:   nombre,
        Puesto:   puesto,
        Partido:  partido,
        Geo:      geoid,
      }
  }
  log.Println("Candidate loader: done.")
}

func (e *Election) getCandidato(nombre string) *Candidato {
  return e.Candidatos[nombre]
}
