package com.globant.nonblock.netty.server.service.votes.impl;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.globant.nonblock.netty.server.entity.Voto;
import com.globant.nonblock.netty.server.message.loader.SubmitVotesMessage;
import com.globant.nonblock.netty.server.message.loader.VoteDatum;
import com.globant.nonblock.netty.server.message.subscription.SubscribeMessage;
import com.globant.nonblock.netty.server.service.location.Location;
import com.globant.nonblock.netty.server.service.location.LocationService;
import com.globant.nonblock.netty.server.service.location.LocationType;
import com.globant.nonblock.netty.server.service.votes.VoteService;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

/**
 * JPA backed {@link VoteService} implementation.
 * 
 * @author Julian Gutierrez Oschmann
 * 
 */
public class JpaVoteServiceImpl implements VoteService {

	/**
	 * JPA entity manager.
	 */
	private final Provider<EntityManager> entityManager;

	/**
	 * Location service.
	 */
	private final LocationService locationService;

	/**
	 * HQL Query template.
	 */
	private static final String query =
		"	select sum(votos),	 			" +
		"		   puesto,					" +
		"		   candidato,				" +
		"		   partido,					" +			
		"   	   %s         	 			" +
		"  	  from Voto						" +
		"    where %s = '%s' 				" +
		"      and puesto = '%s'			" +
		" group by candidato, 				" +
		"	       %s 						"; 
	
	@Inject
	public JpaVoteServiceImpl(final Provider<EntityManager> entityManager, final LocationService locationService) {
		super();
		this.entityManager = entityManager;
		this.locationService = locationService;
	}

	@Override
	@Transactional
	public void addVotes(final SubmitVotesMessage newResults) {

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
	public List<Object[]> calculateStatus(final SubscribeMessage message) {
		final Query query = this.entityManager.get().createQuery(buildQueryString(message));
		final List<Object[]> queryResult = query.getResultList();
		return queryResult;
	}

	private String buildQueryString(final SubscribeMessage message) {
		return String.format(query, 
				calculateProjections(message.getNivel()),
				message.getAlcance().toString().toLowerCase(),
				message.getLugar(),
				message.getPuesto(),
				message.getNivel().toString().toLowerCase());
	}

	private String calculateProjections(final LocationType nivel) {
		if (nivel.getParent() == null) {
			return nivel.toString().toLowerCase();
		} else {
			return nivel.toString().toLowerCase() + ", " + calculateProjections(nivel.getParent());
		}
	}
	
}
