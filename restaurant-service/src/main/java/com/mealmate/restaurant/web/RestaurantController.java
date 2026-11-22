package com.mealmate.restaurant.web;
import com.mealmate.restaurant.service.RestaurantService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*; import static com.mealmate.restaurant.web.RestaurantDtos.*;
@RestController @RequestMapping("/restaurants")
public class RestaurantController { private final RestaurantService s; public RestaurantController(RestaurantService s){this.s=s;}
 @PostMapping public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody RestaurantRequest x){return ResponseEntity.status(HttpStatus.CREATED).body(s.create(x));}
 @GetMapping public List<RestaurantResponse> all(){return s.all();} @GetMapping("/{id}") public RestaurantResponse one(@PathVariable UUID id){return s.getResponse(id);}
 @PutMapping("/{id}") public RestaurantResponse update(@PathVariable UUID id,@Valid @RequestBody RestaurantRequest x){return s.update(id,x);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id){s.delete(id);}
 @PostMapping("/{id}/menu-items") public ResponseEntity<MenuItemResponse> add(@PathVariable UUID id,@Valid @RequestBody MenuItemRequest x){return ResponseEntity.status(HttpStatus.CREATED).body(s.addItem(id,x));}
 @GetMapping("/{id}/menu-items") public List<MenuItemResponse> menu(@PathVariable UUID id){return s.menu(id);}
 @PutMapping("/{rid}/menu-items/{iid}") public MenuItemResponse edit(@PathVariable UUID rid,@PathVariable UUID iid,@Valid @RequestBody MenuItemRequest x){return s.updateItem(rid,iid,x);}
 @DeleteMapping("/{rid}/menu-items/{iid}") @ResponseStatus(HttpStatus.NO_CONTENT) public void remove(@PathVariable UUID rid,@PathVariable UUID iid){s.deleteItem(rid,iid);}
}
