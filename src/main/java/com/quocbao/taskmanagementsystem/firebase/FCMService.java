package com.quocbao.taskmanagementsystem.firebase;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.quocbao.taskmanagementsystem.common.NotificationParameter;

@Service
public class FCMService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FCMService.class);

	public void sendMessage(Map<String, String> data, String token, String topic, String title, String body)
			throws InterruptedException, ExecutionException {
		Message message = getPreconfiguredMessageWithData(data, token, topic, title, body);
		String response = sendAndGetResponse(message);
		LOGGER.info("Sent message with data. Topic: " + token + ", " + response);
	}

	private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
		return FirebaseMessaging.getInstance().sendAsync(message).get();
	}

	private AndroidConfig getAndroidConfig(String topic) {
		return AndroidConfig.builder().setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
				.setPriority(AndroidConfig.Priority.HIGH)
				.setNotification(AndroidNotification.builder().setSound(NotificationParameter.SOUND.getValue())
						.setColor(NotificationParameter.COLOR.getValue()).setTag(topic)
						.setClickAction("FLUTTER_NOTIFICATION_CLICK")
						.build())
				.build();
	}

	private ApnsConfig getApnsConfig(String topic) {
		return ApnsConfig.builder().setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
	}

	private Message getPreconfiguredMessageWithData(Map<String, String> data, String token, String topic, String title,
			String body) {
		return getPreConfiguredMessageBuilder(topic, title, body).putAllData(data).setToken(token).build();
	}

	private Message.Builder getPreConfiguredMessageBuilder(String topic, String title, String body) {
		AndroidConfig androidConfig = getAndroidConfig(topic);
		ApnsConfig apnsConfig = getApnsConfig(topic);
		return Message.builder().setApnsConfig(apnsConfig).setAndroidConfig(androidConfig)
				.setNotification(new Notification(title, body));
	}

}
