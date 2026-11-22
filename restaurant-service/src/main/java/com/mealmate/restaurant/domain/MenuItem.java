package com.mealmate.restaurant.domain;

import jakarta.persistence.*;
import java.math.BigDecimal; import java.util.UUID;

@Entity @Table(name="menu_items")
public class MenuItem {
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="restaurant_id",nullable=false) private Restaurant restaurant;
  @Column(nullable=false) private String name; private String description;
  @Column(nullable=false, precision=12, scale=2) private BigDecimal price;
  protected MenuItem() {}
  public MenuItem(Restaurant r,String n,String d,BigDecimal p){restaurant=r;name=n;description=d;price=p;}
  public UUID getId(){return id;} public Restaurant getRestaurant(){return restaurant;} public String getName(){return name;} public String getDescription(){return description;} public BigDecimal getPrice(){return price;}
  public void update(String n,String d,BigDecimal p){name=n;description=d;price=p;}
}
