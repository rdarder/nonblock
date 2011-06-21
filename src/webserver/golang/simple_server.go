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


/* just a message to suscribe/unsuscribe */
type wsSubscription struct {
  ws        *websocket.Conn
  subscribe bool
}
/* a channel to communicate the message */
var subscriptionChannel = make(chan wsSubscription)

/* a channel to communicate a DB update TODO: buffer ? */
var dbupdateChannel = make(chan *submitBody)

/* Client notificator */
func notificator() {
}

/* listen for messages from clients */
func clientListener(conn *websocket.Conn) {
  defer func() {
    subscriptionChannel <- wsSubscription{conn, false}
    conn.Close()
  }()
  /* suscribe the websocket */
  subscriptionChannel <- wsSubscription{conn, true}

  buf := make([]byte, 1024)
  for  {
    if n, err := conn.Read(buf); err != nil {
      /* TODO: cancel registrations */
      break
    } else {

      if m := decodeMessage(buf[:n]); m != nil {
        switch m.Name {
        case "suscribe":
          if b := m.decodeSuscribe(); b != nil {
            /* register */
            //TODO: register
          }
        case "cancel":
          /* unregister */
          //TODO: cancel
          log.Println("Cancel")
        default:
          log.Println("Recieved unknown message: " + m.Name)
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
        /*log.Println(b)*/
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