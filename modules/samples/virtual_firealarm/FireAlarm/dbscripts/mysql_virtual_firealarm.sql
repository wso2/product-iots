-- -----------------------------------------------------
-- Table `FIREALARM_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `VIRTUAL_FIREALARM_DEVICE` (
  `VIRTUAL_FIREALARM_DEVICE_ID` VARCHAR(45) NOT NULL ,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`VIRTUAL_FIREALARM_DEVICE_ID`) )
ENGINE = InnoDB;




