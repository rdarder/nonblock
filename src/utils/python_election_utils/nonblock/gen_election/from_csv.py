from pkg_resources import resource_filename
import csv
import pprint
import sqlalchemy

class sfe_dialect(csv.Dialect):
  delimiter=';'
  doublequote=True
  escapechar=None
  lineterminator='\r\n'
  quotechar='"'
  quoting=0
  skipinitialwhitespace=False

#@pypes.stream_filter
def read_csv(fname):
  f = open(fname, 'rb')
  for x in range(5):
    f.readline()
  return csv.DictReader(f, dialect=sfe_dialect)

elecciones = {
'concejales': ['data/2011-prim-conc-localis.csv',
              'data/2011-prim-conc-rosario.csv',
              'data/2011-prim-conc-santafe.csv',
              'data/2011-prim-ccom-localis.csv'],
'intendentes': ['data/2011-prim-inte-localis.csv',
              'data/2011-prim-inte-rosario.csv',
              'data/2011-prim-inte-santafe.csv'],
'diputados': ['data/2011-prim-dipu-localis.csv',
             'data/2011-prim-dipu-rosario.csv',
             'data/2011-prim-dipu-santafe.csv'],
'senadores': ['data/2011-prim-sena-localis.csv',
             'data/2011-prim-sena-rosario.csv',
             'data/2011-prim-sena-santafe.csv'],
'gobernadores': ['data/2011-prim-gobe-localis.csv',
             'data/2011-prim-gobe-rosario.csv',
             'data/2011-prim-gobe-santafe.csv']
}


def strip_dict(d):
  for k in d:
    if d[k]:
      d[k] = d[k].strip()

def load_sfe_csv():
  deptos = {}
  localidades = {}
  rnd_nombres = []
  rnd_apellidos = []
  partidos = {}
  for cargo, docs in elecciones.items():
    for doc in docs:
      for row in read_csv(resource_filename('nonblock.gen_election',
                                            doc)):
        try:
          strip_dict(row)
          loc = row['Localidad']
          if not loc.strip() or (loc.isdigit() and int(loc) == 0): #XXX str
            continue
          if row['Nombre Partido'] == 'Total de votos emitidos':
            continue

          if not deptos.get(row['Depto']):
            deptos[row['Depto']]= {'nombre': row['Nombre Departamento']}
          if not localidades.get(row['Localidad']):
            localidades[row['Localidad']] = {'nombre': row['Nombre Localidad'],
                                                  'departamento':
                                                     row['Depto'],
                                            }
          partidos[row['Partido']] = {'nombre': row['Nombre Partido']}

          if not localidades[row['Localidad']].get(cargo):
            localidades[row['Localidad']][cargo] = []
          localidades[row['Localidad']][cargo].append(
            {'candidato': row['Nombre Candidato'] or row['Nombre Partido'],
             'votos': int(row['Votos']),
             'partido': row['Partido']})
        except:
          print(doc)
          raise

  return {'deptos': deptos, 'localidades': localidades }

def total_votes(raw_loc, *cargos):
  return [sum([k[cargo]['votos'] for k in raw_loc.values]) for cargo in cargos]

def build_geo_tables(raw):
  pass

def main():
 raw = load_sfe_csv()
 pprint.pprint(raw)



