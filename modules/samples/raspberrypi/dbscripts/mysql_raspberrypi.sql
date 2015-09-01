-- -----------------------------------------------------
-- Table `RASPBERRYPI_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `RASPBERRYPI_DEVICE` (
  `RASPBERRYPI_DEVICE_ID` VARCHAR(45) NOT NULL ,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`RASPBERRYPI_DEVICE_ID`) )
ENGINE = InnoDB;




