package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class DemoApplication {
	@Order(1)
	@Bean
	CommandLineRunner commandLineRunner() {
		return args -> {
			int N = 10;
			CountDownLatch latch = new CountDownLatch(N);

			Queue<String> queue = new Queue<>(30);

			Thread[] producerThreads = new Thread[N];
			Thread[] consumerThreads = new Thread[N];

			Runnable producer = () -> {
				latch.countDown();

				try {
					latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for(int i = 0; i < 10; ++i) {
					try {
						queue.push(String.format("%s_%05d", Thread.currentThread().getName(), i));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};

			Runnable consumer = () -> {
				for(int i = 0; i < 10; ++i) {
					try {
						System.out.printf("%s :: %s\n", Thread.currentThread().getName(), queue.pop());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};

			for(int i = 0; i < N; ++i) {
				producerThreads[i] = new Thread(producer);
				producerThreads[i].start();
			}

			for(int i = 0; i < N; ++i) {
				consumerThreads[i] = new Thread(consumer);
				consumerThreads[i].start();
			}

			for(int i = 0; i < N; ++i) {
				producerThreads[i].join();
			}

			for(int i = 0; i < N; ++i) {
				consumerThreads[i].join();
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
