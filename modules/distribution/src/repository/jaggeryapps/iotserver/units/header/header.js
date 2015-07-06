function onRequest(context){
    var constants = require("/modules/constants.js");
    var dcProps = require('/config/dc-props.js').config();
    var localLogoutURL = dcProps.appContext + "api/user/logout";
    var ssoLogoutURL = dcProps.appContext + "sso/logout";
    context.logoutURL = dcProps.ssoConfiguration.enabled? ssoLogoutURL : localLogoutURL;
    context.user = session.get(constants.USER_SESSION_KEY);
    context.homeLink = "/iotserver";
    return context;
}
