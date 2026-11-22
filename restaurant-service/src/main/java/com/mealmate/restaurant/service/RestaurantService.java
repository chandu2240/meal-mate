package com.mealmate.restaurant.service;
import com.mealmate.restaurant.domain.*; import com.mealmate.restaurant.repository.*; import com.mealmate.restaurant.web.RestaurantDtos.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service @Transactional
public class RestaurantService {
 private final RestaurantRepository restaurants; private final MenuItemRepository items;
 public RestaurantService(RestaurantRepository r,MenuItemRepository i){restaurants=r;items=i;}
 public List<RestaurantResponse> all(){return restaurants.findAll().stream().map(RestaurantResponse::of).toList();}
 public RestaurantResponse create(RestaurantRequest x){return RestaurantResponse.of(restaurants.save(new Restaurant(x.name(),x.address(),x.phone())));}
 public Restaurant get(UUID id){return restaurants.findById(id).orElseThrow(()->new NoSuchElementException("Restaurant not found: "+id));}
 public RestaurantResponse getResponse(UUID id){return RestaurantResponse.of(get(id));}
 public RestaurantResponse update(UUID id,RestaurantRequest x){Restaurant r=get(id);r.update(x.name(),x.address(),x.phone());return RestaurantResponse.of(r);}
 public void delete(UUID id){Restaurant r=get(id); if(!items.findByRestaurantId(id).isEmpty()) throw new IllegalStateException("Restaurant has menu items"); restaurants.delete(r);}
 public List<MenuItemResponse> menu(UUID id){get(id);return items.findByRestaurantId(id).stream().map(MenuItemResponse::of).toList();}
 public MenuItemResponse addItem(UUID id,MenuItemRequest x){return MenuItemResponse.of(items.save(new MenuItem(get(id),x.name(),x.description(),x.price())));}
 public MenuItemResponse updateItem(UUID rid,UUID iid,MenuItemRequest x){MenuItem m=item(rid,iid);m.update(x.name(),x.description(),x.price());return MenuItemResponse.of(m);}
 public void deleteItem(UUID rid,UUID iid){items.delete(item(rid,iid));}
 private MenuItem item(UUID rid,UUID iid){get(rid);return items.findByIdAndRestaurantId(iid,rid).orElseThrow(()->new NoSuchElementException("Menu item not found: "+iid));}
}
