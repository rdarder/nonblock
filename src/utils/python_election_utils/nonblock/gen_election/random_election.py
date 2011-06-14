import logging
import yaml
import random
import argparse

def seq_name(level, index):
  return "%s_%i" % (level, index)

def empty_level(level, index, name, slot):
  return slot

class RandomCandidates(object):
  def __init__(self, election_spec, name_factory):
    self.parties = [p['nombre'] for p in election_spec['partidos']]
    self.positions = {}
    for level in election_spec['niveles']:
      self.positions[level['nivel']] = level.get('puestos',[])
    self.votes_label = election_spec['niveles'][-1]['nivel']
    self.votes_random = (election_spec['niveles'][-1]['votantes']['avg'],
                        election_spec['niveles'][-1]['votantes']['mu'])
    self.name_factory = name_factory
  def __call__(self, level, index, name, base):
    election = {}
    for position in self.positions.get(level,[]):
      candidates = []
      for party in self.parties:
        candidates.append({'partido': party, 'nombre': self.name_factory()})
      election[position] = candidates
    if election:
      base['candidatos'] = election
    if level == self.votes_label:
      base['votantes'] = max(1,int(random.normalvariate(*self.votes_random)))
    return base

def gen_election(spec, name_factory, item_factory):
  geo = {}
  bases = [geo]
  levels = spec['niveles']
  autoincrement = 0
  for i, level in enumerate(levels, 1):
    next_bases = []
    logging.debug('generating level %s:%s', i, level)
    for base in bases:
      to_gen = max(1, int(random.normalvariate(level['avg'],level['mu'])))
      insert_into = {}
      base[level['plural']] = insert_into
      for r in range(to_gen):
        autoincrement += 1
        lname = name_factory(level['nivel'], r, autoincrement)
        entry = item_factory(level['nivel'], r, lname, {})
        insert_into[lname] = entry
        next_bases.append(entry)
    bases = next_bases
  return geo


class RandomPeopleNames(object):
  def __init__(self, names, surnames):
    self.names = names
    self.surnames = surnames
  def __call__(self):
    return "%s, %s" % (random.choice(self.surnames),
                       random.choice(self.names))

class RandomPlaceNames(object):
  def __init__(self, place_names, election_spec):
    self.place_names = place_names[:]
    self.name_gen = {}
    for level in election_spec['niveles']:
      self.name_gen[level['nivel']] = level
    random.shuffle(self.place_names)
  def __call__(self, level, local_index, global_index):
    if self.name_gen[level]['generador_nombre'] == "lugares":
      return self.place_names.pop()
    elif self.name_gen[level]['generador_nombre'] == "patron":
      return self.name_gen[level]['patron_nombre'].format(
        level=level,
        local_index=local_index,
        global_index=global_index
      )

def election_from_spec(election_spec_file, people_names_file,
                       places_names_file):

  logging.info('loading yaml specs')
  election_spec = yaml.load(election_spec_file)
  logging.info('loading people names')
  people_names_dict = yaml.load(people_names_file)
  logging.info('loading geo names')
  places_names_dict = yaml.load(places_names_file)
  logging.info('done loading')

  random_places = RandomPlaceNames(places_names_dict['lugares'],
                                   election_spec)
  random_names = RandomPeopleNames(people_names_dict['nombres'],
                                   people_names_dict['apellidos'])
  random_candidates = RandomCandidates(election_spec, random_names)

  logging.info('generating election')
  return gen_election(election_spec, random_places, random_candidates)

def main():
  parser = argparse.ArgumentParser(description='create election database')
  parser.add_argument('-s', '--election-spec', type=argparse.FileType('r'),
                      required=True)
  parser.add_argument('-p', '--people-names', type=argparse.FileType('r'),
                      required=True, help='people names yml source')
  parser.add_argument('-g', '--geo-names', type=argparse.FileType('r'),
                      required=True, help='places names yml source')
  parser.add_argument('-o', '--output', type=argparse.FileType('w'),
                      required=True, help='output election yml file')
  parser.add_argument('-v', '--verbose', action='store_true', default=False)

  args = parser.parse_args()
  if args.verbose:
    logging.basicConfig(level=logging.INFO)

  election = election_from_spec(args.election_spec, args.people_names,
                                args.geo_names)
  logging.info('saving output file')
  yaml.dump(election, args.output, canonical=False)
  logging.info('finished')
