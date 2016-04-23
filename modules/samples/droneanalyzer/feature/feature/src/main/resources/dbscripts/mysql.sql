-- -----------------------------------------------------
-- Table `DRONE_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `DRONE_DEVICE` (
  `DRONE_DEVICE_ID` VARCHAR(45) NOT NULL ,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`DRONE_DEVICE_ID`) )
ENGINE = InnoDB;




