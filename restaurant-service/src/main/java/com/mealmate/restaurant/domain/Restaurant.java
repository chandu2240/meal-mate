package com.mealmate.restaurant.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="restaurants")
public class Restaurant {
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @Column(nullable=false) private String name;
  private String address;
  private String phone;
  protected Restaurant() {}
  public Restaurant(String name,String address,String phone){this.name=name;this.address=address;this.phone=phone;}
  public UUID getId(){return id;} public String getName(){return name;} public String getAddress(){return address;} public String getPhone(){return phone;}
  public void update(String name,String address,String phone){this.name=name;this.address=address;this.phone=phone;}
}
