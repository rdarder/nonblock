/*
 * autor: Mauro Monti
 * correo: mauro.monti@globant.com
 */
var express = require('express');
var utils = require('./nonblock.server.utils.js');

/*
 * Server Constants. 
 */
exports.server = server = {
	port: 3000,
	host: '127.0.0.1',
	protocol: 'http',
	staticContentFolder: 'webroot'
};

/*
 * Database Constants. 
 */
exports.database = database = {
	folder: './database/',
	name: 'votos.sqlite'
};

/*
 * Express Constants. 
 */
exports.express = express = {
	post: {
		//poster: '^\/poster(\/)*$'
		poster: '/poster/'
	},
	get: {
		client: '/client/'
	},
	logger: express.logger(),
	bodyParser: express.bodyParser(),
	methodOverride: express.methodOverride(),
	errorHandler: express.errorHandler({ showStack: true, dumpExceptions: true })
};