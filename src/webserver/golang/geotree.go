package main

import (
  "container/list"
)

type Provincia struct {
  Nombre string
  Departamentos []Departamento
}

type Departamento struct {
  Nombre string
  Contenedor *Provincia
  Seccionales []Seccional
}

type Seccional struct {
  Nombre string
  Contenedor *Departamento
  Locales []Local
}

type Local struct {
  Nombre string
  Contenedor *Seccional
  Mesas []Mesa
}

type Mesa struct {
  Nombre string
  Contenedor *Local
}


type Geo struct {
  Nombre string
  Nivel  string
  Contenedor *Geo
  Contenidos []*Geo
}

/* one list to bind them all */
var Mesas = list.New()
var Locales = list.New()
var Seccionales = list.New()
var Departamentos = list.New()
var Provincias = list.New()

func buildTree() (root *Geo) {
  root = new(Geo)
  return
}
