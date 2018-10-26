package com.net.engine.core.security;

import com.net.engine.exceptions.RefusedAddressException;

public abstract interface IConnectionFilter {
	public abstract void addBannedAddress(String paramString);

	public abstract void removeBannedAddress(String paramString);

	public abstract String[] getBannedAddresses();

	public abstract void validateAndAddAddress(String paramString) throws RefusedAddressException;

	public abstract void removeAddress(String paramString);

	public abstract void addWhiteListAddress(String paramString);

	public abstract void removeWhiteListAddress(String paramString);

	public abstract String[] getWhiteListAddresses();

	public abstract int getMaxConnectionsPerIp();

	public abstract void setMaxConnectionsPerIp(int paramInt);
}