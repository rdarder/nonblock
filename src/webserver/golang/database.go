package main

import (
  "os"
  "log"
  "time"
  "rand"
  /*"github.com/feyeleanor/gosqlite3"*/
  "github.com/kuroneko/gosqlite3"
)

const SQLITE_OPEN_READONLY = 0x00000001
const SQLITE_OPEN_READWRITE = 0x00000002
const SQLITE_OPEN_NOMUTEX = 0x00008000

type DBJob struct {
  SQL     string
  Result  chan []interface{}
}

/* DB job submission channel */
var dbworkChannel = make(chan *DBJob, 64)

/* a DB worker fetches jobs from a channel and returns results */
func DBWorker() os.Error {
  db, err := sqlite3.Open("db.sqlite", SQLITE_OPEN_NOMUTEX, SQLITE_OPEN_READWRITE)
  if err != nil {
    log.Fatalln("DB worker: failed to connect to DB.")
  }
  defer db.Close()

  RunQuery(db, "PRAGMA synchronous=OFF") // don't wait for data to be on disk
  RunQuery(db, "PRAGMA temp_store=2") // store temp tables in memory
  RunQuery(db, "PRAGMA cache_size=10000") // num pages cached

  /* fetch jobs from the channel */
  for job := range dbworkChannel {
    /* prepare SQL */
    if statement, err := db.Prepare(job.SQL); err == nil {
      var waitct = 0
BUSY_RETRY:
      var e os.Error
      for e = statement.Step(); e == sqlite3.ROW; e = statement.Step() {
        if job.Result != nil {
          job.Result <- statement.Row()
        }
      }
      if e == sqlite3.BUSY && waitct < 10 {
        /*log.Printf("DB worker: DB locked, waiting... %v\n", waitct)*/
        waitct += 1
        time.Sleep(1e+8 * rand.Int63n(10))
        statement.Reset()
        goto BUSY_RETRY
      } else if e != nil {
        log.Printf("DB worker: job failed: %v\n", e.String())
      }
      statement.Finalize()
    } else {
      log.Printf("DB worker: prepare failed: %v\n", job.SQL)
    }
    /* job finished close result channel */
    if job.Result != nil {
      close(job.Result)
    }
  }
  return nil
}

func RunQuery(db *sqlite3.Database, sql string) {
  st, e := db.Prepare(sql)
  if e != nil {
    log.Printf("RunQuery: %v\n", e)
  }
  st.Step()
  st.Finalize()
}
