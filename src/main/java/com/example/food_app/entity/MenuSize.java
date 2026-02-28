package com.example.food_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Entity
@Table(name = "menu_sizes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_size_id")
    private BigInteger menuSizeId;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Column(name = "size_name")
    private String sizeName;

    @Column(name = "extra_price")
    private BigDecimal extraPrice;
}
