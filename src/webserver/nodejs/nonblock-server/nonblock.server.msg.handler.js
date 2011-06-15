/*
 * autor: Mauro Monti
 * correo: mauro.monti@globant.com
 */

// = Exports clientConnect Handler. 
exports.onClientConnect = onClientConnect = function(pClient, pServer) {
	console.info('Client arrived [sessionId={0}]'.format(pClient.sessionId));
}

// = Exports clientDisconnect Handler. 
exports.onClientDisconnect = onClientDisconnect = function(pClient, pServer) {
	console.info('Client disconnect [sessionId={0}]'.format(pClient.sessionId));
}

// = Exports clientSubscribe Handler.
exports.onClientSubscribe = onClientSubscribe = function(pClient, pMessage, pServer) {
	console.info('Client Subscribed [sessionId={0}]'.format(pClient.sessionId));
}

// = Exports clientCancel Handler. 
exports.onClientCancel = onClientCancel = function(pClient, pMessage, pServer) {
	console.info('Client Canceled Subscription [sessionId={0}]'.format(pClient.sessionId));
}