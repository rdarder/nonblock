import sqlalchemy as sa
from sqlalchemy import sql, orm
import logging
import argparse
import random
import yaml

from .. import db

def shuffle_vote_centers(session, metadata, spec):
  vote_level = spec['levels'][-1]['level']
  vc_table = metadata.tables['votes_check']
  geo_table = metadata.tables['geo']
  q = sql.select([geo_table.c.name, vc_table.c.total_votes],
                 geo_table.c.id == vc_table.c.voting_center_id)
  q = q.where(geo_table.c.type == vote_level)
  vote_places = session.execute(q).fetchall()
  random.shuffle(vote_places)
  return vote_places

def main():
  parser = argparse.ArgumentParser(description='create election database')
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

  logging.info('connecting to db')
  engine = sa.create_engine(args.db, echo=args.debug)
  metadata = sa.MetaData(bind=engine)
  session_maker = orm.sessionmaker(bind=engine)
  session = session_maker()
  print(shuffle_vote_centers(session, metadata, election_spec))

