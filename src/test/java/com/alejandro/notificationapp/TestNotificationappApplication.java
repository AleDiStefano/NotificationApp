package com.alejandro.notificationapp;

import org.springframework.boot.SpringApplication;

public class TestNotificationappApplication {

	public static void main(String[] args) {
		SpringApplication.from(NotificationappApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
