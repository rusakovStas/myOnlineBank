package com.stasdev.backend;

import com.stasdev.backend.model.entitys.Account;
import com.stasdev.backend.model.entitys.Amount;
import com.stasdev.backend.model.entitys.ApplicationUser;
import com.stasdev.backend.model.entitys.Role;
import com.stasdev.backend.model.repos.AccountRepository;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import com.stasdev.backend.model.services.Preparer;
import com.stasdev.backend.model.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
		AccountRepository accountRepository;
		@Autowired
		BCryptPasswordEncoder bCryptPasswordEncoder;
		@Autowired
		Preparer preparer;

		@Override
		public void run(String... args) throws Exception {
			System.out.println("*********************FIRST START*********************************");
			ApplicationUser admin = repo.saveAndFlush(
					new ApplicationUser("admin",
							bCryptPasswordEncoder.encode("pass"),
							Collections.singleton(new Role("admin")))
			);
			Account account = new Account(
					new Amount("RUR", new BigDecimal("123")),
					"Admin account", admin);
			accountRepository.saveAndFlush(preparer.prepareToSave(account));
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
		AccountRepository accountRepository;
		@Autowired
		BCryptPasswordEncoder bCryptPasswordEncoder;
		@Autowired
		Preparer preparer;

		@Override
		public void run(String... args) throws Exception {
			System.out.println("*********************TEST*********************************");
			ApplicationUser admin = repo.saveAndFlush(
					new ApplicationUser("admin",
							bCryptPasswordEncoder.encode("pass"), Collections.singleton(new Role("admin"))));
			Account account = new Account(
					new Amount("RUR", new BigDecimal("100000")),
					"God account", admin);
			accountRepository.saveAndFlush(preparer.prepareToSave(account));

			ApplicationUser user = repo.saveAndFlush(
					new ApplicationUser("user",
							bCryptPasswordEncoder.encode("pass"),
							Collections.singleton(new Role("user"))));
			Account accountForUser = new Account(
					new Amount("RUR", new BigDecimal("1000")),
					"", user);
			Account accountForUserTwo = new Account(
					new Amount("RUR", new BigDecimal("10000")),
					"", user);

			accountRepository.saveAndFlush(preparer.prepareToSave(accountForUser));
			accountRepository.saveAndFlush(preparer.prepareToSave(accountForUserTwo));
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
		AccountRepository accountRepository;
		@Autowired
		BCryptPasswordEncoder bCryptPasswordEncoder;
		@Autowired
		Preparer preparer;

		@Override
		public void run(String... args) throws Exception {
			System.out.println("*********************DEV*********************************");
			ApplicationUser admin = repo.saveAndFlush(
					new ApplicationUser("admin",
							bCryptPasswordEncoder.encode("pass"), Collections.singleton(new Role("admin"))));
			Account account = new Account(
					new Amount("RUR", new BigDecimal("100000")),
					"God account", admin);
			accountRepository.saveAndFlush(preparer.prepareToSave(account));

			ApplicationUser user = repo.saveAndFlush(
					new ApplicationUser("user",
							bCryptPasswordEncoder.encode("pass"),
							Collections.singleton(new Role("user"))));
			Account accountForUser = new Account(
					new Amount("RUR", new BigDecimal("1000")),
					"", user);
			Account accountForUserTwo = new Account(
					new Amount("RUR", new BigDecimal("10000")),
					"", user);

			accountRepository.saveAndFlush(preparer.prepareToSave(accountForUser));
			accountRepository.saveAndFlush(preparer.prepareToSave(accountForUserTwo));
		}
	}
}
