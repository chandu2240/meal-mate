package com.mealmate.restaurant.web;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.util.UUID;
public final class RestaurantDtos { private RestaurantDtos(){}
 public record RestaurantRequest(@NotBlank @Size(max=150) String name,@Size(max=300) String address,@Size(max=40) String phone){}
 public record RestaurantResponse(UUID id,String name,String address,String phone){ public static RestaurantResponse of(com.mealmate.restaurant.domain.Restaurant r){return new RestaurantResponse(r.getId(),r.getName(),r.getAddress(),r.getPhone());} }
 public record MenuItemRequest(@NotBlank @Size(max=150) String name,@Size(max=1000) String description,@NotNull @DecimalMin("0.01") BigDecimal price){}
 public record MenuItemResponse(UUID id,UUID restaurantId,String name,String description,BigDecimal price){ public static MenuItemResponse of(com.mealmate.restaurant.domain.MenuItem m){return new MenuItemResponse(m.getId(),m.getRestaurant().getId(),m.getName(),m.getDescription(),m.getPrice());} }
}
