package main

import (
  "log"
  "fmt"
)

/* a channel to signal Updates */
var voteupdateChannel = make(chan *GeoNode, 128)

/* Client notificator */
func clientNotifier() {
  for stalenode := range voteupdateChannel {
    for nivel, subscriptions := range stalenode.Subscriptions {
      for s, _ := range subscriptions {

        var data []*NewdataBody
        var resultset = make(chan []interface{})

        dbworkChannel <- &DBJob{
          SQL: geoTree.geoSQL(nivel, s.Request.Alcance,
                              s.Request.Lugar, s.Request.Puesto),
          Result: resultset }

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

        var m = Message{Name: "newdata", Id: "481234", Ref: s.Ref}
        if _, e := s.Conn.Write(m.encodeNewData(data)); e != nil {
          /* this should end client's handler and unsuscribe everything */
          s.Conn.Close()
          log.Println("Client notifier: send error.")
        } else {
          log.Println("Client notifier: notification sent.")
        }
        data = nil
      }
    }
  }
}

/* update vote count on DB and notify clients */
func submitListener() {
  var subct int64
  for s := range dbupdateChannel {
    for _, v := range s.Votos {
      dbworkChannel <- &DBJob{
        SQL: fmt.Sprintf(`
          insert into votos
          select %v, c.id, %v from candidatos c
           where c.nombre = '%v' and c.puesto = '%v'
          `, geoTree.geoID("mesa", s.Mesa),
          v.Cantidad, v.Candidato, v.Puesto),
        Result: nil }
      // TODO: nodes get notified even on failure
      subct += 1
      if subct % 100 == 0 {
        log.Println("Submit listener: 100 submission mark.")
      }
      /* signal an update on the node */
      var n = geoTree.getNode("mesa", s.Mesa)
      for n != nil {
        /*log.Printf("Submit listener: notifying %v - %v\n", n.Tipo, n.Nombre)*/
        voteupdateChannel <- n
        n = n.Contenedor
      }
    }
  }
}
