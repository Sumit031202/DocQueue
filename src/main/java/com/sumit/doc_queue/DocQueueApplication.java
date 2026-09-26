package com.sumit.doc_queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DocQueueApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocQueueApplication.class, args);
	}

}
