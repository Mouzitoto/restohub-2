--liquibase formatted sql

--changeset resto-hub:9
--comment: Add position coordinates for tables on room layout image
ALTER TABLE tables 
ADD COLUMN position_x1 DECIMAL(5,2);

ALTER TABLE tables 
ADD COLUMN position_y1 DECIMAL(5,2);

ALTER TABLE tables 
ADD COLUMN position_x2 DECIMAL(5,2);

ALTER TABLE tables 
ADD COLUMN position_y2 DECIMAL(5,2);
