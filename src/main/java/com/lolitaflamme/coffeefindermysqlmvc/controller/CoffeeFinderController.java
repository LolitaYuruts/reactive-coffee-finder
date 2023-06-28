package com.lolitaflamme.coffeefindermysqlmvc.controller;

import com.lolitaflamme.coffeefindermysqlmvc.domain.Coffee;
import com.lolitaflamme.coffeefindermysqlmvc.repository.CoffeeRepository;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Controller
public class CoffeeFinderController {

    private final CoffeeRepository coffeeRepository;

    private final RSocketRequester rSocketRequester;
    private final WebClient client = WebClient.create("http://localhost:8085/coffees");

    public CoffeeFinderController(CoffeeRepository coffeeRepository, RSocketRequester.Builder builder) {
        this.coffeeRepository = coffeeRepository;
        this.rSocketRequester = builder.tcp("localhost", 8084);
    }

    //REST endpoint
    @GetMapping("/coffees")
    private String findCoffees(Model model) {
        Flux<Coffee> coffeeFlux = coffeeRepository.deleteAll()
                .thenMany(client.get()
                        .retrieve()
                        .bodyToFlux(Coffee.class)
                        .flatMap(coffeeRepository::save));

        model.addAttribute("coffeesBySupplier", coffeeFlux);
        return "coffees";
    }

    //RSocket endpoints (Request - Stream interaction model)
    @ResponseBody
    @GetMapping(value = "/coffeesStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Coffee> getCoffeesStream () {
        return rSocketRequester.route("coffeesStream")
                .retrieveFlux(Coffee.class);
    }

    @ResponseBody
    @GetMapping(value = "/coffeeByIdStream/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Coffee> getCoffeeByIdStream (@PathVariable Long id) {
        return rSocketRequester.route("coffeeByIdStream/{id}", id)
                .retrieveFlux(Coffee.class);
    }

    @ResponseBody
    @PostMapping(value = "/createCoffeeStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Coffee> createCoffeeStream (@RequestBody Coffee coffee) {
        return rSocketRequester.route("createCoffeeStream")
                .data(coffee)
                .retrieveFlux(Coffee.class);
    }

    @ResponseBody
    @PutMapping(value = "updateCoffeeStream/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Coffee> updateCoffeeStream(@PathVariable Long id,
                                           @RequestBody Coffee coffee) {
        return rSocketRequester.route("updateCoffeeStream/{id}", id)
                .data(coffee)
                .retrieveFlux(Coffee.class);
    }
}
