import sqlalchemy as sa

def spec_levels(spec):
  return [l['level'] for l in spec['levels']]

def declare_tables(spec, metadata):
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

