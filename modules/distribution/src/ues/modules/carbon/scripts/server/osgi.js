var server = {};

(function (server) {
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext,
        Class = java.lang.Class;

    server.osgiService = function (clazz) {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(Class.forName(clazz));
    };
}(server));