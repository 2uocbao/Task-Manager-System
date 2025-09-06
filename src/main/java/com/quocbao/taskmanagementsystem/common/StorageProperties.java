package com.quocbao.taskmanagementsystem.common;

import org.springframework.stereotype.Component;

@Component
public class StorageProperties {
	/**
	 * Folder location for storing files
	 */
	// private String location = "F:\\storeFile";
	private String location = "/app/storeFile";

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
