package main


type geotree struct {

  Provincias []struct {
    Provincia   string

    Departamentos []struct {
      Departamento  string

      Seccionales []struct {
        Seccional   string

        Locales []struct {
          Local   string

          Mesas []struct {
            Mesa  string

          }
        }
      }
    }
  }
}
