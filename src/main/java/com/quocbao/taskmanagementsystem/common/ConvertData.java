package com.quocbao.taskmanagementsystem.common;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class ConvertData {

	ConvertData() {
	}

	public static String timeStampToString(Timestamp timestamp) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		return timestamp.toLocalDateTime().format(formatter);
	}

	public static Timestamp toTimestamp(String dateString) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // Define the date format
		try {
			Date parseDate = formatter.parse(dateString);
			return new Timestamp(parseDate.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String toLocalDate(Date date) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()) // or specify a specific zone
				.toLocalDate();
		return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-d"));
	}

	public static Boolean isImage(String imageName) {
		List<String> fileImage = new ArrayList<>();
		fileImage.add("jpg");
		fileImage.add("png");
		fileImage.add("jpeg");
		fileImage.add("jfif");
		return fileImage.stream()
				.anyMatch(t -> imageName.substring(imageName.indexOf(".") + 1, imageName.length()).equals(t));

	}

}
