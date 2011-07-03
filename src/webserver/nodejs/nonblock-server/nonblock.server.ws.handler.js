/*
 * autor: Mauro Monti
 * correo: mauro.monti@globant.com
 */
var	socketio = require('socket.io');

/*
 * Websocket Instance. 
 */
var websocket_instance = null;

/*
 * Websocket Prototype.
 */
var websocket_prototype = {
	
	// = Server reference.
	server_ref: null,
	socket_ref: null,
	
	// = Message Handler.
	messageHandler: null,
	
	/*
	 * Handler for events.
	 */
	setHandler: function(pMessageHandlerProvider) {
		this.messageHandler = pMessageHandlerProvider;

		// = Hold the @this reference.
		var self = this;
		
		// = Add Listeners to Events - Connection / Message.
		self.socket_ref.on('connection', function(pClient){
			self.messageHandler.onClientConnect(pClient, self.server_ref);
			
			pClient.on('message', function(pMessage){
				
				/*
				 *	Subscribe message.
				 *  
				message = {
					name : 'subscribe',
					data : {
						puesto : 'Intendente',
						alcance : 'localidad',
						nivel : 'mesa',
						valor_alc : 'Rosario',
					}
				};
				*/
				if (pMessage.name == 'subscribe') {
					self.messageHandler.onClientSubscribe(pClient, pMessage, self.server_ref);
				}

				/*
				 *	Cancel message.
				 *  
				message = {
					name : 'cancel',
					data : {
						puesto : 'Intendente',
						alcance : 'localidad',
						nivel : 'mesa',
						valor_alc : 'Rosario',
					}
				};
				*/				
				if (pMessage.name == 'cancel') {
					self.messageHandler.onClientCancel(pClient, pMessage, self.server_ref);
				}
			});
		});
		
		// = Add Listeners to Events - Disconnect.
		self.socket_ref.on('disconnect', function(pClient) { 
			self.messageHandler.onClientDisconnect(pClient, self.server_ref);
		});
	}
};

/*
 * Return a WebSocketHandler Instance.
 */
exports.createWebsocket = function(pServer) {
	var socketio_instance = socketio.listen(pServer);
	
	websocket_instance = Object.create(websocket_prototype);
	websocket_instance.server_ref = pServer;
	websocket_instance.socket_ref = socketio_instance;
	
	return websocket_instance;
};