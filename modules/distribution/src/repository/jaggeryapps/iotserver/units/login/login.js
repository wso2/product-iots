function onRequest(context){
    var dcProps = require('/config/dc-props.js').config();
    if (dcProps.ssoConfiguration.enabled) {
        response.sendRedirect(dcProps.appContext + "sso/login");
        exit();
    }else{
        context.loginPath = "api/user/login";
    }
    return context;
}
