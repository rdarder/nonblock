package main

import (
  "os"
  "log"
  "github.com/feyeleanor/gosqlite3"
  /*"github.com/kuroneko/gosqlite3"*/
)

const GLOBAL_DB_FILE = "db.sqlite"

/* get a DB handle and set parameters  */
func NewDBHandle() (db *sqlite3.Database) {
  db, err := sqlite3.Open(GLOBAL_DB_FILE)
  if err != nil {
    log.Fatalln("DB worker: failed to connect to DB.")
  }
  db.Execute("PRAGMA synchronous=OFF") // don't wait for data to be on disk
  db.Execute("PRAGMA temp_store=2") // store temp tables in memory
  db.Execute("PRAGMA cache_size=10000") // num pages cached
  db.Execute("PRAGMA journal_mode=MEMORY") // keep journal in memory
  return
}


/* a generic sql job structure */
type DBJob struct {
  SQL     string
  Result  chan []interface{}
}

/* DB job submission channel */
var dbWorkChannel = make(chan *DBJob)

/* a general purpose DB worker */
func DBWorker() os.Error {
  db := NewDBHandle()
  defer db.Close()

  for job := range dbWorkChannel {
    if statement, err := db.Prepare(job.SQL); err == nil {
      var e os.Error
      for e = statement.Step(); e == sqlite3.ROW; e = statement.Step() {
        if job.Result != nil {
          job.Result <- statement.Row()
        }
      }
      if e != nil {
        log.Printf("DB worker: job failed: %v\n", e)
      }
      statement.Finalize()
    } else {
      log.Printf("DB worker: prepare failed: %v\n", job.SQL)
    }
    if job.Result != nil {
      close(job.Result)
    }
  }
  return nil
}


/* mass insertion channel */
var voteInsertChannel = make(chan *SubmitBody, 2048)

/* a worker for mass insertion */
func VoteInserter () os.Error {
  db := NewDBHandle()
  defer db.Close()
  var mesa, candidato int64
  if st, e := db.Prepare("insert into votos values(?, ?, ?)"); e == nil {
    for submission := range voteInsertChannel {
      // HINT: use channel length to delimit transactions ?
      db.Begin()
      mesa = myGeoTree.geoID("mesa", submission.Mesa)
      for _, voto := range submission.Votos {
        candidato = myElection.getCandidato(voto.Candidato).Id
        if e, _ := st.BindAll(mesa, candidato, voto.Cantidad); e != nil {
          log.Fatalf("VoteInserter: failed to bind variables: %v\n", e)
        }
        if e := st.Step(); e != nil {
          log.Printf("VoteInserter: job failed: %v\n", e)
        }
        st.Reset()
      }
      db.Commit()
      // all upstream has to be notified
      myGeoTree.getNode("mesa", submission.Mesa).markDirty()
    }
    st.Finalize()
  } else {
    log.Fatalln("VoteInserter: failed to prepare statement.")
    return e
  }
  return nil
}

type VoteQuery struct {
  *SubscribeBody
  Result  chan []interface{}
}

/* mass insertion channel */
var voteQueryChannel = make(chan *VoteQuery, 2048)

func VoteFetcher() os.Error {
  db := NewDBHandle()
  statements := make(map[string]*sqlite3.Statement)

  defer func() {
    for _, st := range statements {
      st.Finalize()
    }
    db.Close()
  }()

  for query := range voteQueryChannel {
    var stkey = query.Nivel + query.Alcance
    var st = statements[stkey]
    var e os.Error

    if st == nil {
      if st, e = db.Prepare(myGeoTree.geoSQL(query.Nivel, query.Alcance)); e != nil {
        log.Fatalf("VoteFetcher: Failed to prepare statement: %v\n", e)
      } else {
        statements[stkey] = st
      }
    }

    if err, _ := st.BindAll(myGeoTree.geoID(query.Alcance, query.Lugar),
                            query.Puesto); err == nil {
      db.Begin()
      for e = st.Step(); e == sqlite3.ROW; e = st.Step() {
        if query.Result != nil {
          query.Result <- st.Row()
        }
      }
      db.Commit()
      if e != nil {
        log.Printf("VoteFetcher: job failed: %v\n", e)
      }
      if query.Result != nil {
        close(query.Result)
      }
    } else {
      log.Fatalf("VoteFetcher: failed to bind variables: %v\n", err)
    }
    st.Reset()
  }
  return nil
}
