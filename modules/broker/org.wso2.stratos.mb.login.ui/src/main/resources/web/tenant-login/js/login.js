function loginPreCheck() {
    var userName = document.getElementById("txtUserName").value;
    var errorMsgEle = document.getElementById("errorMsg");
    errorMsgEle.innerHTML = "";
    if (userName != null) {
        atIndex = userName.lastIndexOf('@');
        if (atIndex == -1) {
            // if there are no '@'s we are not validating the username
            return;
        }
    }

    var tenantDomain = "";
    var tenantDomainEle = document.getElementById("tenantDomain");
    if (tenantDomainEle.innerHTML != "") {
        return;
    }
    tenantDomain = getDomainFromUserName();
    if (tenantDomain == null || tenantDomain == "") {
        // tenant 0, we are submitting without rename check
        return;
    }
    
    var busyCheck = document.getElementById("busyCheck");
    busyCheck.innerHTML = "<img src=\"../tenant-login/images/ajax-loader.gif\"/>";
    
    new Ajax.Request('../tenant-login/domain_rename_checker_ajaxprocessor.jsp',
    {
        method:'post',
        parameters: {domain: tenantDomain},

        onSuccess: function(transport) {
            busyCheck.innerHTML = "";
            var returnValue = transport.responseText;
            if (returnValue.search(/----success----/) != -1) {
                // nothing much needed to be done
            } else if (returnValue.search(/----trial----/) != -1) {
                // need to redirect to the trial domain

                var newTenantDomain = tenantDomain + "-trial";
                msg = "The domain name of your account is renamed to " + newTenantDomain + " "  +
                      "as the ownership of the domain is not confirmed. You can login to the account under the " +
                      "changed domain name and confirm the ownership of your domain from the 'Account management' page.";
                var exitCode = function() {
                            var username = getTenantAwareUserName();
                            var newUsername = username + "@" + newTenantDomain;
                            document.getElementById("txtUserName").value = newUsername;
                        };
                CARBON.showWarningDialog(msg, exitCode, exitCode);
            } else {
                // some error just show no messages
                msg = "";
                errorMsgEle.innerHTML = msg;
            }
        },

        onFailure: function(transport){
            busyCheck.innerHTML = "";
        }
    });
}

function getDomainFromUserName() {
    var tenantDomain = "";
    var userName = document.getElementById("txtUserName").value;
    if (userName != null) {
        atIndex = userName.lastIndexOf('@');
        if (atIndex != -1) {
            tenantDomain = userName.substring(atIndex + 1, userName.length);
        }
    }
    return tenantDomain;
}


function getTenantAwareUserName() {
    var userName = document.getElementById("txtUserName").value;
    if (userName != null) {
        atIndex = userName.lastIndexOf('@');
        if (atIndex != -1) {
            userName = userName.substring(0, atIndex);
        }
    }
    return userName;
}



