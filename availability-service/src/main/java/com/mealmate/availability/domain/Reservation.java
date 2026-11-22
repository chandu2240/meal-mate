package com.mealmate.availability.domain;
import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="availability_reservations",uniqueConstraints=@UniqueConstraint(columnNames={"order_id","menu_item_id"}))
public class Reservation { @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(name="order_id",nullable=false) private UUID orderId; @Column(name="menu_item_id",nullable=false) private UUID menuItemId; @Column(nullable=false) private int quantity; protected Reservation(){} public Reservation(UUID o,UUID m,int q){orderId=o;menuItemId=m;quantity=q;} public UUID getOrderId(){return orderId;} public UUID getMenuItemId(){return menuItemId;} public int getQuantity(){return quantity;} }
