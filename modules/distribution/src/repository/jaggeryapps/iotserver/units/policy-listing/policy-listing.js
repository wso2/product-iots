function onRequest(context) {
    // var log = new Log("policy-listing");
    var policyModule = require("/modules/policy.js").policyModule;
    var allPolicies = policyModule.getPolicies();
    //log.info((allPolicies));
    if (!allPolicies || allPolicies.length == 0) {
        context.policies = [];
        context.listPolicyStatus = "Oops, Sorry, No other Policies found.";
    } else {
        var i, filteredPoliciesList = [];
        for (i = 0; i < allPolicies.length; i++) {
            filteredPoliciesList.push(allPolicies[i]);
        }
        //log.info(filteredPoliciesList.length);
        context.policies = filteredPoliciesList;
        context.listPolicyStatus = "Total number of Policies found : " + filteredPoliciesList.length;
    }
    //context.permissions = policyModule.getUIPermissions();
    return context;
}