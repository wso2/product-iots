(function (server) {
    var log = new Log(),
        PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext,
        MultitenantConstants = Packages.org.wso2.carbon.utils.multitenancy.MultitenantConstants,
        MultitenantUtils = Packages.org.wso2.carbon.utils.multitenancy.MultitenantUtils,
        realmService = server.osgiService('org.wso2.carbon.user.core.service.RealmService'),
        tenantManager = realmService.getTenantManager();

    server.tenantDomain = function (options) {
        if (!options) {
            return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        if (options.tenantId) {
            return tenantManager.getDomain(options.tenantId);
        }
        if (options.username) {
            return MultitenantUtils.getTenantDomain(options.username);
        }
        if (options.url) {
            return MultitenantUtils.getTenantDomainFromRequestURL(options.url);
        }
        return null;
    };

    server.tenantId = function (options) {
        var domain = options ? (options.domain || server.tenantDomain(options)) : server.tenantDomain();
        return domain ? tenantManager.getTenantId(domain) : null;
    };

    server.tenantUser = function (username) {
        var domain = server.tenantDomain({
                username: username
            }),
            id = server.tenantId({
                domain: domain
            });
        username = MultitenantUtils.getTenantAwareUsername(username);
        return {
            domain: domain,
            username: username,
            tenantId: id
        };
    };

    server.superTenant = {
        tenantId: MultitenantConstants.SUPER_TENANT_ID,
        domain: MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
    };

    server.sandbox = function (options, fn) {
        var context,
            PrivilegedCarbonContext = org.wso2.carbon.context.PrivilegedCarbonContext;
        PrivilegedCarbonContext.startTenantFlow();
        log.debug('startTenantFlow');
        try {
            context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            context.setTenantDomain(server.tenantDomain({
                tenantId: options.tenantId
            }));
            context.setTenantId(options.tenantId);
            context.setUsername(options.username || null);
            return fn();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
            log.debug('endTenantFlow');
        }
    };

}(server));