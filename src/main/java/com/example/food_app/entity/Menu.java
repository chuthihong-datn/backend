package com.example.food_app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "menus")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private BigInteger menuId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> images;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    private Integer amount;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "menu_toppings",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "topping_id")
    )
    private Set<Topping> toppings = new HashSet<>();

    @OneToMany(mappedBy = "menu",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<MenuSize> sizes = new ArrayList<>();

    @Column(name = "out_of_stock")
    private boolean outOfStock = false;

    public void addSize(MenuSize size) {
        sizes.add(size);
        size.setMenu(this);
    }

    private boolean isDeleted = false;
}