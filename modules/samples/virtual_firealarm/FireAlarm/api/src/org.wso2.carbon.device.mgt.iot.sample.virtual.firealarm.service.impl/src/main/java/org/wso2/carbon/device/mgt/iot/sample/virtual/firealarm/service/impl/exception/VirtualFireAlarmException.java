package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.exception;

public class VirtualFireAlarmException extends Exception {
    private static final long serialVersionUID = 118512086957330189L;

    public VirtualFireAlarmException(String errorMessage) {
        super(errorMessage);
    }

    public VirtualFireAlarmException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }
}
