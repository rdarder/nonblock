import sqlalchemy as sa
from sqlalchemy import sql, orm
import logging

metadata = sa.MetaData()
session_maker = orm.sessionmaker()
session = session_maker()

def spec_levels(spec):
  return [l['nivel'] for l in spec['niveles']]

def declare_tables(spec, metadata):
  levels = spec_levels(spec)
  positions = spec['puestos']
  sa.Table('geo', metadata,
           sa.Column('id', sa.Integer, primary_key=True),
           sa.Column('nombre', sa.String(64), nullable=False),
           sa.Column('tipo', sa.Enum(*levels), nullable=False),
           sa.Column('contenedor_id', sa.Integer,
                     sa.ForeignKey('geo.id'))
          )
  sa.Table('partidos', metadata,
           sa.Column('id', sa.Integer, primary_key=True),
           sa.Column('nombre', sa.String(64), nullable=False),
          )
  sa.Table('candidatos', metadata,
           sa.Column('id', sa.Integer, primary_key=True),
           sa.Column('nombre', sa.String(64), nullable=False),
           sa.Column('partido_id', sa.Integer, sa.ForeignKey('partidos.id')),
           sa.Column('puesto', sa.Enum(*positions), nullable=False),
           sa.Column('contenedor_id', sa.Integer,
                     sa.ForeignKey('geo.id'))
          )
  votes_check = sa.Table('votos_check', metadata,
                         sa.Column('votantes', sa.Integer),
                        )
  votes = sa.Table('votos', metadata,
                   sa.Column(levels[-1] + '_id',
                             sa.Integer, sa.ForeignKey('geo.id'),
                             primary_key=True),
                   sa.Column('candidato_id', sa.Integer,
                             sa.ForeignKey('candidatos.id'),
                             primary_key=True),
                   sa.Column('votos', sa.Integer, nullable=False)
                  )
  x_votes = sa.Table('votos_expandidos', metadata,
                     sa.Column('partido_id', sa.Integer,
                               sa.ForeignKey('partidos.id'),
                               primary_key=True),
                     sa.Column('candidato', sa.String(64)),
                     sa.Column('candidato_id', sa.Integer,
                               sa.ForeignKey('candidatos.id'),
                               primary_key=True),
                     sa.Column('puesto', sa.Enum(*positions),
                               nullable=False),
                     sa.Column('partido', sa.String(64)),
                     sa.Column('votos', sa.Integer, default=0)
                    )

  for level in levels:
    x_votes.append_column(sa.Column(level + '_id', sa.Integer,
                                    sa.ForeignKey('geo.id'),
                                    primary_key=True))
    votes_check.append_column(sa.Column(level + '_id', sa.Integer,
                                        sa.ForeignKey('geo.id'),
                                        primary_key=True))
    x_votes.append_column(sa.Column(level , sa.String(64)))
    votes_check.append_column(sa.Column(level , sa.String(64)))

def setup_mapper(spec, tables, classes):
  pass

def setup(election_spec, url, clean_db=False, echo=False):
  global spec, engine, metadata, session_maker, session

  spec = election_spec

  logging.info('connecting to db')
  engine = sa.create_engine(url,echo=echo)
  metadata.bind = engine
  session_maker.bind = engine
  session = session_maker()

  logging.info('building table structure')
  declare_tables(spec, metadata)
  if clean_db:
    logging.info('cleaning previous tables')
    metadata.drop_all()
    logging.info('creating new tables')
    metadata.create_all()



def configure_mapper(spec, tables, classes):
  pass



