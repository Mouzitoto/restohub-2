package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import com.restohub.adminapi.util.TablePositionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    
    @Mock
    private RoomRepository roomRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private FloorRepository floorRepository;
    
    @Mock
    private ImageRepository imageRepository;
    
    @Mock
    private TableRepository tableRepository;
    
    @Mock
    private ImageService imageService;
    
    @Mock
    private TableService tableService;
    
    @Mock
    private TablePositionUtils tablePositionUtils;
    
    @InjectMocks
    private RoomService roomService;
    
    private Restaurant restaurant;
    private Floor floor;
    private Room room;
    private Image image;
    private RestaurantTable table;
    
    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setIsActive(true);
        
        floor = new Floor();
        floor.setId(1L);
        floor.setRestaurant(restaurant);
        floor.setFloorNumber("1");
        floor.setIsActive(true);
        
        image = new Image();
        image.setId(1L);
        image.setIsActive(true);
        
        room = new Room();
        room.setId(1L);
        room.setFloor(floor);
        room.setName("Test Room");
        room.setImage(image);
        room.setIsActive(true);
        
        table = new RestaurantTable();
        table.setId(1L);
        table.setRoom(room);
        table.setTableNumber("1");
        table.setCapacity(4);
        table.setPositionX1(BigDecimal.valueOf(10));
        table.setPositionY1(BigDecimal.valueOf(10));
        table.setPositionX2(BigDecimal.valueOf(20));
        table.setPositionY2(BigDecimal.valueOf(20));
        table.setIsActive(true);
    }
    
    @Test
    void testGetRoomLayout_Success() {
        // Arrange
        when(roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(room));
        when(tableRepository.findByRoomIdAndIsActiveTrue(1L))
                .thenReturn(Arrays.asList(table));
        
        TableResponse tableResponse = new TableResponse();
        tableResponse.setId(1L);
        tableResponse.setPositionX1(BigDecimal.valueOf(10));
        tableResponse.setPositionY1(BigDecimal.valueOf(10));
        tableResponse.setPositionX2(BigDecimal.valueOf(20));
        tableResponse.setPositionY2(BigDecimal.valueOf(20));
        
        when(tableService.getTable(1L, 1L))
                .thenReturn(tableResponse);
        
        // Act
        RoomLayoutResponse response = roomService.getRoomLayout(1L, 1L);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getRoom());
        assertNotNull(response.getTables());
        assertEquals(1, response.getTables().size());
        assertNotNull(response.getImageUrl());
        verify(roomRepository, times(1)).findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L);
        verify(tableRepository, times(1)).findByRoomIdAndIsActiveTrue(1L);
    }
    
    @Test
    void testUpdateTablePositions_Success() {
        // Arrange
        UpdateTablePositionRequest request = new UpdateTablePositionRequest();
        request.setTableId(1L);
        request.setPositionX1(BigDecimal.valueOf(15));
        request.setPositionY1(BigDecimal.valueOf(15));
        request.setPositionX2(BigDecimal.valueOf(25));
        request.setPositionY2(BigDecimal.valueOf(25));
        
        List<UpdateTablePositionRequest> requests = Arrays.asList(request);
        
        when(roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(room));
        when(tableRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(table));
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenReturn(table);
        
        TableResponse tableResponse = new TableResponse();
        tableResponse.setId(1L);
        when(tableService.getTable(1L, 1L))
                .thenReturn(tableResponse);
        
        doNothing().when(tablePositionUtils).checkRectangleIntersections(any());
        
        // Act
        List<TableResponse> response = roomService.updateTablePositions(1L, 1L, requests);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(tablePositionUtils, times(1)).checkRectangleIntersections(any());
        verify(tableRepository, times(1)).save(any(RestaurantTable.class));
    }
    
    @Test
    void testUpdateTablePositions_TableNotInRoom() {
        // Arrange
        Room otherRoom = new Room();
        otherRoom.setId(2L);
        table.setRoom(otherRoom);
        
        UpdateTablePositionRequest request = new UpdateTablePositionRequest();
        request.setTableId(1L);
        request.setPositionX1(BigDecimal.valueOf(15));
        request.setPositionY1(BigDecimal.valueOf(15));
        request.setPositionX2(BigDecimal.valueOf(25));
        request.setPositionY2(BigDecimal.valueOf(25));
        
        List<UpdateTablePositionRequest> requests = Arrays.asList(request);
        
        when(roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(room));
        when(tableRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(table));
        
        doNothing().when(tablePositionUtils).checkRectangleIntersections(any());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> roomService.updateTablePositions(1L, 1L, requests));
        assertEquals("TABLE_NOT_IN_ROOM", exception.getMessage());
    }
    
    @Test
    void testUploadRoomImage_ClearsTablePositions() throws Exception {
        // Arrange
        ImageResponse imageResponse = new ImageResponse();
        imageResponse.setId(2L);
        
        Image newImage = new Image();
        newImage.setId(2L);
        newImage.setIsActive(true);
        
        when(roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(room));
        when(imageService.uploadImage(any()))
                .thenReturn(imageResponse);
        when(imageRepository.findByIdAndIsActiveTrue(2L))
                .thenReturn(Optional.of(newImage));
        when(roomRepository.save(any(Room.class)))
                .thenReturn(room);
        
        doNothing().when(tableService).clearTablePositionsForRoom(1L);
        
        // Act
        roomService.uploadRoomImage(1L, 1L, null);
        
        // Assert
        verify(tableService, times(1)).clearTablePositionsForRoom(1L);
    }
    
    @Test
    void testDeleteRoomImage_ClearsTablePositions() {
        // Arrange
        when(roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class)))
                .thenReturn(room);
        
        doNothing().when(tableService).clearTablePositionsForRoom(1L);
        
        // Act
        roomService.deleteRoomImage(1L, 1L);
        
        // Assert
        verify(tableService, times(1)).clearTablePositionsForRoom(1L);
    }
    
    @Test
    void testUpdateRoom_ImageChanged_ClearsTablePositions() {
        // Arrange
        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setImageId(2L);
        
        Image newImage = new Image();
        newImage.setId(2L);
        newImage.setIsActive(true);
        
        when(roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(room));
        when(imageRepository.findByIdAndIsActiveTrue(2L))
                .thenReturn(Optional.of(newImage));
        when(roomRepository.save(any(Room.class)))
                .thenReturn(room);
        
        doNothing().when(tableService).clearTablePositionsForRoom(1L);
        
        // Act
        roomService.updateRoom(1L, 1L, request);
        
        // Assert
        verify(tableService, times(1)).clearTablePositionsForRoom(1L);
    }
}

