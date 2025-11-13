package com.nutribot.bot;

import org.springframework.boot.SpringApplication;

public class TestNutribotTelegramApplication {

	public static void main(String[] args) {
		SpringApplication.from(NutribotTelegramApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
