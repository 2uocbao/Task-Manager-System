package com.quocbao.taskmanagementsystem.service;

import java.util.Locale;
import java.util.ResourceBundle;

public class MessageServiceBundle {
	public String getMessage(String key, String lang) {
		Locale locale = new Locale.Builder().setLanguage(lang).build();
		ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
		return bundle.getString(key);
	}
}
