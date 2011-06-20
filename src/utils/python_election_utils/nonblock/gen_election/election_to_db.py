import sqlalchemy as sa
from sqlalchemy import sql, orm
import yaml
import logging
import argparse
from .. import db
from . import expanded_votes

def spec_levels(spec):
  return [l['nivel'] for l in spec['niveles']]

def spec_titles(spec):
  return [l['plural'] for l in spec['niveles']]

class ElectionDB(object):
  def __init__(self, config):
    self.tables = config.metadata.tables
    self.spec = config.spec
    self.levels = spec_levels(self.spec)
    self.titles = spec_titles(self.spec)
    self.parties = {}
    self.session = config.session
  def fill_geo(self, election, i=0, parent_container_id=None):
    level = self.levels[i]
    title = self.titles[i]
    if election.has_key(title):
      for name, contents in election[title].items():
        values = {'contenedor_id': parent_container_id,
                  'nombre': name, 'tipo': level}
        r1 = self.session.execute(sql.insert(self.tables['geo'], values))
        my_id = r1.inserted_primary_key[0]
        if contents.has_key('candidatos'):
          for position, candidates in contents['candidatos'].items():
            for candidate in candidates:
              values = {'nombre': candidate['nombre'],
                        'partido_id': self.parties[candidate['partido']],
                        'puesto': position,
                        'contenedor_id': my_id}
              self.session.execute(sql.insert(self.tables['candidatos'],
                                              values))
        #if contents.has_key('votantes'):
        #  values={ 'votantes': contents['votantes'] }
        #  self.session.execute(sql.update(self.tables['votos_check'],
        #  (self.tables['votos_check'].c[self.levels[-1]['nivel'] + '_id'] ==
        #     my_id), values))
        if i < len(self.levels) -1:
          self.fill_geo(contents, i+1, my_id)
  def fill_parties(self):
    for party in self.spec['partidos']:
      r1 = self.session.execute(sql.insert(self.tables['partidos'],
        values=dict(nombre=party['nombre'])))
      self.parties[party['nombre']] = r1.inserted_primary_key[0]
  def fill_votes_check(self):
    tables = expanded_votes.Tables(db.spec, db)
    to_insert = expanded_votes.get_expanded(tables, db.spec)
    db.engine.execute(to_insert)
    pass
  def load(self, election):
    logging.info('inserting parties data')
    self.fill_parties()
    logging.info('inserting geo data')
    self.fill_geo(election)
    logging.info('commiting transaction')
    self.session.commit()
    logging.info('inserting expanded votes (may take a while)')
    self.fill_votes_check()
    self.session.flush()

def main():
  parser = argparse.ArgumentParser(description='create election database')
  parser.add_argument('-e', '--election', type=argparse.FileType('r'),
                      required=True)
  parser.add_argument('-s', '--election-spec', type=argparse.FileType('r'),
                      required=True)
  parser.add_argument('-d', '--db',  default="sqlite:///db.sqlite",
                      help="url format described in "
                      "http://www.sqlalchemy.org/docs/core/engines.html")
  parser.add_argument('-v', '--verbose', action='store_true', default=False)
  parser.add_argument('-D', '--debug', action='store_true', default=False)
  args = parser.parse_args()

  if args.verbose:
    logging.basicConfig(level=logging.INFO)

  logging.info('loading election spec')
  election_spec = yaml.load(args.election_spec)

  logging.info('loading election')
  election = yaml.load(args.election)

  db.setup(election_spec, args.db, True, args.debug)

  logging.info('loading election into the db')
  election_db = ElectionDB(db)
  election_db.load(election)
  logging.info('finished')
