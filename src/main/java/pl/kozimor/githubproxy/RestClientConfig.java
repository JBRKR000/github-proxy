package pl.kozimor.githubproxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class RestClientConfig {
    @Bean
    RestClient restClient(@Value("${github.base-url}") String githubBaseUrl) {
        return RestClient.builder()
                .baseUrl(githubBaseUrl)
                .build();
    }
}
