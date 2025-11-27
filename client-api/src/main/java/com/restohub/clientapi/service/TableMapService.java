package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.TableMapResponse;
import com.restohub.clientapi.entity.Floor;
import com.restohub.clientapi.entity.RestaurantTable;
import com.restohub.clientapi.entity.Room;
import com.restohub.clientapi.repository.FloorRepository;
import com.restohub.clientapi.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableMapService {
    
    private final FloorRepository floorRepository;
    private final TableRepository tableRepository;
    
    @Autowired
    public TableMapService(
            FloorRepository floorRepository,
            TableRepository tableRepository) {
        this.floorRepository = floorRepository;
        this.tableRepository = tableRepository;
    }
    
    public TableMapResponse getTableMap(Long restaurantId, Long floorId, Long roomId) {
        List<Floor> floors = floorRepository.findByRestaurantIdAndIsActiveTrue(restaurantId);
        
        if (floorId != null) {
            floors = floors.stream()
                    .filter(f -> f.getId().equals(floorId))
                    .collect(Collectors.toList());
        }
        
        List<TableMapResponse.FloorMapResponse> floorMaps = floors.stream()
                .sorted(Comparator.comparing(Floor::getFloorNumber))
                .map(floor -> {
                    List<Room> rooms = floor.getRooms().stream()
                            .filter(Room::getIsActive)
                            .collect(Collectors.toList());
                    
                    if (roomId != null) {
                        rooms = rooms.stream()
                                .filter(r -> r.getId().equals(roomId))
                                .collect(Collectors.toList());
                    }
                    
                    List<TableMapResponse.RoomMapResponse> roomMaps = rooms.stream()
                            .sorted(Comparator.comparing(Room::getName))
                            .map(room -> {
                                List<RestaurantTable> tables = tableRepository.findByRoomIdAndIsActiveTrue(room.getId());
                                
                                List<TableMapResponse.TableMapItemResponse> tableMaps = tables.stream()
                                        .sorted(Comparator.comparing(RestaurantTable::getTableNumber))
                                        .map(table -> TableMapResponse.TableMapItemResponse.builder()
                                                .id(table.getId())
                                                .tableNumber(table.getTableNumber())
                                                .capacity(table.getCapacity())
                                                .positionX1(table.getPositionX1())
                                                .positionY1(table.getPositionY1())
                                                .positionX2(table.getPositionX2())
                                                .positionY2(table.getPositionY2())
                                                .build())
                                        .collect(Collectors.toList());
                                
                                return TableMapResponse.RoomMapResponse.builder()
                                        .id(room.getId())
                                        .name(room.getName())
                                        .imageId(room.getImage() != null ? room.getImage().getId() : null)
                                        .tables(tableMaps)
                                        .build();
                            })
                            .collect(Collectors.toList());
                    
                    return TableMapResponse.FloorMapResponse.builder()
                            .id(floor.getId())
                            .floorNumber(floor.getFloorNumber())
                            .rooms(roomMaps)
                            .build();
                })
                .collect(Collectors.toList());
        
        return TableMapResponse.builder()
                .floors(floorMaps)
                .build();
    }
}

