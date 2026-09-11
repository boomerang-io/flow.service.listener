package io.boomerang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import io.swagger.v3.oas.models.OpenAPI;

@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

	@Bean
	public OpenAPI api() {
		return new OpenAPI();
	}
}
