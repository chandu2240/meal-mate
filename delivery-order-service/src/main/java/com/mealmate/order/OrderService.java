package com.mealmate.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
class OrderService {
    final OrderRepository r;
    final KafkaTemplate<String,Object> k;
    final RestTemplate w = new RestTemplate();
    final String url, token;

    OrderService(OrderRepository r, KafkaTemplate<String,Object> k,
                 @Value("${services.availability-url}") String url,
                 @Value("${internal.token}") String token) {
        this.r = r; this.k = k; this.url = url; this.token = token;
    }

    OrderModel create(CreateRequest q, String key) {
        var existing = r.findByIdempotencyKey(key);
        if (existing.isPresent()) return existing.get();
        var order = r.save(new OrderModel(q.customerId(), q.restaurantId(), q.failPayment(), q.failDeliveryAssignment(), key));
        boolean reserved = false;
        try {
            var headers = new HttpHeaders();
            headers.set("X-Internal-Token", token);
            var reservation = new AvailabilityReservation(order.getId(), q.items());
            w.postForEntity(url + "/availability/reservations", new HttpEntity<>(reservation, headers), Void.class);
            reserved = true;
            k.send("food-order-events", order.getId().toString(),
                    new Event(UUID.randomUUID().toString(), "FoodOrderCreated", order.getId(), q.failPayment(), q.failDeliveryAssignment()));
        } catch (Exception ex) {
            if (reserved) releaseReservation(order.getId());
            order.setStatus(OrderStatus.REJECTED);
            r.save(order);
            System.err.println("FoodOrderRejected order=" + order.getId() + " reason=" + ex.getMessage());
        }
        return order;
    }

    private void releaseReservation(UUID orderId) {
        try {
            var headers = new HttpHeaders();
            headers.set("X-Internal-Token", token);
            w.exchange(
                    url + "/availability/reservations/" + orderId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    Void.class);
        } catch (Exception ex) {
            System.err.println("Reservation compensation failed order=" + orderId + " reason=" + ex.getMessage());
        }
    }

    OrderModel get(UUID id) { return r.findById(id).orElseThrow(); }

    OrderModel cancel(UUID id) {
        var order = get(id);
        order.setStatus(OrderStatus.CANCELLED);
        releaseReservation(id);
        return r.save(order);
    }

    record AvailabilityReservation(UUID orderId, List<Item> items) {}
    record Event(String eventId, String type, UUID orderId, boolean failPayment, boolean failDeliveryAssignment) {}
}
