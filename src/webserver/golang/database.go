package main

import (
  "fmt"
  "log"
  "websocket"
  // goinstall -v -dashboard=true github.com/kuroneko/gosqlite3
  "github.com/kuroneko/gosqlite3"
)

/* initialization */
func init() {
  /*buildGeoQueries()*/
  buildGeoTree()
  go submitListener()
}

type GeoNode struct {
  Nombre string // Lugar
  Tipo   string // Alcance
  Contenedor *GeoNode
  Clients []*websocket.Conn
  Dirty  bool
  SQL    *string
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
  *sqlNivel["provincia"] = `
   select v.Puesto, g0.Mesa, g1.Local, g2.Seccional, v.Localidad,
          v.Departamento, v.Provincia, v.Candidato, v.Cantidad
     from votos_expandidos v
    where v.x = '%v'
 group by v.Provincia
  `
}


/* one list to bind them all */
var Geos = map[string]map[int64]*GeoNode{
  "provincia"    : make(map[int64]*GeoNode),
  "departamento" : make(map[int64]*GeoNode),
  "localidad"    : make(map[int64]*GeoNode),
  "seccional"    : make(map[int64]*GeoNode),
  "local"        : make(map[int64]*GeoNode),
  "mesa"         : make(map[int64]*GeoNode) }

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

    log.Print(sql)

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
          SQL: sqlNivel[geotype],
          Dirty: true }
      }
      st.Finalize()
    } else {
      log.Fatalf("GeoTree construction failed. %v", err)
    }
    prevLevel = geotype
  }
}


/* single tree updater to avoid locking */
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
        insert into votos select g.id, c.id, %v from candidatos c, geo g
                           where c.nombre = '%v' and c.puesto = '%v'
                             and g.nombre = '%v' and g.tipo = 'mesa'
      `, v.Cantidad, v.Candidato, v.Puesto, s.Mesa)

      if _, err := db.Execute(sql); err != nil {
        log.Fatalf("Submission to DB failed. %v", err)
      } else {
        log.Println("Loaded a vote")
      }
      /* TODO: mark the tree dirty propagating upwards */
      // <- GeoNode
    }
  }
}
