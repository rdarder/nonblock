package com.globant.nonblock.netty.server.service.votes.impl;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.globant.nonblock.netty.server.entity.Voto;
import com.globant.nonblock.netty.server.message.loader.VoteDatum;
import com.globant.nonblock.netty.server.message.loader.VotesResult;
import com.globant.nonblock.netty.server.message.newdata.NewDataDatum;
import com.globant.nonblock.netty.server.message.newdata.NewDataMessage;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.location.Location;
import com.globant.nonblock.netty.server.service.location.LocationService;
import com.globant.nonblock.netty.server.service.votes.VoteService;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class JpaVoteServiceImpl implements VoteService {

	private final Provider<EntityManager> entityManager;

	private final LocationService locationService;

	@Inject
	public JpaVoteServiceImpl(final Provider<EntityManager> entityManager, final LocationService locationService) {
		super();
		this.entityManager = entityManager;
		this.locationService = locationService;
	}

	@Override
	@Transactional
	public void addVotes(final VotesResult newResults) {

		final Location l = this.locationService.findLocation(newResults.getMesa());

		for (final VoteDatum vd : newResults.getData()) {
			final Voto v = new Voto();
			v.setMesa(newResults.getMesa());
			v.setLocal(l.getLocal());
			v.setSeccional(l.getSeccional());
			v.setLocalidad(l.getLocalidad());
			v.setDepartamento(l.getDepartamento());
			v.setProvincia(l.getProvincia());

			v.setPuesto(vd.getPuesto());
			v.setPartido(vd.getPartido());
			v.setCandidato(vd.getCandidato());
			v.setVotos(Long.valueOf(vd.getCantidad()));

			this.entityManager.get().persist(v);
		}

	}

	@SuppressWarnings("unchecked")
	@Transactional
	public NewDataMessage calculateStatus(final SubscribeMessage message) {

		final Query q = this.entityManager.get().createQuery(
				"select sum(v.votos), v." + message.getNivel().toString().toLowerCase() + ", v.candidato from Voto v where v." + message.getAlcance().toString().toLowerCase() + " = '" + message.getAlcanceValue() + "'"
						+ " and v.puesto = '" + message.getPuesto() + "' " + " group by v.candidato, v." + message.getNivel().toString().toLowerCase());

		List<Object[]> queryResult = q.getResultList();
		NewDataMessage resultMessage = new NewDataMessage();
		for (Object[] e : queryResult) {
			Long cant = (Long) e[0];
			String nivel = (String) e[1];
			String candidato = (String) e[2];
			resultMessage.getDatos().add(new NewDataDatum(cant, nivel, candidato));
		}
		return resultMessage;
	}

}
