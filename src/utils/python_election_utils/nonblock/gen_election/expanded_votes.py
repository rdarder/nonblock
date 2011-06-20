import sqlalchemy as sa
from sqlalchemy import sql, orm
from sqlalchemy.ext.compiler import compiles


from sqlalchemy.sql.expression import Executable, ClauseElement

class InsertFromSelect(Executable, ClauseElement):
    def __init__(self, table, field_names, select):
        self.table = table
        self.field_names = field_names
        self.select = select

@compiles(InsertFromSelect)
def visit_insert_from_select(element, compiler, **kw):
    return "INSERT INTO %s (%s) %s" % (
        compiler.process(element.table, asfrom=True),
        ', '.join(element.field_names),
        compiler.process(element.select)
    )


class Tables(object):
  def __init__(self, spec, db):
    for nombre in ['candidatos', 'geo', 'votos', 'votos_check', 'partidos',
                 'votos_expandidos']:
      setattr(self, nombre, db.metadata.tables[nombre])
    for nombre in spec['niveles']:
      setattr(self, nombre['nivel'], sql.alias(db.metadata.tables['geo'],
                                               nombre['nivel']))


def get_expanded(tables, spec):
  join = tables.mesa
  join = join.join(tables.local,
                   tables.mesa.c.contenedor_id == tables.local.c.id)
  join = join.join(tables.seccional,
                   tables.local.c.contenedor_id == tables.seccional.c.id)
  join = join.join(tables.localidad,
                   tables.seccional.c.contenedor_id ==
                   tables.localidad.c.id)
  join = join.join(tables.departamento,
                   tables.localidad.c.contenedor_id ==
                   tables.departamento.c.id)
  join = join.join(tables.provincia,
                   tables.departamento.c.contenedor_id ==
                   tables.provincia.c.id)

  #fields to select
  fields = [tables.mesa.c.id, tables.mesa.c.nombre,
            tables.local.c.id, tables.local.c.nombre,
            tables.seccional.c.id, tables.seccional.c.nombre,
            tables.localidad.c.id, tables.localidad.c.nombre,
            tables.departamento.c.id, tables.departamento.c.nombre,
            tables.provincia.c.id, tables.provincia.c.nombre,
           ]
  field_names = """mesa_id mesa local_id local seccional_id seccional
                   localidad_id localidad departamento_id departamento
                   provincia_id provincia""".split()

  query = sql.select(fields, from_obj=join, use_labels=True)
  return InsertFromSelect(tables.votos_check, field_names, query)

