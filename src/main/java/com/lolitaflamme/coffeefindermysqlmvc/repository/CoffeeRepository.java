package com.lolitaflamme.coffeefindermysqlmvc.repository;

import com.lolitaflamme.coffeefindermysqlmvc.domain.Coffee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CoffeeRepository extends ReactiveCrudRepository<Coffee, Long> {
}
