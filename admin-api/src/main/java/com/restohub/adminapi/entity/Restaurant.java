package com.restohub.adminapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
public class Restaurant extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "phone", length = 50)
    private String phone;
    
    @Column(name = "whatsapp", length = 50)
    private String whatsapp;
    
    @Column(name = "instagram")
    private String instagram;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "working_hours", columnDefinition = "TEXT")
    private String workingHours;
    
    @Column(name = "manager_language_code", nullable = false, length = 10)
    private String managerLanguageCode = "ru";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_image_id")
    private Image logoImage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bg_image_id")
    private Image bgImage;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Floor> floors;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<MenuItem> menuItems;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Promotion> promotions;
}

