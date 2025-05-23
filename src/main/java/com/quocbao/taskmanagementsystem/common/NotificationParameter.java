package com.quocbao.taskmanagementsystem.common;

public enum NotificationParameter {

	SOUND("default"), COLOR("#ff6347");
	
	private String value;
	
	NotificationParameter(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}
