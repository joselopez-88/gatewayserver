package com.eazybites.gatewayserver.config;

import java.time.LocalDateTime;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator eazyBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder
        .routes()
        .route(p -> p
          .path("/eazybank/accounts/**")
          .filters(f -> f.rewritePath("/eazybank/accounts/(?<segment>.*)","/${segment}")
            .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
          .uri("lb://ACCOUNTS"))
        .route(p -> p
          .path("/eazybank/loans/**")
          .filters(f -> f.rewritePath("/eazybank/loans/(?<segment>.*)","/${segment}")
            .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
          .uri("lb://LOANS"))
        .route(p -> p
          .path("/eazybank/cards/**")
          .filters(f -> f.rewritePath("/eazybank/cards/(?<segment>.*)","/${segment}")
            .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
          .uri("lb://CARDS"))    
        .build();
    }
}
