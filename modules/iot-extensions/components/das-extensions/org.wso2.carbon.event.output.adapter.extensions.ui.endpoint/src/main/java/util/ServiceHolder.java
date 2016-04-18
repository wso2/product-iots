package util;


import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.extensions.ui.UIOutputCallbackControllerService;

public class ServiceHolder {

    private static ServiceHolder instance;
    private UIOutputCallbackControllerService uiOutputCallbackControllerService;

    private ServiceHolder(){
        uiOutputCallbackControllerService = (UIOutputCallbackControllerService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getOSGiService(UIOutputCallbackControllerService.class, null);
    }

    public synchronized static ServiceHolder getInstance(){
        if (instance==null){
            instance= new ServiceHolder();
        }
        return instance;
    }

    public UIOutputCallbackControllerService getUiOutputCallbackControllerService() {
        return uiOutputCallbackControllerService;
    }
}
