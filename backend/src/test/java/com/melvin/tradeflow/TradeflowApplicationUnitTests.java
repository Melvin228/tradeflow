package com.melvin.tradeflow;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class TradeflowApplicationUnitTests {
	@Test
	void mainDelegatesToSpringApplication() {
		String[] args = {};

		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			TradeflowApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(TradeflowApplication.class, args));
		}
	}
}