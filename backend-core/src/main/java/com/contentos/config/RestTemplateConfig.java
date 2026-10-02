package com.contentos.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * WHAT IT DOES:
 * Configures RestTemplate for HTTP communication with external services.
 *
 * WHY IT EXISTS:
 * RestTemplate needs sensible defaults (timeouts, error handling).
 * This bean is used by AiEngineClient to call the FastAPI service.
 *
 * HOW IT CONNECTS:
 * RestTemplateBuilder → RestTemplate → AiEngineClient
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(60))  // LLM calls can take time
            .build();
    }
}
