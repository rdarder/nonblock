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

type subscribeBody struct {
  Puesto  string
  Nivel   string // nivel de agregacion
  Alcance string // altura en el arbol
  Lugar   string // geo a esta altura
}

type newDataBody struct {
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

type submitBody struct {
  Mesa          string
  Votos         []struct {
    Puesto      string
    Candidato   string
    Partido     string
    Cantidad    int64
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

func (m *Message)decodeSubscribe() *subscribeBody {
  if m.Name != "subscribe" {
    log.Println("Not a subscribe body.")
    return nil
  }
  b := new(subscribeBody)
  if err := json.Unmarshal(*m.Data, b); err != nil {
    log.Println("Failed to decode subscribe body: %v", m)
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

func (m *Message)encodeNewData(nd *newDataBody) []byte {
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
