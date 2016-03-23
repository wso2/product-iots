(function (server) {
    var log = new Log();
    var process = require("process")
    var login = function (url, username, password) {
        var res, options, payload,
            ws = require('ws'),
            client = new ws.WSRequest(),
            host = url.match(/.*:\/\/([^:\/]*)/)[1];

        options = {
            useSOAP: 1.2,
            useWSA: 1.0,
            action: 'urn:login'
        };

        payload =
            <aut:login xmlns:aut="http://authentication.services.core.carbon.wso2.org">
                <aut:username>{username}</aut:username>
                <aut:password>{password}</aut:password>
                <aut:remoteAddress>{host}</aut:remoteAddress>
            </aut:login>;

        try {
            client.open(options, url + '/services/AuthenticationAdmin', false);
            client.send(payload);
            res = client.responseXML;
            if (res.*::["return"].text() != 'true') {
                return false;
            }
            return client.getResponseHeader('Set-Cookie');
        } catch (e) {
            log.error(e.toString());
            throw new Error('Error while login to the server : ' + url + ', user : ' + username);
        }
    };

    var logout = function (url, cookie) {
        var options,
            ws = require('ws'),
            client = new ws.WSRequest();

        options = {
            useSOAP: 1.2,
            useWSA: 1.0,
            action: 'urn:logout',
            mep: 'in-only',
            HTTPHeaders: [
                { name: 'Cookie', value: cookie }
            ]
        };

        try {
            client.open(options, url + '/services/AuthenticationAdmin', false);
            client.send(null);
            return true;
        } catch (e) {
            log.error(e.toString());
            throw new Error('Error while logging out in server : ' + url + ', cookie : ' + cookie);
        }
    };

    var Cookie = function (cookie) {
        this.cookie = cookie;
    };

    server.Cookie = Cookie;

    var Server = function (options) {
        this.url = (options && options.url) ? options.url : 'local:/';
    };
    server.Server = Server;

    Server.prototype.authenticate = function (username, password) {
        var realm, user,
            carbon = require('carbon'),
            realmService = server.osgiService('org.wso2.carbon.user.core.service.RealmService');
        user = carbon.server.tenantUser(username);
        realm = realmService.getTenantUserRealm(user.tenantId);
        return realm.getUserStoreManager().authenticate(user.username, password);
    };

    Server.prototype.login = function (username, password) {
        var cookie = login(this.url, username, password);
        return new Cookie(cookie);
    };
    /*
      Description:- If the HostName is provided in carbon.xml it will be used to return the
      address with the valid port. If HostName is not mentioned -local ip will be used
      to return the address.
      Usage:- Scenario where the address of the server is required based on host or ip
      Parameters:- Transport is https or http.
    */
    server.address = function(transport){
        var  host = process.getProperty('server.host'),
        ip = process.getProperty('carbon.local.ip');
        var port;
        if(transport=="http"){
          port = process.getProperty('mgt.transport.http.proxyPort');
          if(!port){
            //can use http.port as well
            port = process.getProperty('mgt.transport.http.port');
          }
        }else if(transport=="https"){
          port = process.getProperty('mgt.transport.https.proxyPort');
          if(!port){
            //can use https.port as well
            port = process.getProperty('mgt.transport.https.port');
          }
        }
        var postUrl;
        if(host==null || host=="localhost"){
          postUrl  = transport+"://" + ip + ":" + port;
        }else{
          postUrl = transport+"://" + host+ ":" +port;
        }
        return postUrl;
    }


    Server.prototype.logout = function (cookie) {
        return logout(this.url, cookie.cookie);
    };
}(server));
