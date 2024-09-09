package com.lolitaflamme.coffeefindermysqlmvc.service;

import com.lolitaflamme.coffeefindermysqlmvc.domain.Coffee;
import com.lolitaflamme.coffeefindermysqlmvc.repository.CoffeeRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class CoffeeFinderService {

    @NonNull
    private final CoffeeRepository coffeeRepository;

    private final WebClient client = WebClient.create("http://localhost:8085/coffees");

    public Flux<Coffee> getAllCoffees() {
        Flux<Coffee> coffeeFlux = coffeeRepository.deleteAll()
                .thenMany(client.get()
                        .retrieve()
                        .bodyToFlux(Coffee.class)
                        .flatMap(coffeeRepository::save));

        coffeeFlux.thenMany(coffeeRepository.findAll())
                .subscribe(System.out::println);
        return coffeeFlux;
    }

    public Mono<Coffee> getCoffeeByTitle(String title) {
        return client.get()
                .uri("/byTitle/{title}", title)
                .retrieve()
                .bodyToMono(Coffee.class);
    }

    public Mono<Coffee> getCoffeeById(Long id) {
        return coffeeRepository.findById(id);
    }

}
