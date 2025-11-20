package com.restohub.adminapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "floors", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"restaurant_id", "floor_number"})
})
@Getter
@Setter
public class Floor extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @Column(name = "floor_number", nullable = false, length = 50)
    private String floorNumber;
    
    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL)
    private List<Room> rooms;
}

