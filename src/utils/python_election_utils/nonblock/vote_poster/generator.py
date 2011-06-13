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
  return [l['level'] for l in spec['levels']]

def spec_titles(spec):
  return [l['title'] for l in spec['levels']]

class VotesGenerator(object):
  def __init__(self, election_spec):
    self.spec = election_spec
    self.levels = spec_levels(self.spec)
    self.titles = spec_titles(self.spec)
    self.parties = {}
    for party in self.spec['parties']:
      self.parties[party['name']] = party
  def generate(self, election, i=0, parent_candidates={}):
    level = self.levels[i]
    title = self.titles[i]

    candidates = {}
    candidates.update(parent_candidates)

    if election.has_key(title):
      for name, contents in election[title].items():
        if contents.has_key('candidates'):
          for position, level_candidates in contents['candidates'].items():
              candidates[position] = level_candidates
        if contents.has_key('voters'):
          total_votes = contents['voters']
          voting_center_name = name
          vote_entries = []
          for position, candidate_list in candidates.items():
            random_share = []
            for candidate in candidate_list:
              party_spec = self.parties[candidate['party']]
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
              vote_entries.append({ 'position': position, 'candidate': c,
                                 'votes': votes })
          yield {'voting_center': voting_center_name, 'data':vote_entries}
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
  poster = ConstantRateVotePoster(votes, args.url, args.rate)
  poster.start()
  logging.info('finished')

from datetime import datetime, timedelta

class ConstantRateVotePoster(object):
  def __init__(self, votes, url, rate):
    self.votes = votes
    self.url =  url
    self.rate = rate
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
      self.put_vote(vote)
      self.posted += 1
    gevent.kill(mon)
  def rate_monitor(self):
    while True:
      rate = self.posted / (datetime.now() - self.start_time).total_seconds()
      logging.info("Posting at real rate of %0.2f votes per second", rate)
      gevent.sleep(1)
  def put_vote(self, vote):
    data = json.dumps(vote)
    req = urllib2.Request(self.url, data, {'Content-Type':'application/json'})
    f = urllib2.urlopen(req)
    response = f.read()
    f.close()








