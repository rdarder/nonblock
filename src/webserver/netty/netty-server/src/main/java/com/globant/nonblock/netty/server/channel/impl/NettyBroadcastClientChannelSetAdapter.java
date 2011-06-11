package com.globant.nonblock.netty.server.channel.impl;

import java.util.Collection;
import java.util.Iterator;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import com.globant.nonblock.netty.server.channel.BroadcastClientChannelSet;
import com.globant.nonblock.netty.server.channel.ClientChannel;

public final class NettyBroadcastClientChannelSetAdapter implements BroadcastClientChannelSet {

	private final ChannelGroup channelGroup = new DefaultChannelGroup();

	@Override
	public void writeToAll(final String message) {
		this.channelGroup.write(message);
	}

	@Override
	public boolean add(final ClientChannel e) {
		final NettyClientChannelAdapter ncc = (NettyClientChannelAdapter) e;
		return this.channelGroup.add(ncc.getWrappedChannel());
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(Collection<? extends ClientChannel> c) {
		return this.channelGroup.addAll((Collection<? extends Channel>) c);
	}

	@Override
	public void clear() {
		this.channelGroup.clear();
	}

	@Override
	public boolean contains(Object o) {
		return this.channelGroup.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.channelGroup.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return this.channelGroup.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<ClientChannel> iterator() {
		return (Iterator) this.channelGroup.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return this.channelGroup.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.channelGroup.retainAll(c);
	}

	@Override
	public int size() {
		return this.channelGroup.size();
	}

	@Override
	public Object[] toArray() {
		return this.channelGroup.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.channelGroup.toArray(a);
	}

}
