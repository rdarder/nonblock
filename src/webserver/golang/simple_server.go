package main

import (
  "flag"
  "http"
  "log"
  "websocket"
)

var addr = flag.String("addr", ":8080", "http service address")


func main() {
  /* parse commandline options */
  flag.Parse()

  /* these handlers apply on the defautl ServeMux */
  http.HandleFunc("/", serveWebClient)
  http.HandleFunc("/js/", serveJSFile)
  http.HandleFunc("/submit", loadVotes)
  /* spawn a client listener for each new ws connection */
  http.HandleFunc("/ws", func(rw http.ResponseWriter, r *http.Request) {
    if _, found := r.Header["Sec-Websocket-Key1"]; found {
      websocket.Handler(clientListener).ServeHTTP(rw, r)
    } else {
      websocket.Draft75Handler(clientListener).ServeHTTP(rw, r)
    }
  })

  /* spawn new http server: forks a goroutine per request */
  if err := http.ListenAndServe(*addr, nil); err != nil {
    log.Fatal("ListenAndServe:", err)
  }
}


type Subscription struct {
  Subscribe bool
  Ref       string
  Request   *subscribeBody
  Node      *GeoNode
  Conn      *websocket.Conn
}

var subscriptionChannel = make(chan *Subscription, 10)

/* a channel to communicate a DB update, buffer upto 1024 reqs */
var dbupdateChannel = make(chan *submitBody, 128)

/* Subscriptions indexed by client, then by Ref */
var ClientSubscriptions = map[*websocket.Conn]map[string]*Subscription{}


/* listen for messages from clients */
func clientListener(conn *websocket.Conn) {
  defer func() {
    // TODO: cleanup subscriptions
    conn.Close()
    for _, subs := range ClientSubscriptions[conn] {
      subs.Subscribe = false
      subscriptionChannel <- subs
    }
  }()

  buf := make([]byte, 1024)
  for  {
    if n, err := conn.Read(buf); err != nil {
      break
    } else {

      if m := decodeMessage(buf[:n]); m != nil {
        switch m.Name {
        case "subscribe":
          if b := m.decodeSubscribe(); b != nil {
            subscriptionChannel <- &Subscription{
              Subscribe: true, Conn: conn, Ref: m.Ref, Request: b }
          }
        case "cancel":
          /* unregister */
          subscriptionChannel <- &Subscription{
            Subscribe: false, Conn: conn, Ref: m.Ref }
        default:
          log.Println("Recieved unknown message: " + m.Name)
          return
        }
      }
    }
  }
}


func loadVotes(rw http.ResponseWriter, req *http.Request) {
  buf := make([]byte, 1024)

  if n, err := req.Body.Read(buf); err != nil {
    rw.WriteHeader(http.StatusBadRequest)
      log.Println("Couldn't read body.")
  } else {
    /* parse message header */
    if m := decodeMessage(buf[:n]); m != nil {
      /* decode body */
      if b := m.decodeSubmit(); b != nil {
        rw.WriteHeader(http.StatusOK)
        /* update votes */
        dbupdateChannel <- b
        log.Println(b)
      } else {
        rw.WriteHeader(http.StatusBadRequest)
        log.Println("Couldn't decode submit.")
      }
    } else {
      rw.WriteHeader(http.StatusBadRequest)
      log.Println("Couldn't decode message.")
    }
  }
}
