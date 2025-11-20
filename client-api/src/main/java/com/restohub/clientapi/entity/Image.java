package com.restohub.clientapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "images")
@Getter
@Setter
public class Image extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "image_data", nullable = false, columnDefinition = "BYTEA")
    private byte[] imageData;
    
    @Column(name = "preview_data", nullable = false, columnDefinition = "BYTEA")
    private byte[] previewData;
    
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
}

