package com.lolitaflamme.coffeefindermysqlmvc.controller;

import com.lolitaflamme.coffeefindermysqlmvc.domain.Coffee;
import com.lolitaflamme.coffeefindermysqlmvc.service.CoffeeFinderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;

import static com.lolitaflamme.coffeefindermysqlmvc.domain.Beans.ARABICA;
import static com.lolitaflamme.coffeefindermysqlmvc.domain.Beans.ROBUSTA;
import static com.lolitaflamme.coffeefindermysqlmvc.domain.Roast.DARK_ROAST;
import static com.lolitaflamme.coffeefindermysqlmvc.domain.Roast.MEDIUM_ROAST;

@WebFluxTest(controllers = {CoffeeFinderController.class})
class CoffeeFinderControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private RSocketRequester rSocketRequester;

    @MockBean
    CoffeeFinderService coffeeFinderService;

    private Coffee coffee1, coffee2, coffee3;

    @BeforeEach
    void setUp() {
        coffee1 = new Coffee(1L, "Tanzania Gombe", ARABICA, "citrus, walnuts and a hint of honey",
                MEDIUM_ROAST, new BigDecimal("7.90"), 1.5);
        coffee2 = new Coffee(2L, "Colombia Pink Bourbon", ARABICA, "fruits", MEDIUM_ROAST,
                new BigDecimal(9.40), 1.5);
        coffee3 = new Coffee(3L, "Espresso India Parchment Robusta", ROBUSTA,
                "dark chocolate, tobacco and cinnamon", DARK_ROAST, new BigDecimal(6.90), 2.7);

        //Hooks.onOperatorDebug();
        Mockito.when(coffeeFinderService.getAllCoffees()).thenReturn(Flux.just(coffee1, coffee2, coffee3)
                .checkpoint("All coffees"));

        Mockito.when(coffeeFinderService.getCoffeeByTitle("Tanzania Gombe")).thenReturn(Mono.just(coffee1).checkpoint());
        Mockito.when(coffeeFinderService.getCoffeeByTitle("Colombia Pink Bourbon")).thenReturn(Mono.just(coffee2));
        Mockito.when(coffeeFinderService.getCoffeeByTitle("Espresso India Parchment Robusta")).thenReturn(Mono.just(coffee3));

        Mockito.when(coffeeFinderService.getCoffeeById(1L)).thenReturn(Mono.just(coffee1));
        Mockito.when(coffeeFinderService.getCoffeeById(2L)).thenReturn(Mono.just(coffee2));
        Mockito.when(coffeeFinderService.getCoffeeById(3L)).thenReturn(Mono.just(coffee3));
    }

    @Test
    void findCoffees() {
        Hooks.onOperatorDebug();
        StepVerifier.create(webClient.get()
                        .uri("/coffees")
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .returnResult(Coffee.class)
                        .getResponseBody())
                .expectNext(coffee1)
                .expectNext(coffee2)
                .expectNext(coffee3)
                .verifyComplete();
    }

    @Test
    void findCoffeeByTitle() {
        StepVerifier.create(webClient.get()
                        .uri("/coffees/search?title=Tanzania Gombe")
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .returnResult(Coffee.class)
                        .getResponseBody())
                .expectNext(coffee1)
                .thenAwait(Duration.ofSeconds(10))
                .expectComplete()
                .verify();
    }
}