/* Try a one goroutine per client since theyŕe supposed to be very light */

package main

import (
  "github.com/kuroneko/gosqlite3"
  "fmt"
)


func attend_requests()
func notify_clients()
func client_suscribe()
func handleRequest()


/*
{
  name: ‘subscribe’,
  data: {
    puesto: “intendente”,
    alcance:  “localidad”, (could be: Provincia, Pais)
    nivel:  “mesa”,
    valor_alc:  “Rosario”, (could be: Santa Fe, Argentina)
    valor_niv:  “172”
  }
}
*/

func main() {
  if db, e := sqlite3.Open("db.sqlite"); e != nil {
    panic("Failed to open db.")
  } else {

    db.Execute("select type, name from geo", func(s *sqlite3.Statement, params ...interface{}) {
      fmt.Printf("%v: %v\n", s.ColumnName(0), s.Column(0))
    })

    db.Close()
  }
}
