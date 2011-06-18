package main

import (
  "fmt"
  "log"
  // goinstall -v -dashboard=true github.com/kuroneko/gosqlite3
  "github.com/kuroneko/gosqlite3"
)

/* initialization */
func init() {
  buildGeoTree()
}

type GeoNode struct {
  Nombre string
  Tipo   string
  Contenedor *GeoNode
}

/* one list to bind them all */
var Mesas = make(map[int]*GeoNode)
var Locales = make(map[int]*GeoNode)
var Seccionales = make(map[int]*GeoNode)
var Localidades = make(map[int]*GeoNode)
var Departamentos = make(map[int]*GeoNode)
var Provincias = make(map[int]*GeoNode)
var Geos = map[string]map[int]*GeoNode{
  "provincia": Provincias,
  "departamento": Departamentos,
  "localidad": Localidades,
  "seccional": Seccionales,
  "local": Locales,
  "mesa": Mesas }


func buildGeoTree() () {
  db, err := sqlite3.Open("db.sqlite")
  if err != nil {
    log.Fatalln("Failed to connect to DB.")
  }
  defer db.Close()

  // maps are a reference type
  var prevLevel map[int]*GeoNode = nil

  for geotype, geomap := range Geos {
    sql := fmt.Sprintf(`
      select g.id, g.name, g.contenedor_id
        from geo g
       where g.tipo = '%v'
    `, geotype)

    if st, err := db.Prepare(sql); err == nil {
      for st.Step() != nil {
        /* get the parent */
        var container *GeoNode = nil
        if prevLevel != nil {
          container = prevLevel[st.Column(2).(int)]
        }
        /* create the node */
        geomap[st.Column(0).(int)] = &GeoNode{
          Nombre: st.Column(1).(string),
          Tipo: geotype,
          Contenedor: container}
      }
      st.Finalize()
    } else {
      log.Fatalln("GeoTree construction failed.")
    }
    prevLevel = geomap
  }
}



func voteFetcher(req chan *suscribeBody, ret chan interface{}) {
  db, err := sqlite3.Open("db.sqlite")
  if err != nil {
    log.Fatalln("Failed to connect to DB.")
  }
  defer db.Close()

  /* read messages */
  /*for m := range req {*/
    /*sql := buildQuery(m)*/
    /*log.Println("Parsed sql: " + sql)*/

    /*if st, err := db.Prepare(sql); err == nil {*/
      /*for st.Step() != nil {*/
        /*r:= st.Row()*/
        /*log.Println(r)*/
      /*}*/
      /*st.Finalize()*/
    /*}*/
    /*ret <- &newDataMessage{}*/
  /*}*/
}
