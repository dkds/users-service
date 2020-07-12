package com.test.elk.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class BeanConfig {

    @Value("${service.mock.base-url}")
    String mockBaseUrl;
    @Value("${service.todos.base-url}")
    String todosBaseUrl;

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("Request Header {}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    @Bean
    @Qualifier("mockService")
    public WebClient webClientMockService(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(mockBaseUrl).filter(logRequest()).build();
    }

    @Bean
    @Qualifier("todosService")
    @LoadBalanced
    public WebClient webClientTodosService(WebClient.Builder webClientBuilder, ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        return webClientBuilder.baseUrl(todosBaseUrl).filter(lbFunction).build();
    }
}
