/*
 * autor: Mauro Monti
 * correo: mauro.monti@globant.com
 */

/*
 * String format function. 
 */
exports.format = String.prototype.format = function() {
    var string = this;
	var idx = arguments.length;

    while (idx--) {
        string = string.replace(new RegExp('\\{' + idx + '\\}', 'gm'), arguments[idx]);
    }
    return string;
};