package com.net.engine.core.security;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.net.engine.exceptions.RefusedAddressException;

public class DefaultConnectionFilter implements IConnectionFilter {
	private final Set<String> addressWhiteList;
	private final Set<String> bannedAddresses;
	private final ConcurrentMap<String, AtomicInteger> addressMap;
	private int maxConnectionsPerIp;

	public DefaultConnectionFilter() {
		this.addressWhiteList = new HashSet<String>();
		this.bannedAddresses = new HashSet<String>();
		this.addressMap = new ConcurrentHashMap<String, AtomicInteger>();
		this.maxConnectionsPerIp = 10;
	}

	public void addBannedAddress(String ipAddress) {
		synchronized (this.bannedAddresses) {
			this.bannedAddresses.add(ipAddress);
		}
	}

	public void addWhiteListAddress(String ipAddress) {
		synchronized (this.addressWhiteList) {
			this.addressWhiteList.add(ipAddress);
		}
	}

	public String[] getBannedAddresses() {
		String[] set = null;

		synchronized (this.bannedAddresses) {
			set = new String[this.bannedAddresses.size()];
			set = (String[]) this.bannedAddresses.toArray(set);
		}

		return set;
	}

	public int getMaxConnectionsPerIp() {
		return this.maxConnectionsPerIp;
	}

	public String[] getWhiteListAddresses() {
		String[] set = null;

		synchronized (this.addressWhiteList) {
			set = new String[this.addressWhiteList.size()];
			set = (String[]) this.addressWhiteList.toArray(set);
		}

		return set;
	}

	public void removeAddress(String ipAddress) {
		synchronized (this.addressMap) {
			AtomicInteger count = (AtomicInteger) this.addressMap.get(ipAddress);

			if (count != null) {
				int value = count.decrementAndGet();

				if (value == 0)
					this.addressMap.remove(ipAddress);
			}
		}
	}

	public void removeBannedAddress(String ipAddress) {
		synchronized (this.bannedAddresses) {
			this.bannedAddresses.remove(ipAddress);
		}
	}

	public void removeWhiteListAddress(String ipAddress) {
		synchronized (this.addressWhiteList) {
			this.addressWhiteList.remove(ipAddress);
		}
	}

	public void setMaxConnectionsPerIp(int max) {
		this.maxConnectionsPerIp = max;
	}

	public void validateAndAddAddress(String ipAddress) throws RefusedAddressException {
		synchronized (this.addressWhiteList) {
			if (this.addressWhiteList.contains(ipAddress)) {
				return;
			}
		}

		if (isAddressBanned(ipAddress)) {
			throw new RefusedAddressException("Ip Address: " + ipAddress + " is banned!");
		}

		synchronized (this.addressMap) {
			AtomicInteger count = (AtomicInteger) this.addressMap.get(ipAddress);

			if ((count != null) && (count.intValue() >= this.maxConnectionsPerIp)) {
				throw new RefusedAddressException(
						"Ip Address: " + ipAddress + " has reached maximum allowed connections.");
			}

			if (count == null) {
				count = new AtomicInteger(1);
				this.addressMap.put(ipAddress, count);
			} else {
				count.incrementAndGet();
			}
		}
	}

	private boolean isAddressBanned(String ip) {
		boolean isBanned = false;

		synchronized (this.bannedAddresses) {
			isBanned = this.bannedAddresses.contains(ip);
		}

		return isBanned;
	}

	@SuppressWarnings("unused")
	private boolean isValidIpCount(String ip) throws RefusedAddressException {
		boolean ok = false;

		AtomicInteger count = (AtomicInteger) this.addressMap.get(ip);

		if (count == null) {
			count = new AtomicInteger(0);
			ok = true;
		} else if (count.intValue() < this.maxConnectionsPerIp) {
			count.incrementAndGet();
			ok = true;
		}

		return ok;
	}
}