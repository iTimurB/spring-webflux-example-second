package com.example.secondwebfluxexample;

import com.example.secondwebfluxexample.dto.AuthResponseDto;
import com.example.secondwebfluxexample.model.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class SecondWebfluxExampleApplication {

    public static void main(String[] args) {
        WebClient webClient = WebClient.create();
        Mono<AuthResponseDto> response = webClient.post()
                .uri("http://localhost:8888/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{ \"username\": \"admin\", \"password\": \"admin\" }")
                .retrieve()
                .bodyToMono(AuthResponseDto.class);

        AuthResponseDto jwtToken = response.block();

        assert jwtToken != null;
        WebClient webClient1 = getWebClientWithJwtToken(jwtToken.getToken());

        webClient1.get().uri("/admin/all")
                .retrieve()
                .bodyToFlux(User.class)
                .doOnNext(System.out::println) // Вывод в консоль
                .last() // Получение последнего элемента
                .flatMap(user -> {
                    // Запрос пользователя по id
                    return webClient1.get().uri("/admin/user/{userId}", user.getId())
                            .retrieve()
                            .bodyToMono(User.class);
                })
                .doOnSuccess(System.out::println) // Вывод в консоль
                .block(); // Ожидание выполнения
    }

    public static WebClient getWebClientWithJwtToken(String jwtToken) {
        return WebClient.builder()
                .baseUrl("http://localhost:8888")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .build();
    }

}
