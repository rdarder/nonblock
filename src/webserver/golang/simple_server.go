package main

import (
  "flag"
  "http"
  "log"
  "websocket"
)

var addr = flag.String("addr", ":8080", "http service address")

var requests = make(chan *suscribeMessage)
var responses = make(chan *newDataMessage)

func main() {
  /* parse commandline options */
  flag.Parse()

  go voteFetcher(requests, responses)

  /* these handlers apply on the defautl ServeMux */
  http.HandleFunc("/", serveWebClient)
  http.HandleFunc("/js/", serveJSFile)
  http.HandleFunc("/submit", loadVotes)

  http.HandleFunc("/ws", func(rw http.ResponseWriter, r *http.Request) {
    // Handle old and new versions of protocol.
    if _, found := r.Header["Sec-Websocket-Key1"]; found {
      websocket.Handler(clientHandler).ServeHTTP(rw, r)
    } else {
      websocket.Draft75Handler(clientHandler).ServeHTTP(rw, r)
    }
  })

  /* spawn new http server: forks a goroutine per request */
  if err := http.ListenAndServe(*addr, nil); err != nil {
    log.Fatal("ListenAndServe:", err)
  }
}


func clientHandler(conn *websocket.Conn) {
  defer conn.Close()
  buf := make([]byte, 1024)

  for {
    if n, err := conn.Read(buf); err == nil && n > 0 {
      log.Printf("Suscribe Message: %v\n", string(buf[:n]))
      requests <- json2message(buf[:n]) //.(*suscribeMessage)
      /*conn.Write([]byte("Connected"));*/
    } else {
      break
    }
  }
}
