import os, json, argparse, logging
from gevent.pywsgi import WSGIServer
from gevent.server import StreamServer
from gevent.event import Event
from websocket.server import WebSocketHandler
from paste.fileapp import DirectoryApp
from pkg_resources import resource_filename
import urlparse

from . import parsers, db

class HttpElectionServer(WebSocketHandler):
  static_file_handler = DirectoryApp(os.path.dirname(
    resource_filename('nonblock.webserver', 'static')))
  def __init__(self, socket_server, votes_store):
    self.socket_server = socket_server
    self.store =  votes_store
  def __call__(self, environ, start_response):
    if (environ["PATH_INFO"] == '/votes' and
        environ["REQUEST_METHOD"] == 'POST'):
      return self.load_votes(environ, start_response)
    elif environ["PATH_INFO"] == '/results':
      return self.get_results(environ, start_response)
    elif environ["PATH_INFO"] == '/ws':
      return self.handle_websocket(environ, start_response)
    elif environ["PATH_INFO"].startswith('/static'):
      return self.static_file_handler(environ, start_response)
    else:
      return self.forbidden(environ, start_response)

  def forbidden(self, environ, start_response, msg='Forbidden'):
    start_response('403 Forbidden', [('Content-Type', 'Text/Plain')])
    return [msg]
  def bad_request(self, environ, start_response, msg='Bad Request'):
    start_response('400 Bad Request', [('Content-Type', 'Text/Plain')])
    return [msg]
  def success(self, environ, start_response, msg="OK"):
    start_response('200 OK', [('Content-Type', 'Text/Plain')])
    return [msg]
  def json_ok(self, environ, start_response, data=None):
    start_response('200 OK', [('Content-Type', 'application/json')])
    return json.dumps(data)

  def handle_websocket(self, environ, start_response):
    get_websocket = environ.get('wsgi.get_websocket')
    if get_websocket is not None:
      socket = get_websocket()
      socket.do_handshake()
      return self.socket_server(socket,
                                environ.get('REMOTE_ADDR', 'UNKNOWN'))
  def load_votes(self, environ, start_response):
    votes = json.load(environ['wsgi.input'])
    voting_center = votes['data']['mesa']
    votes = votes['data']['votos']
    self.store.store_votes(voting_center, votes)
    return self.success(environ, start_response)
  def get_results(self, environ, start_response):
    required = ['scope', 'place', 'level', 'position']
    params = dict(urlparse.parse_qsl(environ['QUERY_STRING']))
    for field in required:
      if field not in params:
          return self.bad_request(environ, start_response,
                                  'missing %s field' % field)
    results = self.store.get_results(*[params[f] for f in required])
    return self.json_ok(environ, start_response, list(results))

class ServerBuilder(object):
  def __init__(self, parser_factory, registry):
    self.parser_factory= parser_factory
    self.registry = registry
  def __call__(self, socket, address):
    return ElectionNotificationServer(self.parser_factory(socket),
                                      self.registry).main()

class ElectionNotificationServer(object):
  message_names = set(['subscribe', 'cancel'])
  def __init__(self, client, registry):
    self.client = client
    self.registry = registry
  def main(self):
    while True:
      message = self.client.receive()
      print message
      if message:
        assert(isinstance(message, dict))
        assert('name' in message)
        assert('data' in message)
        if message['name'] in self.message_names:
          getattr(self, message['name'])(message['data'])
      else:
        pass
        #cleanup, close
  def subscribe(self, event):
    print 'subcribe!'
  def cancel(self, event):
    print 'subcribe!'


def argument_parser():
  parser = argparse.ArgumentParser(description='Election results Server')
  parser.add_argument('-w', '--http',
                      help="address:port where http server will bind to")
  parser.add_argument('-t', '--tcp',
                      help="address:port  where tcp server will bind to")
  parser.add_argument('-f', '--format', help="packet format",
                      choices=parsers.packet_parsers, default='json')
  parser.add_argument('--db-host',  default="localhost")
  parser.add_argument('--db-port',  type=int, default=3306)
  parser.add_argument('--db-user', required=True)
  parser.add_argument('--db-pass', required=True)
  parser.add_argument('--db-name', required=True)
  parser.add_argument('--db-pool-size', type=int, default=20)
  parser.add_argument('-v', '--verbose', action='store_true', default=False)
  parser.add_argument('-D', '--debug', action='store_true', default=False)
  return parser

def setup_logging(args):
  if args.debug:
    logging.basicConfig(level=logging.DEBUG)
  elif args.verbose:
    logging.basicConfig(level=logging.INFO)
  else:
    logging.basicConfig(level=logging.WARNING)

def main():
  args = argument_parser().parse_args()
  setup_logging(args)
  store = db.VotesStore(db.DbPool(args))
  registry = NotificationTree()

  if not (args.http or args.tcp):
    logging.fatal('At least one of --http or --tcp options is required')
    return(-1)
  socket_server = ServerBuilder(parsers.packet_parsers[args.format],
                                  registry)
  if args.http:
    address, port = args.http.split(':',1)
    http_server = HttpElectionServer(socket_server, store)
    http_listener =  WSGIServer((address, int(port)), http_server)
    http_listener.start()
  if args.tcp:
    socket_server = ServerBuilder(parsers.stream_parsers[args.format],
                                  registry)
    address, port = args.tcp.split(':',1)
    tcp_server = StreamServer((address, int(port)), socket_server)
    tcp_server.start()

  stop_event = Event()
  stop_event.wait()

class NotificationTree(object):
  pass

class GeoLevel(object):
  def __init__(self, container, geo_id, scope):
    self.container = container
    self.geo_id = geo_id
    self.scope = scope
    self.dirty = False
  def register(self, level, position):
    if self.buckets.has_key((level,position)):
      pass
    else:
      self._create_bucket(self, level, position)


if __name__ == '__main__':
  main()
