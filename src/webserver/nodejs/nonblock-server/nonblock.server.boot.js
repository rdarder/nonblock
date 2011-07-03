/*
 * autor: Mauro Monti
 * correo: mauro.monti@globant.com
 */
var nonblock = require('./nonblock.server.js');
var configuration = require('./nonblock.server.config.js');
var mhandler = require('./nonblock.server.msg.handler.js');

// = Server Instance.
var server = nonblock.createNonBlockServer(configuration);

// = Set MessageHandler.
server.setMessageHandler(mhandler);

// = Start the Server.
server.start(function(pServer) {
	console.log("NonBlock Voting Server is Up and Running: %s", pServer.address());
});

