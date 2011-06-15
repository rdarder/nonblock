package main

import (
  "fmt"
  "log"
  "http"
  // goinstall -v -dashboard=true github.com/kuroneko/gosqlite3
  "github.com/kuroneko/gosqlite3"
)


func buildQuery(m *suscribeMessage) (sql string) {
  sql = fmt.Sprintf(`
    select c.name, c.position, g.name, g.type, sum(v.total_votes)
      from candidates c, votes v, geo g
     where c.id = v.candidate_id
       and g.id = v.voting_center_id
       and c.position like '%v'
       and g.type like '%v'
  group by c.name, c.position, g.name, g.type
  `, m.Data.Puesto, m.Data.Nivel)
  return
}


func voteFetcher(req chan *suscribeMessage, ret chan *newDataMessage) {
  db, err := sqlite3.Open("db.sqlite")
  if err != nil {
    log.Fatalln("Failed to connect to DB.")
  }
  defer db.Close()

  /* read messages */
  for m := range req {
    sql := buildQuery(m)
    log.Println("Parsed sql: " + sql)

    if st, err := db.Prepare(sql); err == nil {
      for {
        if err = st.Step(); err != nil {
          r:= st.Row()
          log.Println(r)

        } else {
          break
        }
      }
      st.Finalize()
    }
  }
}

func loadVotes(c http.ResponseWriter, req *http.Request) {
  log.Println("noop, Loading a vote.")
}
