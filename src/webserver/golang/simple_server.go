package main

import (
  "flag"
  "http"
  "log"
  "websocket"
  "sync"
)

var addr = flag.String("addr", ":8080", "http service address")

/*var requests = make(chan *suscribeMessage)*/
/*var responses = make(chan *newDataMessage)*/

var connections struct {
  websocks  map[*websocket.Conn]int
  lock      sync.RWMutex
}

/* initalize stuff prior to entry point */
func init() {
  connections.websocks = make(map[*websocket.Conn]int)
}

func main() {
  /* parse commandline options */
  flag.Parse()

  /*go voteFetcher(requests, responses)*/

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


/* wait for updates from db or messages from clients */
func hub() {
  for {

  }
}

/* listen for messages from clients */
func clientListener(conn *websocket.Conn) {
  defer conn.Close()
  defer func() {
    connections.lock.Lock()
    connections.websocks[conn] = 0, false // remove connection from map
    connections.lock.Unlock()
  }()

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
          }
        case "cancel":
          /* unregister */
          log.Println("Cancel")
        default:
          log.Println("Recieved unknown message: " + m.Name)
        }
      }
    }
  }
}


func loadVotes(c http.ResponseWriter, req *http.Request) {
  log.Println("noop, Loading a vote.")
  /* signal new data */
  /*newdata <- true*/
}
