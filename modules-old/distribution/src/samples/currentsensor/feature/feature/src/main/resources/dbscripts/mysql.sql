-- -----------------------------------------------------
--                  Agent Database
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `currentsensor_DEVICE` (
  `currentsensor_DEVICE_ID` VARCHAR(45) NOT NULL ,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`currentsensor_DEVICE_ID`) )
ENGINE = InnoDB;




