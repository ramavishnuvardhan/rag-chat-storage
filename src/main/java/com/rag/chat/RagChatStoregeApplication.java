package com.rag.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class RagChatStoregeApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagChatStoregeApplication.class, args);
	}

}
