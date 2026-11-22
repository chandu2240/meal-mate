package com.mealmate.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import java.util.List;

@SpringBootApplication
public class RestaurantServiceApplication { public static void main(String[] args) { SpringApplication.run(RestaurantServiceApplication.class, args); } @Bean OpenAPI mealMateOpenAPI(){ return new OpenAPI().servers(List.of(new Server().url("http://localhost:9000"))).components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("bearerAuth",new io.swagger.v3.oas.models.security.SecurityScheme().type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))).addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth")); } }
