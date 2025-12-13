package com.restohub.clientapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_smoking", nullable = false)
    private Boolean isSmoking = false;
    
    @Column(name = "is_outdoor", nullable = false)
    private Boolean isOutdoor = false;
    
    @Column(name = "is_live_music", nullable = false)
    private Boolean isLiveMusic = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<RestaurantTable> tables;
}

