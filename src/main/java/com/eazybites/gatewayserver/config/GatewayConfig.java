package com.eazybites.gatewayserver.config;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator eazyBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder
        .routes()
        .route(p -> p
          .path("/eazybank/accounts/**")
          .filters(f -> f.rewritePath("/eazybank/accounts/(?<segment>.*)","/${segment}")
            .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
            .circuitBreaker(config -> config.setName("accountsCircuitBreaker")
              .setFallbackUri("fordward:/contactSupport")
            )
          )
          .uri("lb://ACCOUNTS"))

        .route(p -> p
          .path("/eazybank/loans/**")
          .filters(f -> f.rewritePath("/eazybank/loans/(?<segment>.*)","/${segment}")
            .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
            // recomendado usar retry con solo con metodos idempotentes
            .retry(retryConfig -> retryConfig.setRetries(3)
              .setMethods(HttpMethod.GET)
              // parametros:
              // Despues de que el request falla en responder por demora basado en el valor especificado en application.yml (responseTimeout)
              // Se realizan reintentos de conexion
              // firstBackoff(backoff mínimo): Tiempo inicial de reintento (el primer reintento se realiza a los 100ms )
              // maxBackoff(backoff máximo): Tope de tiempo a esperar para realizar el reintento
              // factor(multiplicador exponencial): delay(n) = initialDelay × (multiplicador ^ (n-1)) n= numero de reintento (para calcular el retraso en el intento)
              // jitter (aleatoriedad en el backoff): Si está en true, introduce una variación aleatoria en los tiempos
                  //de espera para evitar que muchos clientes reintenten al mismo tiempo (efecto avalancha).
              .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
          )
          .uri("lb://LOANS"))

        .route(p -> p
          .path("/eazybank/cards/**")
          .filters(f -> f.rewritePath("/eazybank/cards/(?<segment>.*)","/${segment}")
            .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
          )
          .uri("lb://CARDS"))    
        .build();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer(){
      return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
      .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
      .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4))
      .build())
      .build());
    }
}
