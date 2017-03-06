
-- -----------------------------------------------------
--  Agent Database
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `geolocationTracker_DEVICE` (
  `geolocationTracker_DEVICE_ID` VARCHAR(45) NOT NULL ,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`geolocationTracker_DEVICE_ID`) );



