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
@Table(name = "order_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private BigInteger orderDetailId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne
    @JoinColumn(name = "menu_size_id")
    private MenuSize menuSize;

    private Integer quantity;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "size_extra_price")
    private BigDecimal sizeExtraPrice;

    @Column(name = "topping_total_price")
    private BigDecimal toppingTotalPrice;

    @Column(name = "item_total_price")
    private BigDecimal itemTotalPrice;

    @ManyToMany
    @JoinTable(
            name = "order_detail_toppings",
            joinColumns = @JoinColumn(name = "order_detail_id"),
            inverseJoinColumns = @JoinColumn(name = "topping_id")
    )
    private Set<Topping> toppings = new HashSet<>();
}
