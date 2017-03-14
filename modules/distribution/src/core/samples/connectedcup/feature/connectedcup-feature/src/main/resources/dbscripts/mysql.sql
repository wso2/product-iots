-- -----------------------------------------------------
-- Table `CONNECTED_CUP_DEVICE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `CONNECTED_CUP_DEVICE` (
  `CONNECTED_CUP_DEVICE_ID` VARCHAR(45) NOT NULL ,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`CONNECTED_CUP_DEVICE_ID`) )
ENGINE = InnoDB;




