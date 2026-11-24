package com.mealmate.customer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class CustomerApplication {
    public static void main(String[] args) { SpringApplication.run(CustomerApplication.class, args); }
    @Bean OpenAPI mealMateOpenAPI() { return new OpenAPI().servers(List.of(new Server().url("http://localhost:9000"))).components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme().type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))).addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth")); }
}

@Entity
class Customer {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @NotBlank private String name;
    @NotBlank private String email;
    private String phone;
    protected Customer() {}
    Customer(String name, String email, String phone) { this.name=name; this.email=email; this.phone=phone; }
    public UUID getId(){return id;} public String getName(){return name;} public String getEmail(){return email;} public String getPhone(){return phone;}
    public void update(CustomerRequest x){name=x.name();email=x.email();phone=x.phone();}
}

record CustomerRequest(@NotBlank String name, @NotBlank String email, String phone) {}
interface CustomerRepository extends JpaRepository<Customer, UUID> {}

@RestController
@RequestMapping("/customers")
class CustomerController {
    private final CustomerRepository repository;
    CustomerController(CustomerRepository repository){this.repository=repository;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) Customer create(@Valid @RequestBody CustomerRequest request){return repository.save(new Customer(request.name(),request.email(),request.phone()));}
    @GetMapping List<Customer> all(){return repository.findAll();}
    @GetMapping("/{id}") Customer one(@PathVariable UUID id){return repository.findById(id).orElseThrow();}
    @PutMapping("/{id}") Customer update(@PathVariable UUID id,@Valid @RequestBody CustomerRequest request){Customer c=one(id);c.update(request);return repository.save(c);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void delete(@PathVariable UUID id){repository.delete(one(id));}
}
