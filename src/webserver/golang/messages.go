package main

import (
  "json"
  "log"
)

type _messageHeader struct {
  Name  string // suscribe | cancel | newdata
  Id    string
  Ref   string
}

type _messageSuscribeData struct {
  Puesto        string
  Nivel         string
  Alcance       string
  Lugar         string
}

type _messageNewDataData struct {
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

type suscribeMessage struct {
  _messageHeader
  Data _messageSuscribeData
}

type newDataMessage struct {
  _messageHeader
  Data _messageNewDataData
}

func json2message(mess []byte) (m *suscribeMessage) {
  m = new(suscribeMessage)
  if err := json.Unmarshal(mess, m); err != nil {
    log.Fatalln("Failed to parse json.")
  }
  return
}

/*func message2json(mess *Message) ([]byte) {*/
  /*j, err := json.Marshal(mess)*/
  /*if err {*/
    /*log.Fatalln("Failed to encode json.")*/
  /*}*/
  /*return j*/
/*}*/
