package com.user_service;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class UserServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
	public String getApplicationName() {
		return "user-service";
	}
}