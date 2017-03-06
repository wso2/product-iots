
-- -----------------------------------------------------
-- Table `CONNECTEDLAP_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS CONNECTEDLAP_DEVICE (
  CONNECTEDLAP_DEVICE_ID VARCHAR(45) NOT NULL ,
  DEVICE_NAME VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (CONNECTEDLAP_DEVICE_ID) );

