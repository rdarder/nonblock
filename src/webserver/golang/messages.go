package main

import (
  "json"
  "log"
  "os"
)

/* generic message header */
type Message struct {
  Name  string
  Id    string
  Ref   string
  Data  *json.RawMessage // message payload
}

type SubscribeBody struct {
  Puesto  string
  Nivel   string // nivel de agregacion
  Alcance string // altura en el arbol
  Lugar   string // geo a esta altura
}

type SubmitBody struct {
  Mesa          string      "mesa"
  Votos         []struct {
    Puesto      string      "puesto"
    Candidato   string      "candidato"
    Partido     string      "partido"
    Cantidad    int64       "cant"
  }                         "votos"
}

/* decode a message's payload into propper message */
func (m *Message)DecodeData(b interface{}) os.Error {
  switch m.Name {
  case "subscribe":
    return json.Unmarshal(*m.Data, b.(*SubscribeBody))
  case "submitVotes":
    return json.Unmarshal(*m.Data, b.(*SubmitBody))
  }
  return os.EINVAL
}


type NewdataBody struct {
  Mesa          string
  Local         string
  Seccional     string
  Localidad     string
  Departamento  string
  Provincia     string
  Candidato     string
  Partido       string
  Puesto        string
  Cantidad      int64
}


func (m *Message)EncodeData(b interface{}) ([]byte, os.Error) {
  switch body := b.(type) {
  case NewdataBody:
    m.Name = "newdata"
    /*json.Marshal(body)*/
  }
  return nil, os.EINVAL
}


func (m *Message)encodeNewData(nd []*NewdataBody) []byte {
  if j, err := json.Marshal(nd); err == nil {
    i := json.RawMessage(j)
    m.Data = &i
    if k, err := json.Marshal(m); err == nil {
      return k
    }
    log.Println("Failed to encode Message")
    return nil
  }
  log.Println("Failed to encode newData")
  return nil
}
