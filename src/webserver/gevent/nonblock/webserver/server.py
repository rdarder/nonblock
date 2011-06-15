from pkg_resources import resource_filename
import json
from paste.fileapp import DirectoryApp
import os
import argparse
import logging
from gevent.pywsgi import WSGIServer, WSGIHandler
import yaml
from gevent.server import StreamServer
from gevent.event import Event
import gevent
import msgpack
from websocket.server import WebSocketHandler

from nonblock import db


class NotificationRegistry(object):
  pass

class HttpElectionServer(WSGIHandler):
  static_file_handler = DirectoryApp(os.path.dirname(
    resource_filename('nonblock.webserver', 'static')))
  def __init__(self, socket_server):
    self.socket_server = socket_server
  def __call__(self, environ, start_response):
    if environ["PATH_INFO"] == '/votes':
      start_response('200 OK', [('Content-Type', 'Text/Plain')])
      return []
    elif environ["PATH_INFO"] == '/ws':
      get_websocket = environ.get('wsgi.get_websocket')
      if get_websocket is not None:
        socket = get_websocket()
        socket.do_handshake()
        return self.socket_server(socket, environ.get('REMOTE_ADDR',
                                                      'UNKNOWN'))
    elif environ["PATH_INFO"].startswith('/static'):
      return self.static_file_handler(environ, start_response)
    else:
      start_response('403 Forbidden', [('Content-Type', 'Text/Plain')])
      return ["Forbidden"]

class ServerBuilder(object):
  def __init__(self, parser_factory, registry):
    self.parser_factory= parser_factory
    self.registry = registry
  def __call__(self, socket, address):
    print 'calling socket server'
    return ElectionServer(self.parser_factory(socket), self.registry).main()

class ElectionServer(object):
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

class YamlStreamParser(object):
  def __init__(self, sock):
    self.stream = sock.makefile()
  def receive(self):
    buf = []
    while True:
      line = self.stream.readline()
      if line.startswith('---'):
        return yaml.loads(''.join(buf))
      else:
        buf.append(line)
  def send(self, msg):
    self.stream.write(yaml.dumps(msg) + '\n---\n')

class YamlPacketParser(object):
  def __init__(self, pkt):
    self.pkt = pkt
  def receive(self):
    return yaml.loads(self.pkt.receive())
  def send(self, msg):
    self.pkt.send(yaml.dumps(self, msg))


class JsonStreamParser(object):
  def __init__(self, sock):
    self.stream = sock.makefile()
  def receive(self):
    return json.load(self.stream.readline())
  def send(self, msg):
    packet = json.dumps(msg) + '\n'
    self.stream.write(packet + '\n')

class JsonPacketParser(object):
  def __init__(self, pkt):
    self.pkt = pkt
  def receive(self):
    return json.loads(self.pkt.receive())
  def send(self, msg):
    self.pkt.send(json.dumps(self, msg))


class MsgPacketParser(object):
  def __init__(self, pkt):
    self.pkt = pkt
  def receive(self):
    return msgpack.loads(self.pkt.receive())
  def send(self, msg):
    self.pkt.send(msgpack.dumps(msg))

class MsgStreamParser(object):
  def __init__(self, stream):
    self.stream = stream
  def receive(self):
    return msgpack.load(self.stream)
  def send(self, msg):
    msgpack.dump(msg, self.stream)



stream_parsers = {'json': JsonStreamParser,
                  'yaml': YamlStreamParser,
                  'msgpack': MsgStreamParser,
                 }
packet_parsers = {'json': JsonPacketParser,
                  'yaml': YamlPacketParser,
                  'msgpack': MsgPacketParser,
                 }

def argument_parser():
  parser = argparse.ArgumentParser(description='Election results Server')
  parser.add_argument('-w', '--http',
                      help="address:port where http server will bind to")
  parser.add_argument('-t', '--tcp',
                      help="address:port  where tcp server will bind to")
  parser.add_argument('-f', '--format', help="packet format",
                      choices=packet_parsers, default='json')
  parser.add_argument('-d', '--db',  default="sqlite:///db.sqlite",
                      help="url format described in "
                      "http://www.sqlalchemy.org/docs/core/engines.html",
                      required=True)
  parser.add_argument('-s', '--election-spec', type=argparse.FileType('r'),
                      required=True)
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

def setup_db(args):
  election_spec = yaml.load(args.election_spec)
  db.setup(election_spec, args.db, True, args.debug)

def main():
  args = argument_parser().parse_args()
  setup_logging(args)

  if args.http is None and args.tcp is None:
    logging.fatal('At least one of --http or --tcp options is required')
    return(-1)
  registry = NotificationRegistry()

  if args.http:
    socket_server = ServerBuilder(packet_parsers[args.format], registry)
    address, port = args.http.split(':',1)
    http_server = HttpElectionServer(socket_server)
    http_listener =  WSGIServer((address, int(port)), http_server,
                               handler_class=WebSocketHandler)
    http_listener.start()
  if args.tcp:
    socket_server = ServerBuilder(stream_parsers[args.format], registry)
    address, port = args.tcp.split(':',1)
    tcp_server = StreamServer((address, int(port)), socket_server)
    tcp_server.start()

    stop_event = Event()
    stop_event.wait()




if __name__ == '__main__':
  main()
