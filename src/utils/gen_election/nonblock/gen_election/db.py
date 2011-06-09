import sqlalchemy as sa
from sqlalchemy import sql, orm
import yaml
import logging
import argparse

def spec_levels(spec):
  return [l['level'] for l in spec['levels']]

def spec_titles(spec):
  return [l['title'] for l in spec['levels']]

def add_geo_columns(spec, table):
  last = spec_levels[-1]

def create_n_tables(spec, election, metadata):
  levels = spec_levels(spec)
  positions = spec['positions']
  sa.Table('geo', metadata,
           sa.Column('id', sa.Integer, primary_key=True),
           sa.Column('name', sa.String(64), nullable=False),
           sa.Column('type', sa.Enum(*levels), nullable=False),
           sa.Column('container_id', sa.Integer,
                     sa.ForeignKey('geo.id'))
          )
  sa.Table('parties', metadata,
           sa.Column('id', sa.Integer, primary_key=True),
           sa.Column('name', sa.String(64), nullable=False),
          )
  sa.Table('positions', metadata,
           sa.Column('id', sa.Integer, primary_key=True),
           sa.Column('name', sa.String(64), nullable=False),
          )
  sa.Table('candidates', metadata,
           sa.Column('id', sa.Integer, primary_key=True),
           sa.Column('name', sa.String(64), nullable=False),
           sa.Column('party_id', sa.Integer, sa.ForeignKey('parties.id')),
           sa.Column('position', sa.Enum(*positions), nullable=False),
           sa.Column('container_id', sa.Integer,
                     sa.ForeignKey('geo.id'))
          )
  votes_check = sa.Table('votes_check', metadata,
                         sa.Column('total_votes', sa.Integer, nullable=False),
                         sa.Column('voting_center_id',
                                   sa.Integer, sa.ForeignKey('geo.id'))
                        )
  votes = sa.Table('votes', metadata,
                   sa.Column('voting_center_id',
                             sa.Integer, sa.ForeignKey('geo.id'),
                             primary_key=True),
                   sa.Column('candidate_id', sa.Integer,
                             sa.ForeignKey('candidates.id'),
                             primary_key=True),
                   sa.Column('votes', sa.Integer, nullable=False)
                  )

class ElectionDB(object):
  def __init__(self, metadata, spec, session):
    self.tables = metadata.tables
    self.spec = spec
    self.levels = spec_levels(self.spec)
    self.titles = spec_titles(self.spec)
    self.parties = {}
    self.session = session
  def fill_geo(self, election, i=0, parent_container_id=None):
    level = self.levels[i]
    title = self.titles[i]
    if election.has_key(title):
      for name, contents in election[title].items():
        values = {'container_id': parent_container_id,
                  'name': name, 'type': level}
        r1 = self.session.execute(sql.insert(self.tables['geo'], values))
        my_id = r1.inserted_primary_key[0]
        if contents.has_key('candidates'):
          for position, candidates in contents['candidates'].items():
            for candidate in candidates:
              values = {'name': candidate['name'],
                        'party_id': self.parties[candidate['party']],
                        'position': position,
                        'container_id': my_id}
              self.session.execute(sql.insert(self.tables['candidates'],
                                              values))
        if contents.has_key('voters'):
          values={'voting_center_id': my_id,
                  'total_votes': contents['voters']
                 }
          self.session.execute(sql.insert(self.tables['votes_check'], values))
        if i < len(self.levels) -1:
          self.fill_geo(contents, i+1, my_id)
  def fill_parties(self):
    for party in self.spec['parties']:
      r1 = self.session.execute(sql.insert(self.tables['parties'],
                                          values=dict(name=party)))
      self.parties[party] = r1.inserted_primary_key[0]
  def load(self, election):
    self.fill_parties()
    self.fill_geo(election)

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
  election =  yaml.load(args.election)

  logging.info('connecting to db')
  engine = sa.create_engine(args.db, echo=args.debug)
  metadata = sa.MetaData(bind=engine)
  session_maker = orm.sessionmaker(bind=engine)
  session = session_maker()

  logging.info('building table structure')
  create_n_tables(election_spec, election, metadata)

  logging.info('cleaning previous tables')
  metadata.drop_all()

  logging.info('creating new tables')
  metadata.create_all()

  logging.info('loading election into the db')
  election_db = ElectionDB(metadata, election_spec, session)
  election_db.load(election)
  logging.info('finished')

