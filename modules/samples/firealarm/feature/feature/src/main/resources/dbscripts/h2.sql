
-- -----------------------------------------------------
--  Agent Database
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `firealarm_DEVICE` (
  `firealarm_DEVICE_ID` VARCHAR(45) NOT NULL ,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  `ACCESS_TOKEN` VARCHAR(50) NOT NULL,
  `REFRESH_TOKEN` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`firealarm_DEVICE_ID`) );



