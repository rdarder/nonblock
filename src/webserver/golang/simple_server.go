package main

import (
  "flag"
  "http"
  "log"
  "websocket"
  "json"
  "os"
  "time"
)

var addr = flag.String("addr", ":8080", "http service address")

var myGeoTree GeoTree
var myElection Election
var myTransLog, myLogger *log.Logger

func setup() {
  go DBWorker()
  go VoteInserter()
  go VoteFetcher()
  go VoteFetcher()

  myGeoTree = NewGeoTree()
  myElection = NewElection()

  go myGeoTree.hooker()
  go myGeoTree.scanDirty("mesa")
  go myGeoTree.scanDirty("local")
  go myGeoTree.scanDirty("seccional")
  go myGeoTree.scanDirty("localidad")
  go myGeoTree.scanDirty("departamento")
  go myGeoTree.scanDirty("provincia")
}


func main() {
  flag.Parse()

  serverlog, _ := os.Create("server.log")
  transclog, _ := os.Create("transaction.log")
  myLogger = log.New(serverlog, "Goserver:", 0)
  myTransLog = log.New(transclog, "Gotransactions:", 0)

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
  // TODO: put a rate limiter on Serve
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

var subscriptionChannel = make(chan *Subscription, 1024)

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
      myLogger.Printf("subscribeRecieved:%v::%v:%v\n", time.Nanoseconds(),
                      message.Id, message.Ref)
      subscriptionChannel <- subscriptions[message.Ref]

    case "cancel":
      myLogger.Printf("cancelRecieved:%v::%v:%v\n", time.Nanoseconds(),
                      message.Id, message.Ref)
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

/* parse message votes and queue them to the DB updater */
func loadVotes(rw http.ResponseWriter, req *http.Request) {
  var message Message
  rw.Header().Set("Connection", "close")
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
    myLogger.Printf("submitVotesRecieved:%v::%v:%v\n", time.Nanoseconds(),
                    message.Id, message.Ref)
    voteInsertChannel <- &submit
    myLogger.Printf("submitVotesInsertedDB:%v::%v:%v\n", time.Nanoseconds(),
                    message.Id, message.Ref)
  } else {
    log.Println("Vote loader: No vote data in message.")
  }
}
