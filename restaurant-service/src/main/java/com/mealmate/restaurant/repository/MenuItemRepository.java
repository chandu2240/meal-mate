package com.mealmate.restaurant.repository;
import com.mealmate.restaurant.domain.MenuItem; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface MenuItemRepository extends JpaRepository<MenuItem,UUID> { List<MenuItem> findByRestaurantId(UUID restaurantId); Optional<MenuItem> findByIdAndRestaurantId(UUID id,UUID restaurantId); }
