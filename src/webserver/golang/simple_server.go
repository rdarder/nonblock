package main

import (
  "flag"
  "http"
  "log"
  "websocket"
  "json"
  "os"
)

var addr = flag.String("addr", ":8080", "http service address")
var NUM_DB_WORKERS = flag.Int("dbworkers", 10, "Number of DB workers")
var NUM_NOTIFIERS = flag.Int("notifiers", 10, "Number of client notifiers")
var NUM_SUSCRIBERS = flag.Int("suscribers", 2, "Number of client suscribers")
var NUM_LISTENERS = flag.Int("listeners", 1, "Number of vote listeners")

var geoTree GeoTree

func setup() {
  for i := 0; i < *NUM_DB_WORKERS; i++ {
    go DBWorker()
  }
  for i := 0; i < *NUM_NOTIFIERS; i++ {
    go clientNotifier()
  }
  for i := 0; i < *NUM_SUSCRIBERS; i++ {
    go submitListener()
  }
  geoTree = NewGeoTree()
  for i := 0; i < *NUM_LISTENERS; i++ {
    go geoTree.listenSubscriptions()
  }
}


func main() {
  flag.Parse()

  setup()

  /* these handlers apply on the defautl ServeMux */
  http.HandleFunc("/", serveWebClient)
  http.HandleFunc("/favicon.ico",
    func(rw http.ResponseWriter, r *http.Request) {
      rw.WriteHeader(http.StatusBadRequest)
    })
  http.HandleFunc("/js/", serveJSFile)
  http.HandleFunc("/submit", loadVotes)
  /* spawn a client listener for each new ws connection */
  http.HandleFunc("/ws",
    func(rw http.ResponseWriter, r *http.Request) {
      if _, found := r.Header["Sec-Websocket-Key1"]; found {
        websocket.Handler(clientHandler).ServeHTTP(rw, r)
      } else {
        websocket.Draft75Handler(clientHandler).ServeHTTP(rw, r)
      }
    })

  if err := http.ListenAndServe(*addr, nil); err != nil {
    log.Fatal("ListenAndServe:", err)
  }
}


type Subscription struct {
  Subscribe bool
  Ref       string
  Request   *SubscribeBody
  Node      *GeoNode
  Conn      *websocket.Conn
}

var subscriptionChannel = make(chan *Subscription, 32)

/* handle each client individually */
func clientHandler(conn *websocket.Conn) {
  /* register all client subscriptions */
  var subscriptions = map[string]*Subscription{}
  /* client clean up */
  defer func() {
    conn.Close()
    for _, s := range subscriptions {
      if s.Node != nil {
        s.Subscribe = false
        subscriptionChannel <- s
      }
    }
    log.Println("Client handler: cleanup done.")
  }()
  /* listen for client messages */
  decoder := json.NewDecoder(conn)
  for {
    var message Message
    if e := decoder.Decode(&message); e != nil {
      if e != os.EOF {
        log.Printf("Client handler: failed to parse message. %v\n", e)
      }
      return
    }
    switch message.Name {

    case "subscribe":
      var subscribe SubscribeBody
      if message.DecodeData(&subscribe) != nil {
        log.Println("Client handler: not a suscribe message.")
        return
      }
      subscriptions[message.Ref] =
        &Subscription{
          Subscribe: true,
          Conn: conn,
          Ref: message.Ref,
          Request: &subscribe,
        }
      subscriptionChannel <- subscriptions[message.Ref]

    case "cancel":
      subscriptions[message.Ref].Subscribe = false
      subscriptionChannel <- subscriptions[message.Ref]
      /* delete subcription */
      subscriptions[message.Ref] = nil, false

    default:
      log.Printf("Client handler: unknown message: %v\n", message.Name)
      return
    }
  }
}


/* the http module forks one goroutine per post,
   have each gorouting wait until it can update the DB,
   this means no buffering on the channel */
var dbupdateChannel = make(chan *SubmitBody, 4)

/* parse message votes and queue them to the DB updater */
func loadVotes(rw http.ResponseWriter, req *http.Request) {
  var message Message
  if e := json.NewDecoder(req.Body).Decode(&message); e != nil {
    rw.WriteHeader(http.StatusBadRequest)
    log.Printf("Vote loader: failed to parse message. %v\n", e)
    return
  }
  var submit SubmitBody
  if message.DecodeData(&submit) != nil {
    rw.WriteHeader(http.StatusBadRequest)
    log.Println("Vote loader: not a submit message.")
    return
  }
  rw.WriteHeader(http.StatusOK)
  if len(submit.Votos) > 0 {
    dbupdateChannel <- &submit
  } else {
    log.Println("Vote loader: No vote data in message.")
  }
}
