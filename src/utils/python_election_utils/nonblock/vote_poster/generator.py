import gevent
from gevent import monkey
monkey.patch_socket()

from itertools import izip
import yaml
import logging
import argparse
import random
import json
import time
import urllib2


def spec_levels(spec):
  return [l['nivel'] for l in spec['niveles']]

def spec_titles(spec):
  return [l['plural'] for l in spec['niveles']]

class VotesGenerator(object):
  def __init__(self, election_spec):
    self.spec = election_spec
    self.levels = spec_levels(self.spec)
    self.titles = spec_titles(self.spec)
    self.parties = {}
    for party in self.spec['partidos']:
      self.parties[party['nombre']] = party
  def generate(self, election, i=0, parent_candidates={}):
    level = self.levels[i]
    title = self.titles[i]

    candidates = {}
    candidates.update(parent_candidates)

    if election.has_key(title):
      for name, contents in election[title].items():
        if contents.has_key('candidatos'):
          for position, level_candidates in contents['candidatos'].items():
              candidates[position] = level_candidates
        if contents.has_key('votantes'):
          total_votes = contents['votantes']
          voting_center_name = name
          vote_entries = []
          for position, candidate_list in candidates.items():
            random_share = []
            for candidate in candidate_list:
              party_spec = self.parties[candidate['partido']]
              random_share.append(random.normalvariate(party_spec['avg'],
                                                       party_spec['mu']))
            total_share = sum(random_share)
            random_percentages = [s/total_share for s in random_share]
            split_votes = total_votes
            for i,(c,p) in enumerate(izip(candidate_list,
                                          random_percentages)):
              if i < len(candidate_list):
                votes = int(split_votes * p)
                split_votes -= votes
              else:
                votes = split_votes
              vote_entries.append({ 'puesto': position, 'candidato': c,
                                 'votos': votes })
          yield {'centro_votacion': voting_center_name,
                 'data':vote_entries}
        if i < len(self.levels) -1:
          for v in self.generate(contents, i+1, candidates):
            yield v

def main():
  parser = argparse.ArgumentParser(description='votes generator')
  parser.add_argument('-e', '--election', type=argparse.FileType('r'),
                      required=True)
  parser.add_argument('-s', '--election-spec', type=argparse.FileType('r'),
                      required=True)
  parser.add_argument('-r', '--rate', type=int, default=10,
                      help="rate in votes per second")
  parser.add_argument('-l', '--limit', type=int, default=0,
                      help="put at most <limit> votes, default all")
  parser.add_argument('-u', '--url',  default="http://localhost",
                      help="url address where votes should be POSTed")
  parser.add_argument('-v', '--verbose', action='store_true', default=False)
  parser.add_argument('-D', '--debug', action='store_true', default=False)
  args = parser.parse_args()

  if args.verbose:
    logging.basicConfig(level=logging.INFO)

  logging.info('loading election spec')
  election_spec = yaml.load(args.election_spec)

  logging.info('loading election')
  election = yaml.load(args.election)

  logging.info('generating votes')
  generator= VotesGenerator(election_spec)
  votes = list(generator.generate(election))

  logging.info('shuffling votes')
  random.shuffle(votes)

  logging.info('starting to post votes @%d vps', args.rate)
  poster = ConstantRateVotePoster(votes, args.url, args.rate, args.limit)
  poster.start()
  logging.info('finished')

from datetime import datetime, timedelta

class ConstantRateVotePoster(object):
  def __init__(self, votes, url, rate, limit=0):
    self.votes = votes
    self.url =  url
    self.rate = rate
    if limit:
      self.votes = votes[:limit]
  def start(self):
    mon = gevent.spawn(self.rate_monitor)
    self.start_time = datetime.now()
    self.posted=0
    for vote in self.votes:
      wait_until = self.start_time + timedelta(seconds=self.posted /
                                               float(self.rate))
      logging.debug('started on %s, waiting until %s', self.start_time,
                    wait_until)
      sleep_time = (wait_until - datetime.now()).total_seconds()
      if sleep_time > 0:
        logging.debug('sleeping for %s seconds', sleep_time)
        gevent.sleep(sleep_time)
        #time.sleep(sleep_time)
      else:
        logging.debug('not sleeping')
      self.posted += 1
      gevent.spawn(self.put_vote,vote)
    gevent.kill(mon)
  def rate_monitor(self):
    while True:
      rate = self.posted / (datetime.now() - self.start_time).total_seconds()
      logging.info("Posted: %d votes. Rate %0.2f votes per second",
                   self.posted, rate)
      gevent.sleep(1)
  def put_vote(self, vote):
    data = json.dumps(vote)
    req = urllib2.Request(self.url, data, {'Content-Type':'application/json'})
    try:
      f = urllib2.urlopen(req)
      response = f.read()
    except Exception, e:
      print(e)
    else:
      print(response)
      f.close()









