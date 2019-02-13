package com.stasdev.backend;

import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	/**
	 * Так правильнее создавать "преднастройки"
	 * */
	@Component
	@Profile("firstStart")
	class UserCommandLineRunner implements CommandLineRunner {

		@Autowired
		ApplicationUserRepository repo;
		@Autowired
		BCryptPasswordEncoder bCryptPasswordEncoder;

		@Override
		public void run(String... args) throws Exception {
			System.out.println("*********************FIRST START*********************************");
			repo.saveAndFlush(
					new ApplicationUser("admin",
							bCryptPasswordEncoder.encode("pass"),
							Collections.singleton(new Role("admin")))
			);
		}
	}

	/**
	 * Преднастройки для теста
	 * Создается админ и юзер для более правильных и не пересекающихся тестов
	 * */
	@Component
	@Profile("test")
	class UserCommandLineRunnerTest implements CommandLineRunner {

		@Autowired
		ApplicationUserRepository repo;
		@Autowired
		BCryptPasswordEncoder bCryptPasswordEncoder;

		@Override
		public void run(String... args) throws Exception {
			System.out.println("*********************TEST*********************************");
			repo.saveAndFlush(
					new ApplicationUser("admin",
							bCryptPasswordEncoder.encode("pass"),
							Collections.singleton(new Role("admin")))
			);

			repo.saveAndFlush(
					new ApplicationUser("user",
							bCryptPasswordEncoder.encode("pass"),
							Collections.singleton(new Role("user")))
			);
		}
	}

	/**
	 * Преднастройки для теста
	 * Создается админ и юзер для более правильных и не пересекающихся тестов
	 * */
	@Component
	@Profile("dev")
	class UserCommandLineRunnerDev implements CommandLineRunner {

		@Autowired
		ApplicationUserRepository repo;
		@Autowired
		BCryptPasswordEncoder bCryptPasswordEncoder;

		@Override
		public void run(String... args) throws Exception {
			System.out.println("*********************DEV*********************************");
			repo.saveAndFlush(
					new ApplicationUser("admin",
							bCryptPasswordEncoder.encode("pass"),
							Collections.singleton(new Role("admin")))
			);

			repo.saveAndFlush(
					new ApplicationUser("user",
							bCryptPasswordEncoder.encode("pass"),
							Collections.singleton(new Role("user")))
			);
		}
	}
}
