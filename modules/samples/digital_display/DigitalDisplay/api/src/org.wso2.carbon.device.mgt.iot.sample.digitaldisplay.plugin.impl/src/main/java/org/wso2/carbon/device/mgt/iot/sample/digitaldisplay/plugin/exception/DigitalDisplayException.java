package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.plugin.exception;

/**
 * Created by nuwan on 12/2/15.
 */
public class DigitalDisplayException extends Exception {

    private static final long serialVersionUID = 2736466230451105441L;

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public DigitalDisplayException(String msg, DigitalDisplayException nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public DigitalDisplayException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public DigitalDisplayException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public DigitalDisplayException() {
        super();
    }

    public DigitalDisplayException(Throwable cause) {
        super(cause);
    }


}
