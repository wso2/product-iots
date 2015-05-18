function onRequest(context){
    var dcProps = require('/config/dc-props.js').config();
    if (dcProps.ssoConfiguration.enabled) {
        response.sendRedirect(dcProps.appContext + "sso/login");
        exit();
    }else{
        context.loginPath = "api/user/login";
    }

    var constants = require("/modules/constants.js");
    var localLogoutURL = dcProps.appContext + "api/user/logout";
    var ssoLogoutURL = dcProps.appContext + "sso/logout";
    context.logoutURL = dcProps.ssoConfiguration.enabled? ssoLogoutURL : localLogoutURL;
    context.user = session.get(constants.USER_SESSION_KEY);

    context.viewonly = !context.user;

    return context;
}
