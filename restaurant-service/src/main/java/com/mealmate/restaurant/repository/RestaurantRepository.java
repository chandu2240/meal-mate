package com.mealmate.restaurant.repository;
import com.mealmate.restaurant.domain.Restaurant; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RestaurantRepository extends JpaRepository<Restaurant,UUID> {}
