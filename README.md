# Reactive Coffee Finder

## Introduction

Reactive-coffee-finder is an asynchronous, highly available, scalable, fast app that requires a minimum amount of 
resources to efficiently complete a task, developed to asynchronously receive available coffees from [**Reactive Coffee 
Supplier**](https://github.com/LolitaYuruts/reactive-coffee-supplier), save received coffees into DB (**MongoDB**), 
show available coffees to the end users via server-side Java template engine **Thymeleaf** and provide API for CRUD 
operations via RSocket, binary protocol for use on byte stream transports such as TCP, WebSockets, and Aeron, to 
eliminate the inter-process communication cons via _"Request - Stream"_ interaction model.

## Technologies

- Java 11
- R2DBC
- MongoDB
- Spring Data
- Project Reactor
- Spring WebFlux
- Spring MVC
- Thymeleaf
- Domain-Driven Design
- REST Architecture
- RSocket