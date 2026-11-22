package com.mealmate.availability.repository;
import com.mealmate.availability.domain.Reservation; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ReservationRepository extends JpaRepository<Reservation,UUID> { List<Reservation> findByOrderId(UUID id); boolean existsByOrderId(UUID id); }
