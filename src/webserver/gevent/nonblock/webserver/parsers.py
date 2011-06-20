import yaml
import msgpack
import json


class YamlStreamParser(object):
  def __init__(self, stream):
    self.stream = stream
    self.loader = yaml.load_all(self.stream)
  def receive(self):
    return next(self.loader)
  def send(self, msg):
    self.stream.write(yaml.dump(msg) + '\n---\n')

class YamlPacketParser(object):
  def __init__(self, channel):
    self.channel= channel
  def receive(self):
    return yaml.load(self.channel.receive())
  def send(self, msg):
    self.channel.send(yaml.dump(msg))


class JsonStreamParser(object):
  def __init__(self, stream):
    self.stream = stream
  def receive(self):
    return json.loads(self.stream.readline())
  def send(self, msg):
    self.stream.write(json.dumps(msg) + '\n')

class JsonPacketParser(object):
  def __init__(self, channel):
    self.channel = channel
  def receive(self):
    return json.loads(self.channel.receive())
  def send(self, msg):
    self.channel.send(json.dumps(msg))

class MsgPacketParser(object):
  def __init__(self, channel):
    self.channel = channel
  def receive(self):
    return msgpack.loads(self.channel.receive())
  def send(self, msg):
    self.channel.send(msgpack.dumps(msg))

class MsgStreamParser(object):
  def __init__(self, stream):
    self.stream = stream
    self.loader = iter(msgpack.Unpacker(stream))
  def receive(self):
    return next(self.loader)
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

