package com.appspace.roomlinktest;

public class HubConnectionData {
	private String hubName;
	private String hubPath;
	private String defaultListenSharedAccessSignature;
	private String defaultFullSharedAccessSignature;

	public String getHubName() {
		return hubName;
	}

	public void setHubName(String hubName) {
		this.hubName = hubName;
	}

	public String getHubPath() {
		return hubPath;
	}

	public void setHubPath(String hubPath) {
		this.hubPath = hubPath;
	}

	public String getDefaultListenSharedAccessSignature() {
		return defaultListenSharedAccessSignature;
	}

	public void setDefaultListenSharedAccessSignature(
			String defaultListenSharedAccessSignature) {
		this.defaultListenSharedAccessSignature = defaultListenSharedAccessSignature;
	}

	public String getDefaultFullSharedAccessSignature() {
		return defaultFullSharedAccessSignature;
	}

	public void setDefaultFullSharedAccessSignature(
			String defaultFullSharedAccessSignature) {
		this.defaultFullSharedAccessSignature = defaultFullSharedAccessSignature;
	}

	public String toString() {
		return String.format("hubName:%s, hubPath:%s ", hubName, hubPath);
	}
}
