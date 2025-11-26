package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.CreateTableRequest;
import com.restohub.adminapi.dto.UpdateTableRequest;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {
    
    @Mock
    private TableRepository tableRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private RoomRepository roomRepository;
    
    @Mock
    private FloorRepository floorRepository;
    
    @Mock
    private ImageRepository imageRepository;
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private BookingStatusRepository bookingStatusRepository;
    
    @Mock
    private ImageService imageService;
    
    @InjectMocks
    private TableService tableService;
    
    private Restaurant restaurant;
    private Floor floor;
    private Room room;
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
        
        room = new Room();
        room.setId(1L);
        room.setFloor(floor);
        room.setName("Test Room");
        room.setIsActive(true);
        
        table = new RestaurantTable();
        table.setId(1L);
        table.setRoom(room);
        table.setTableNumber("1");
        table.setCapacity(4);
        table.setIsActive(true);
        table.setCreatedAt(LocalDateTime.now());
        table.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testCreateTable_WithCoordinates() {
        // Arrange
        CreateTableRequest request = new CreateTableRequest();
        request.setRoomId(1L);
        request.setTableNumber("1");
        request.setCapacity(4);
        request.setPositionX1(BigDecimal.valueOf(10));
        request.setPositionY1(BigDecimal.valueOf(10));
        request.setPositionX2(BigDecimal.valueOf(20));
        request.setPositionY2(BigDecimal.valueOf(20));
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(roomRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(room));
        when(tableRepository.findByRoomIdAndTableNumberAndIsActiveTrue(1L, "1"))
                .thenReturn(Optional.empty());
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(invocation -> {
                    RestaurantTable t = invocation.getArgument(0);
                    t.setId(1L);
                    return t;
                });
        
        // Act
        var response = tableService.createTable(1L, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(10), response.getPositionX1());
        assertEquals(BigDecimal.valueOf(10), response.getPositionY1());
        assertEquals(BigDecimal.valueOf(20), response.getPositionX2());
        assertEquals(BigDecimal.valueOf(20), response.getPositionY2());
        verify(tableRepository, times(1)).save(any(RestaurantTable.class));
    }
    
    @Test
    void testUpdateTable_WithCoordinates() {
        // Arrange
        UpdateTableRequest request = new UpdateTableRequest();
        request.setPositionX1(BigDecimal.valueOf(15));
        request.setPositionY1(BigDecimal.valueOf(15));
        request.setPositionX2(BigDecimal.valueOf(25));
        request.setPositionY2(BigDecimal.valueOf(25));
        
        when(tableRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(table));
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenReturn(table);
        
        // Act
        var response = tableService.updateTable(1L, 1L, request);
        
        // Assert
        assertNotNull(response);
        verify(tableRepository, times(1)).save(any(RestaurantTable.class));
    }
    
    @Test
    void testClearTablePositionsForRoom() {
        // Arrange
        RestaurantTable table1 = new RestaurantTable();
        table1.setId(1L);
        table1.setRoom(room);
        table1.setPositionX1(BigDecimal.valueOf(10));
        table1.setPositionY1(BigDecimal.valueOf(10));
        table1.setPositionX2(BigDecimal.valueOf(20));
        table1.setPositionY2(BigDecimal.valueOf(20));
        
        RestaurantTable table2 = new RestaurantTable();
        table2.setId(2L);
        table2.setRoom(room);
        table2.setPositionX1(BigDecimal.valueOf(30));
        table2.setPositionY1(BigDecimal.valueOf(30));
        table2.setPositionX2(BigDecimal.valueOf(40));
        table2.setPositionY2(BigDecimal.valueOf(40));
        
        List<RestaurantTable> tables = Arrays.asList(table1, table2);
        
        when(tableRepository.findByRoomIdAndIsActiveTrue(1L))
                .thenReturn(tables);
        when(tableRepository.saveAll(any()))
                .thenReturn(tables);
        
        // Act
        tableService.clearTablePositionsForRoom(1L);
        
        // Assert
        assertNull(table1.getPositionX1());
        assertNull(table1.getPositionY1());
        assertNull(table1.getPositionX2());
        assertNull(table1.getPositionY2());
        assertNull(table2.getPositionX1());
        assertNull(table2.getPositionY1());
        assertNull(table2.getPositionX2());
        assertNull(table2.getPositionY2());
        verify(tableRepository, times(1)).saveAll(any());
    }
    
    @Test
    void testClearTablePosition() {
        // Arrange
        table.setPositionX1(BigDecimal.valueOf(10));
        table.setPositionY1(BigDecimal.valueOf(10));
        table.setPositionX2(BigDecimal.valueOf(20));
        table.setPositionY2(BigDecimal.valueOf(20));
        
        when(tableRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.of(table));
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenReturn(table);
        
        // Act
        tableService.clearTablePosition(1L, 1L);
        
        // Assert
        assertNull(table.getPositionX1());
        assertNull(table.getPositionY1());
        assertNull(table.getPositionX2());
        assertNull(table.getPositionY2());
        verify(tableRepository, times(1)).save(any(RestaurantTable.class));
    }
    
    @Test
    void testClearTablePosition_NotFound() {
        // Arrange
        when(tableRepository.findByIdAndRestaurantIdAndIsActiveTrue(1L, 1L))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> tableService.clearTablePosition(1L, 1L));
        assertEquals("TABLE_NOT_FOUND", exception.getMessage());
    }
}

