package com.restohub.clientapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@jakarta.persistence.Table(name = "tables", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "table_number"})
})
@Getter
@Setter
public class RestaurantTable extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    
    @Column(name = "table_number", nullable = false, length = 50)
    private String tableNumber;
    
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;
    
    @Column(name = "deposit_amount")
    private String depositAmount;
    
    @Column(name = "deposit_note", columnDefinition = "TEXT")
    private String depositNote;
}

