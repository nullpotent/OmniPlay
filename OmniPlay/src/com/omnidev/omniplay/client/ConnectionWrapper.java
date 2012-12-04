package com.omnidev.omniplay.client;

public class ConnectionWrapper {
	private String name;
	private String address;
	
	public ConnectionWrapper(final String name, final String address) {
		this.name 		= name;
		this.address 	= address;
	}
	
	public String toString() {
		return this.name;
	}
	
	public String getAddress() {
		return this.address;
	}
}
