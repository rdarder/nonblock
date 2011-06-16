package main

import (
  "json"
  "log"
)

type MessageHeader struct {
  Name  string // suscribe | cancel | newdata
  Id    string
  Ref   string
  Data  json.RawMessage
}

type suscribeBody struct {
  Puesto  string
  Nivel   string
  Alcance string
  Lugar   string
}

type newDataBody struct {
  Puesto        string
  Mesa          string
  Local         string
  Seccional     string
  Localidad     string
  Departamento  string
  Provincia     string
  Candidato     string
  Partido       string
  Votos         int
}

type MessageBody interface{}


func decodeMessage(m []byte) (*MessageHeader, MessageBody) {
  h := new(MessageHeader)
  var b interface {}
  var err

  if err := json.Unmarshal(m, h); err == nil {
    switch h.Name {
    case "suscribe":
      b = new(suscribeBody)
    case "cancel":
      b = nil
    case "newdata":
      b = new(newDataBody)
    default:
      log.Println("Don't know how to decode message name: " + h.Name)
    }
  }

  err := json.Unmarshal(h.Data, b)
  return h, b
}


/*func json2message(mess []byte) (m *suscribeMessage) {*/
  /*m = new(suscribeMessage)*/
  /*if err := json.Unmarshal(mess, m); err != nil {*/
    /*log.Fatalln("Failed to parse json.")*/
  /*}*/
  /*return*/
/*}*/

/*func message2json(m interface{}) ([]byte) {*/
  /*j, err := json.Marshal(m)*/
  /*if err != nil {*/
    /*log.Fatalln("Failed to encode json.")*/
  /*}*/
  /*return j*/
/*}*/
