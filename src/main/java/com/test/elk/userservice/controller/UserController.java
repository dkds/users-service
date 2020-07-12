package com.test.elk.userservice.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@RestController
@RequestMapping("users")
@Slf4j
public class UserController {

    private final ParameterizedTypeReference<List<User>> typeList = new ParameterizedTypeReference<List<User>>() {
    };

    @Autowired
    @Qualifier("todosService")
    private WebClient webClientTodosService;
    @Autowired
    @Qualifier("mockService")
    private WebClient webClientMockService;

    @GetMapping
    public List<User> getUsers() {
        log.info("Getting users");
        List<User> body = webClientMockService.get().uri("users")
                .retrieve().bodyToMono(typeList)
                .blockOptional()
                .orElse(Collections.emptyList());
        log.info("Got response of {} users", body.size());
        return body;
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<User> getUser(@PathVariable Integer id) {
        log.info("Getting user for id:{}", id);
        Optional<User> body = webClientMockService.get().uri("users/{id}", id)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        log.error("Error getting user for id: {}, {}", id, clientResponse.statusCode());
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(User.class);
                })
                .blockOptional();

        if (body.isPresent()) {
            log.info("Getting todos for user: {}", id);
            List<Todo> todoList = Flux.fromStream(IntStream.range(id * 10, (id * 10) + 10).boxed())
                    .parallel()
                    .runOn(Schedulers.elastic())
                    .flatMap(this::getTodo)
                    .doOnNext(todo -> log.info("Got todo for id: {}", todo.getId()))
                    .doOnNext(todo -> todo.setUserId(id))
                    .ordered(Comparator.comparing(Todo::getId))
                    .collectList()
                    .block();

            body.get().setTodos(todoList);
            log.info("Got response for user: {}", body);
        }

        return ResponseEntity.of(body);
    }

    public Mono<Todo> getTodo(Integer id) {
        return webClientTodosService.get().uri("todos/{id}", id)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        log.error("Error getting todo for id: {}, {}", id, clientResponse.statusCode());
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(Todo.class);
                });
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class User {
        private int id;
        private String name;
        private String phone;
        private String website;
        private Address address;
        private Company company;
        private String email;
        private String username;
        private List<Todo> todos;

        @Data
        public static class Geo {
            private String lng;
            private String lat;
        }

        @Data
        public static class Company {
            private String bs;
            private String catchPhrase;
            private String name;
        }

        @Data
        public static class Address {
            private String zipcode;
            private Geo geo;
            private String suite;
            private String city;
            private String street;
        }
    }

    @Data
    public static class Todo {
        private int id;
        private boolean completed;
        private String title;
        private int userId;
    }
}
