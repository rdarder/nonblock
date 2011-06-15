/*
 * autor: Mauro Monti
 * correo: mauro.monti@globant.com
 */

/*
 * 
 */
var event_spec = {
	puesto : null,
	alcance : null,
	valor_alc : null,
	nivel : null
};

/*
 * 
 */
var datum_spec = {
	puesto : null,
	candidato : null,
	partido : null,
	cant : null
};

/*
 * 
 */
var response_event = {
	name : 'newdata',
	data : {
		event : null, 
		data: []
	}
}

/*
 * Poster Handler 
 */
var posterHandler = function(pRequest, pResponse) {
	var message = pRequest.body;		

	/*
	var message = { 
		mesa: 758, 
		data: [{
			puesto : 'intendente',
			candidato : 'Giustiniani',
			partido : 'PSD',
			cant : 7
		}]
	};
	*/

	// = 
	var response_event_spec = Object.create(event_spec);
	response_event_spec.puesto = message.data.puesto;
	response_event_spec.alcance = message.data.candidato;
	response_event_spec.valor_alc = message.data.partido;
	response_event_spec.nivel = message.data.cant;	

	// = 
	var response_datum_spec = Object.create(datum_spec);
	response_datum_spec.puesto = message.data.puesto;
	response_datum_spec.candidato = message.data.candidato;
	response_datum_spec.partido = message.data.partido;
	response_datum_spec.cant = message.data.cant;	

	// = 
	var response_message = Object.create(response_event);
	response_message.data.event = response_event_spec;
	response_message.data.data.push(response_datum_spec);

	/*
	 * NewData message.
	 * 
	message = {
		name : 'newdata',
		data : {
			event : {
				puesto : 'xxx',
				alcance : 'xxx',
				valor_alc : 'xxx'
				nivel : 'xxx',
			},
			data: [{
				puesto : 'xxx',
				candidato : 'xxx',
				partido : 'xxx',
				cant : 7
			}]
		}
	};
	*/
	
	// = 
	pResponse.send(JSON.stringify(response_message));
}
