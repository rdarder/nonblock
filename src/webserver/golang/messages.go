package main

import (
  "json"
  "log"
)

type Message struct {
  Name  string
  Id    string
  Ref   string
  Data  *json.RawMessage
}

type suscribeBody struct {
  Puesto  string
  Nivel   string // nivel de agregacion
  Alcance string // altura en el arbol
  Lugar   string // geo a esta altura
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
  Cantidad      int
}

type submitBody struct {
  Mesa          string
  Votos         []struct {
    Puesto      string
    Candidato   string
    Partido     string
    Cantidad    int
  }
}


func decodeMessage(m []byte) *Message {
  r := new(Message)
  if err := json.Unmarshal(m, r); err != nil {
    log.Println("Failed to decode message: %v", r)
    return nil
  }
  return r
}

func (m *Message)decodeSuscribe() *suscribeBody {
  if m.Name != "suscribe" {
    log.Println("Not a suscribe body.")
    return nil
  }
  b := new(suscribeBody)
  if err := json.Unmarshal(*m.Data, b); err != nil {
    log.Println("Failed to decode suscribe body: %v", m)
    return nil
  }
  return b
}

func (m *Message)decodeCancel() {
  log.Println("Cancel messages don't carry data.")
  return
}

func (m *Message)decodeSubmit() *submitBody {
  if m.Name != "submitvotes" {
    log.Println("Not a submit body.")
    return nil
  }
  b := new(submitBody)
  if err := json.Unmarshal(*m.Data, b); err != nil {
    log.Println("Failed to decode submit body: %v", m)
    return nil
  }
  return b
}

/*func (m *Message)encodeJSON(any interface{}) []byte {*/
  /*if j, err := json.Marshal(m.Data); err != nil {*/
    /*log.Println("Failed to encode message: " + m)*/
    /*return nil*/
  /*}*/

  /*if j, err := json.Marshal(m); err != nil {*/
    /*log.Println("Failed to encode message: " + m)*/
    /*return nil*/
  /*}*/
  /*return j*/
/*}*/
