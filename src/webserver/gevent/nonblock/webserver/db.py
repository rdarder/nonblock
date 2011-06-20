import gevent.queue
import amysql
from contextlib import contextmanager

class GeoLookup(object):
  geo_fields = """mesa mesa_id local_id local seccional_id seccional
              localidad_id localidad departamento_id departamento
              provincia_id provincia""".split()
  geo_field_indexes = dict((v,i) for i,v in enumerate(geo_fields))
  def __init__(self, dbpool):
    self.dbpool = dbpool
    with dbpool.connection as conn:
      self._init_geo(conn)
      self._init_parties(conn)
      self._init_candidates(conn)
      self._init_geoid(conn)
  def _init_geoid(self, conn):
    self.geo_id = dict.fromkeys("provincia departamento localidad seccional "
                                "local mesa".split(), {})
  def id_lookup(self, level, name):
    res = self.geo_id[level].get(name, False)
    if res:
      return res
    with self.dbpool.connection as conn:
      rs = conn.query("select nombre, id from geo "
                      "where tipo = %s and nombre = %s", (level, name))
      print(rs.rows)
      self.geo_id[level].update(rs.rows)
      return rs.rows[0][1]
  def _init_geo(self, conn):
    self.geo = {}
    rs = conn.query("select %s from votos_check" % ', '.join(self.geo_fields))
    for row in rs.rows:
      self.geo[row[0]] = row
  def _init_parties(self, conn):
    rs = conn.query("select nombre, id from partidos")
    self.parties = dict(rs.rows)
  def _init_candidates(self, conn):
    rs = conn.query("select nombre, id from candidatos")
    self.candidates = dict(rs.rows)


class DbPool(object):
  def __init__(self, conf):
    self.size = conf.db_pool_size
    self.q = gevent.queue.Queue()
    for i in range(self.size):
      conn = amysql.Connection()
      conn.connect(conf.db_host, conf.db_port, conf.db_user, conf.db_pass,
                   conf.db_name)
      self.q.put(conn)
  @property
  @contextmanager
  def connection(self):
    conn = self.q.get()
    yield conn
    self.q.put(conn)


class VotesSqlHelper(object):
  query = "select %s from votos_expandidos where %s group by %s order by %s"
  insert = "replace into votos_expandidos (%s) values %s"
  class fields:
    levels = 'provincia departamento localidad seccional local mesa'.split()
    id_levels = ('provincia_id departamento_id localidad_id seccional_id '
               'local_id mesa_id'.split())
    level_positions = dict((k,i) for i,k in enumerate(levels))
    id_choices = "partido_id candidato_id".split()
    choices = "partido candidato".split()
    votes = ["sum(votos)"]
    insert_fields = (GeoLookup.geo_fields + ("candidato_id candidato "
                                            "partido_id partido puesto "
                                            "votos".split()))

  def __init__(self, lookup):
    self.lookup = lookup

  def asc(self, fields):
    return [ item + " ASC" for item in fields]
  def desc(self, fields):
    return [ item + " DESC" for item in fields]
  def sqltype(self, value):
    if isinstance(value, (str, unicode)):
      return "'" + value.replace("'",'"') + "'"
    else:
      return str(value)
  def insert_vote(self, voting_center, votes):
    base_fields = self.lookup.geo.get(voting_center)
    to_insert = []
    for vote in votes:
      candidate = vote['candidato']
      candidate_id = self.lookup.candidates[candidate]
      party = vote['partido']
      party_id = self.lookup.parties[party]
      position = vote['puesto']
      votes = vote['cant']
      values = map(self.sqltype,
                   base_fields +(candidate_id, candidate, party_id, party,
                                 position, votes))
      to_insert.append("(" + ", ".join(values) + ")")
    query = self.insert % (", ".join(self.fields.insert_fields),
                           ", ".join(to_insert))
    return (query, self.fields.insert_fields)


  def aggregate_votes(self, scope, place, level, position):

    geo_pos = self.fields.level_positions[level]
    id_levels = self.fields.id_levels[:geo_pos]
    levels = self.fields.levels[:geo_pos]

    group_fields = (id_levels + self.fields.id_choices + levels +
                    self.fields.choices)
    select_fields = (id_levels + self.fields.id_choices + levels +
                     self.fields.choices + self.fields.votes)

    order_clause = self.asc(self.fields.levels) + self.desc(self.fields.votes)

    geo_id = self.lookup.id_lookup(scope, place)
    where_clause  = ("%s_id = %d and puesto = '%s'" %
                     (scope, geo_id, position))
    query = self.query  % (", ".join(select_fields), where_clause,
                           ", ".join(group_fields), ", ".join(order_clause))

    print(query)
    return (query, select_fields)


class VotesStore(object):
  def __init__(self, dbpool):
    lookup = GeoLookup(dbpool)
    self.dbpool = dbpool
    self.sql_helper = VotesSqlHelper(lookup)
  def store_votes(self, voting_center, votes):
    sql_insert, fields = self.sql_helper.insert_vote(voting_center, votes)
    with self.dbpool.connection as conn:
      conn.query(sql_insert)
  def get_results(self, scope, place, level, position):
    sql_query, fields = \
        self.sql_helper.aggregate_votes(scope, place, level, position)
    with self.dbpool.connection as conn:
      for row in conn.query(sql_query).rows:
        yield dict(zip(fields, row))

