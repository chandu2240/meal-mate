package com.mealmate.availability.repository;
import com.mealmate.availability.domain.Availability; import jakarta.persistence.LockModeType; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface AvailabilityRepository extends JpaRepository<Availability,UUID> { Optional<Availability> findByMenuItemId(UUID id); @Lock(LockModeType.PESSIMISTIC_WRITE) Optional<Availability> findLockedByMenuItemId(UUID id); }
