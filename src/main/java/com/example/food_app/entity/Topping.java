package com.example.food_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "toppings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Topping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topping_id")
    private BigInteger toppingId;

    private String name;

    private BigDecimal price;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "toppings")
    private Set<OrderDetail> orderDetails = new HashSet<>();

    @ManyToMany(mappedBy = "toppings")
    private Set<Menu> menus = new HashSet<>();

    @Column(name = "out_of_stock")
    private boolean outOfStock = false;
}
