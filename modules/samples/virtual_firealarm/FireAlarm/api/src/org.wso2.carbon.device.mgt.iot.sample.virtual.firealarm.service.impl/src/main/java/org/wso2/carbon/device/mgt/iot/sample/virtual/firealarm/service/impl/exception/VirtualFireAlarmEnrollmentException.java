package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.exception;

public class VirtualFireAlarmEnrollmentException extends Exception {
    private static final long serialVersionUID = 118512086957330189L;

    public VirtualFireAlarmEnrollmentException(String errorMessage) {
        super(errorMessage);
    }

    public VirtualFireAlarmEnrollmentException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }
}
