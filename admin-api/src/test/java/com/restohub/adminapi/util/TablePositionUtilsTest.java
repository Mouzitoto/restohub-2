package com.restohub.adminapi.util;

import com.restohub.adminapi.dto.UpdateTablePositionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TablePositionUtilsTest {
    
    private TablePositionUtils utils;
    
    @BeforeEach
    void setUp() {
        utils = new TablePositionUtils();
    }
    
    @Test
    void testRectanglesIntersect_Overlapping() {
        // Прямоугольники пересекаются
        BigDecimal x1 = BigDecimal.valueOf(10);
        BigDecimal y1 = BigDecimal.valueOf(10);
        BigDecimal x2 = BigDecimal.valueOf(30);
        BigDecimal y2 = BigDecimal.valueOf(30);
        
        BigDecimal x3 = BigDecimal.valueOf(20);
        BigDecimal y3 = BigDecimal.valueOf(20);
        BigDecimal x4 = BigDecimal.valueOf(40);
        BigDecimal y4 = BigDecimal.valueOf(40);
        
        assertTrue(utils.rectanglesIntersect(x1, y1, x2, y2, x3, y3, x4, y4));
    }
    
    @Test
    void testRectanglesIntersect_NotOverlapping() {
        // Прямоугольники не пересекаются
        BigDecimal x1 = BigDecimal.valueOf(10);
        BigDecimal y1 = BigDecimal.valueOf(10);
        BigDecimal x2 = BigDecimal.valueOf(20);
        BigDecimal y2 = BigDecimal.valueOf(20);
        
        BigDecimal x3 = BigDecimal.valueOf(30);
        BigDecimal y3 = BigDecimal.valueOf(30);
        BigDecimal x4 = BigDecimal.valueOf(40);
        BigDecimal y4 = BigDecimal.valueOf(40);
        
        assertFalse(utils.rectanglesIntersect(x1, y1, x2, y2, x3, y3, x4, y4));
    }
    
    @Test
    void testRectanglesIntersect_Touching() {
        // Прямоугольники касаются (не пересекаются)
        BigDecimal x1 = BigDecimal.valueOf(10);
        BigDecimal y1 = BigDecimal.valueOf(10);
        BigDecimal x2 = BigDecimal.valueOf(20);
        BigDecimal y2 = BigDecimal.valueOf(20);
        
        BigDecimal x3 = BigDecimal.valueOf(20);
        BigDecimal y3 = BigDecimal.valueOf(10);
        BigDecimal x4 = BigDecimal.valueOf(30);
        BigDecimal y4 = BigDecimal.valueOf(20);
        
        assertFalse(utils.rectanglesIntersect(x1, y1, x2, y2, x3, y3, x4, y4));
    }
    
    @Test
    void testRectanglesIntersect_OneInsideAnother() {
        // Один прямоугольник внутри другого
        BigDecimal x1 = BigDecimal.valueOf(10);
        BigDecimal y1 = BigDecimal.valueOf(10);
        BigDecimal x2 = BigDecimal.valueOf(50);
        BigDecimal y2 = BigDecimal.valueOf(50);
        
        BigDecimal x3 = BigDecimal.valueOf(20);
        BigDecimal y3 = BigDecimal.valueOf(20);
        BigDecimal x4 = BigDecimal.valueOf(30);
        BigDecimal y4 = BigDecimal.valueOf(30);
        
        assertTrue(utils.rectanglesIntersect(x1, y1, x2, y2, x3, y3, x4, y4));
    }
    
    @Test
    void testCheckRectangleIntersections_NoIntersections() {
        List<UpdateTablePositionRequest> positions = new ArrayList<>();
        
        UpdateTablePositionRequest pos1 = new UpdateTablePositionRequest();
        pos1.setTableId(1L);
        pos1.setPositionX1(BigDecimal.valueOf(10));
        pos1.setPositionY1(BigDecimal.valueOf(10));
        pos1.setPositionX2(BigDecimal.valueOf(20));
        pos1.setPositionY2(BigDecimal.valueOf(20));
        positions.add(pos1);
        
        UpdateTablePositionRequest pos2 = new UpdateTablePositionRequest();
        pos2.setTableId(2L);
        pos2.setPositionX1(BigDecimal.valueOf(30));
        pos2.setPositionY1(BigDecimal.valueOf(30));
        pos2.setPositionX2(BigDecimal.valueOf(40));
        pos2.setPositionY2(BigDecimal.valueOf(40));
        positions.add(pos2);
        
        // Не должно быть исключения
        assertDoesNotThrow(() -> utils.checkRectangleIntersections(positions));
    }
    
    @Test
    void testCheckRectangleIntersections_WithIntersections() {
        List<UpdateTablePositionRequest> positions = new ArrayList<>();
        
        UpdateTablePositionRequest pos1 = new UpdateTablePositionRequest();
        pos1.setTableId(1L);
        pos1.setPositionX1(BigDecimal.valueOf(10));
        pos1.setPositionY1(BigDecimal.valueOf(10));
        pos1.setPositionX2(BigDecimal.valueOf(30));
        pos1.setPositionY2(BigDecimal.valueOf(30));
        positions.add(pos1);
        
        UpdateTablePositionRequest pos2 = new UpdateTablePositionRequest();
        pos2.setTableId(2L);
        pos2.setPositionX1(BigDecimal.valueOf(20));
        pos2.setPositionY1(BigDecimal.valueOf(20));
        pos2.setPositionX2(BigDecimal.valueOf(40));
        pos2.setPositionY2(BigDecimal.valueOf(40));
        positions.add(pos2);
        
        // Должно быть исключение
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> utils.checkRectangleIntersections(positions));
        assertEquals("TABLE_POSITIONS_INTERSECT", exception.getMessage());
    }
    
    @Test
    void testCheckRectangleIntersections_WithNullCoordinates() {
        List<UpdateTablePositionRequest> positions = new ArrayList<>();
        
        UpdateTablePositionRequest pos1 = new UpdateTablePositionRequest();
        pos1.setTableId(1L);
        pos1.setPositionX1(BigDecimal.valueOf(10));
        pos1.setPositionY1(BigDecimal.valueOf(10));
        pos1.setPositionX2(BigDecimal.valueOf(20));
        pos1.setPositionY2(BigDecimal.valueOf(20));
        positions.add(pos1);
        
        UpdateTablePositionRequest pos2 = new UpdateTablePositionRequest();
        pos2.setTableId(2L);
        // Координаты null - должно быть пропущено
        positions.add(pos2);
        
        // Не должно быть исключения
        assertDoesNotThrow(() -> utils.checkRectangleIntersections(positions));
    }
    
    @Test
    void testRectanglesIntersect_ReversedCoordinates() {
        // Тест с обратными координатами (x2 < x1)
        BigDecimal x1 = BigDecimal.valueOf(30);
        BigDecimal y1 = BigDecimal.valueOf(30);
        BigDecimal x2 = BigDecimal.valueOf(10);
        BigDecimal y2 = BigDecimal.valueOf(10);
        
        BigDecimal x3 = BigDecimal.valueOf(20);
        BigDecimal y3 = BigDecimal.valueOf(20);
        BigDecimal x4 = BigDecimal.valueOf(40);
        BigDecimal y4 = BigDecimal.valueOf(40);
        
        assertTrue(utils.rectanglesIntersect(x1, y1, x2, y2, x3, y3, x4, y4));
    }
}

