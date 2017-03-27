
-- -----------------------------------------------------
-- Table `${deviceType}_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS ${deviceType}_DEVICE (
  ${deviceType}_DEVICE_ID VARCHAR(45) NOT NULL ,
  DEVICE_NAME VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (${deviceType}_DEVICE_ID) );

