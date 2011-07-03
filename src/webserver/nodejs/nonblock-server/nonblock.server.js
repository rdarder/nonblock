/*
 * autor: Mauro Monti
 * correo: mauro.monti@globant.com
 */
var express = require('express');
var util = require('underscore'); util.mixin(require('underscore.string'));
var events = require('events');
var srv_utils = require('./nonblock.server.utils.js');
var srv_wshandler = require('./nonblock.server.ws.handler.js');
var srv_data = require('./nonblock.server.data.js');

/*
 * Default Poster Handler.
 */
var defaultPosterHandler = function(pRequest, pResponse) {
	console.info("Default Poster Handler.");
	pResponse.send(JSON.stringify({response: 'Ok'}));
}

/*
 * Default Message Handler.
 */
var defaultMessageHandler = function() {
	return {
		onClientConnect: function() {},
		onClientDisconnect: function() {},
		onClientSubscribe: function() {},
		onClientCancel: function() {},
	}
}

/*
 * Hold the Server instance.
 */
var nonblockserver_instance = null;

/*
 * Server prototype.
 */
var nonblockserver_prototype = {
	
	// = Configuration Object.
	configuration: null,
	
	// = 
	server_handler: null,
	static_handler: null,
	socket_handler: null,

	// = Function that handles the vote posts. 
	poster_handler: null,

	// = Function that handles the client messages. 
	message_handler: null,
	
	// = Data structure that hold the tree.
	data_structure: null,

	// = Event Emitter.
	publisher: null,

	/*
	 * Initialize the server with default values.
	 */
	initialize : function(pEndCallback) {
		// = Initialize Server. 
		this.server_handler = express.createServer();

		// = Initialize the WebSocketHandler.
		this.socket_handler = srv_wshandler.createWebsocket(this.server_handler);
		this.socket_handler.setHandler(this.message_handler || defaultMessageHandler());
		
		// = Set the Static Content Folder.
		this.static_handler = express.static(__dirname + '/' + this.configuration.server.staticContentFolder);
		this.server_handler.use(this.static_handler);

		// =
		this.server_handler.use(this.configuration.express.logger);
		this.server_handler.use(this.configuration.express.bodyParser);
		this.server_handler.use(this.configuration.express.methodOverride);
		this.server_handler.use(this.configuration.express.errorHandler);
		
		// = Set Handler for Posts 
		this.server_handler.post(this.configuration.express.post.poster, this.poster_handler || defaultPosterHandler);

		// = 
		var self = this;
		
		// =
		this.data_structure = [];

		// =
		var init_data_structure = function(pCallback) {
			srv_data.initialize(self.configuration, pCallback);	
		};

		// =
		init_data_structure(function(pStructure) {
			self.data_structure = pStructure;
			
			pEndCallback(true);
		});
	},	
	
	/*
	 * Start listening.
	 */
	start: function(pCallback) {
		console.info("Starting NonBlock Voting Server...");
		
		var self = this;

		// = 
		this.initialize(function(pInitialized){
			
			if (!pInitialized) {
				return false;		
			}

			self.server_handler.listen(self.configuration.server.port);
		
			var callback = (pCallback || function() {});
			return callback(self);
		});
	},
	
	/*
	 * Event Listener Shortcut.
	 */
	on: function(pEvent, pCallback) {
		this.publisher.on(pEvent, pCallback);
	},
	
	/*
	 * Message Handler. 
	 */
	setMessageHandler: function(pMessageHandler) {
		this.message_handler = pMessageHandler;
	},
	
	/*
	 * Poster Handler.
	 */
	setPosterHandler: function(pPosterHandler) {
		this.server_handler.poster_handler = pPosterHandler;
	},
	
	/*
	 * Set the folder for static contents delivery.
	 */
	useStaticContent: function(pFolder) {
		this.server_handler.use(express.static(__dirname + pFolder));
	},
	
	/*
	 * Return the address of the server.
	 */
	address: function() {
		return this.configuration.server.protocol + '://' + this.configuration.server.host + ':' + this.configuration.server.port;
	}

}

/*
 * Create a new instance of the server.
 */
exports.createNonBlockServer = function(pConfiguration) {
	nonblockserver_instance = Object.create(nonblockserver_prototype);
	nonblockserver_instance.configuration  = pConfiguration;
	nonblockserver_instance.publisher = new events.EventEmitter;
	
	return nonblockserver_instance;
}