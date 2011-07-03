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

/* listen for messages from clients */
func clientListener(conn *websocket.Conn) {
  /* Subscriptions indexed by client, then by Ref */
  subscriptions := map[string]*Subscription{}
  buf := make([]byte, 1024)

  defer func() {
    // TODO: cleanup subscriptions
    conn.Close()
    for _, s := range subscriptions {
      s.Subscribe = false
      subscriptionChannel <- s
    }
  }()

  for {
    if n, err := conn.Read(buf); err != nil {
      break
    } else {

      if m := decodeMessage(buf[:n]); m != nil {
        switch m.Name {
        case "subscribe":
          if b := m.decodeSubscribe(); b != nil {
            subscriptions[m.Ref] = &Subscription{
              Subscribe: true, Conn: conn, Ref: m.Ref, Request: b }
            subscriptionChannel <- subscriptions[m.Ref]
            log.Println("New subscription.")
          }
        case "cancel":
          /* unregister */
          subscriptions[m.Ref].Subscribe = false
          subscriptionChannel <- subscriptions[m.Ref]
          subscriptions[m.Ref] = nil, false
          log.Println("Canceled subscription.")
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
        log.Println(b)
        dbupdateChannel <- b
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
