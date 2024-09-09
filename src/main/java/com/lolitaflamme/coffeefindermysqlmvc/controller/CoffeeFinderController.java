package com.lolitaflamme.coffeefindermysqlmvc.controller;

import com.lolitaflamme.coffeefindermysqlmvc.domain.Coffee;
import com.lolitaflamme.coffeefindermysqlmvc.service.CoffeeFinderService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CoffeeFinderController {

    private static final String SEARCH_PARAM_TITLE = "title";

    @NonNull
    private final CoffeeFinderService coffeeFinderService;

    @NonNull
    private final RSocketRequester rSocketRequester;
    private final WebClient client = WebClient.create("http://localhost:8085/coffees");

    //REST endpoint
    @GetMapping("/coffeesWithModel")
    public String findCoffees(Model model) {
        model.addAttribute("coffeesBySupplier", coffeeFinderService.getAllCoffees());
        return "coffees";
    }

    @GetMapping("/coffees")
    @ResponseBody
    public Flux<Coffee> findCoffees() {
        return coffeeFinderService.getAllCoffees();
    }

    @ResponseBody
    @GetMapping(value = "/coffees/search")
    public Mono<Coffee> getCoffeeByTitle(@RequestParam Map<String, String> searchParams) {
        Map<String, String> titleParam = new HashMap<>();
        if (!searchParams.isEmpty()) {
            titleParam = searchParams.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(SEARCH_PARAM_TITLE))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return coffeeFinderService.getCoffeeByTitle(titleParam.get(SEARCH_PARAM_TITLE));
    }

    //RSocket endpoints (Request - Stream interaction model)
    @ResponseBody
    @GetMapping(value = "/coffeesStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Coffee> getCoffeesStream() {
        return rSocketRequester.route("coffeesStream")
                .retrieveFlux(Coffee.class);
    }

    @ResponseBody
    @GetMapping(value = "/coffeeByIdStream/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Coffee> getCoffeeByIdStream(@PathVariable Long id) {
        return rSocketRequester.route("coffeeByIdStream/{id}", id)
                .retrieveFlux(Coffee.class);
    }

    @ResponseBody
    @PostMapping(value = "/createCoffeeStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Coffee> createCoffeeStream(@RequestBody Coffee coffee) {
        return rSocketRequester.route("createCoffeeStream")
                .data(coffee)
                .retrieveFlux(Coffee.class);
    }

    @ResponseBody
    @PutMapping(value = "/updateCoffeeStream/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Coffee> updateCoffeeStream(@PathVariable Long id,
                                           @RequestBody Coffee coffee) {
        return rSocketRequester.route("updateCoffeeStream/{id}", id)
                .data(coffee)
                .retrieveFlux(Coffee.class);
    }
}
