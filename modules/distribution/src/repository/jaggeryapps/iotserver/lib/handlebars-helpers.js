var log = new Log('fuse.handlebars');
//TODO: create a different set of helpers for init parsing

var Handlebars = require('handlebars-v2.0.0.js').Handlebars;
var USER_SESSION_KEY = "USER";
var getScope = function (unit,configs) {
    var jsFile = fuse.getFile(unit, '', '.js');
    var templateConfigs = configs || {};
    var script;
    var onRequestCb = function(){}; //Assume that onRequest function will not be defined by the user
    var viewModel = {};
    var cbResult;
    if (jsFile.isExists()) {
        script = require(jsFile.getPath());
        //Eagerly make the viewModel the template configs
        viewModel = templateConfigs;
        //Check if the unit author has specified an onRequest
        //callback
        if(script.hasOwnProperty('onRequest')){
            script.app = {
                url: '/' + fuseState.appName,
                publicURL: '/' + fuseState.appName + '/public/' + unit,
                "class": unit + '-unit'
            };
            onRequestCb = script.onRequest;
            cbResult = onRequestCb(templateConfigs);
            log.debug("passing configs to unit "+unit+" configs: "+stringify(templateConfigs));
            //If the execution does not yield an object we will print
            //a warning as the unit author may have forgotten to return a data object
            if(cbResult===undefined){
                cbResult = {}; //Give an empty data object
                //log.warn('[' + requestId + '] unit "' + unit + '" has a onRequest method which does not return a value.This may lead to the '
                //    +'unit not been rendered correctly.');
            }
            viewModel = cbResult;
        }
    }
    else{
        //If there is no script then the view should get the configurations
        //passed in the unit call
        viewModel = templateConfigs;
    }
    viewModel.app = {
        url: '/' + fuseState.appName
    };
    viewModel.self = {
        publicURL: '/' + fuseState.appName + '/public/' + unit,
        "class": unit + '-unit'
    };
    return viewModel;
};

Handlebars.innerZones = [];
Handlebars.innerZonesFromUnit = null;

Handlebars.registerHelper('defineZone', function (zoneName, zoneContent) {
    var result = '';
    var zone = Handlebars.Utils.escapeExpression(zoneName);
    fuseState.currentZone.push(zone);
    var unitsToRender = fuseState.zones[zone] || [];

    if (Handlebars.innerZones.length > 0) {
        unitsToRender = fuseState.zones[Handlebars.innerZones[0]] || [];
    }

    // if there is no one overriding, then display inline zone
    if (zoneContent['fn'] && unitsToRender.length == 0) {
        return zoneContent.fn(this).trim();
    }

    for (var i = 0; i < unitsToRender.length; i++) {
        var unit = unitsToRender[i];
        if (Handlebars.innerZonesFromUnit == null || Handlebars.innerZonesFromUnit.unitName == unit.unitName) {
            var template = fuse.getFile(unit.originUnitName || unit.unitName, '', '.hbs');
            //log.debug('[' + requestId + '] for zone "' + zone + '" including template :"' + template.getPath() + '"');
            result += Handlebars.compileFile(template)(getScope(unit.unitName, zoneContent.data.root));
        }
    }

    // we go to inner zones if result is empty, what we should really do it
    // if matched zone is fully made of sub-zones. this is a hack to
    // make it easy to implement.
    if (result.trim().length == 0 && zoneContent['fn']) {
        Handlebars.innerZones.push(zoneName);
        for (i = 0; i < unitsToRender.length; i++) {
            unit = unitsToRender[i];
            Handlebars.innerZonesFromUnit = unit;
            result += zoneContent.fn(this).trim();
            Handlebars.innerZonesFromUnit = null;
        }
        Handlebars.innerZones.pop();
        return result;
    }

    fuseState.currentZone.pop();
    return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('zone', function (zoneName, zoneContent) {
    var currentZone = fuseState.currentZone[fuseState.currentZone.length - 1];
    if (currentZone == null) {
        return 'zone_' + zoneName + ' ';
    }

    // if it's exact zone match or if any in inner zone matches we render zone.
    // this second condition is a hack. what we should really do is to keep another stack,
    // and only match with the peek of that stack and always fill it with next in innerZone stack.
    if (zoneName == currentZone || Handlebars.innerZones.indexOf(zoneName) >= 0) {
        return zoneContent.fn(this).trim();
    } else {
        return '';
    }
});

Handlebars.registerHelper('layout', function (layoutName) {
    var currentZone = fuseState.currentZone[fuseState.currentZone.length - 1];
    if (currentZone == null) {
        return 'layout_' + layoutName;
    } else {
        return '';
    }
});

Handlebars.registerHelper('authorized', function () {
    var currentZone = fuseState.currentZone[fuseState.currentZone.length - 1];
    if (currentZone == null) {
        return '';
    } else {
        var loggedUser = session.get(USER_SESSION_KEY);
        if(loggedUser == null){
            response.sendRedirect("/"+ fuseState.appName + "/login");
            exit();
        }
    }
});

Handlebars.registerHelper('unit', function (unitName,options) {
    var unitDef = fuse.getUnitDefinition(unitName);
    var baseUnit = null;
    var templateConfigs = options.hash || {};
    for (var i = 0; i < unitDef.zones.length; i++) {
        var zone = unitDef.zones[i];
        if (zone.name == 'main') {
            baseUnit = zone.origin;
        } else {
            var golbalZone = fuseState.zones[zone.name];
            if (!golbalZone) {
                fuseState.zones[zone.name] = [{"unitName": unitName}];
            } else {
                fuseState.zones[zone.name].push({"unitName": unitName});
            }
        }
    }
    if (baseUnit == null) {
        log.error('unit does not have a main zone');
    }
    //TODO warn when unspecified decencies are included.
    fuseState.currentZone.push('main');
    var template = fuse.getFile(baseUnit, '', '.hbs');
    //log.debug('[' + requestId + '] including "' + baseUnit + '"'+" with configs "+stringify(templateConfigs));
    var result = new Handlebars.SafeString(Handlebars.compileFile(template)(getScope(baseUnit,templateConfigs)));
    fuseState.currentZone.pop();
    return result;
});

Handlebars.compileFile = function (file) {
    //TODO: remove this overloaded argument
    var f = (typeof file === 'string') ? new File(file) : file;

    if (!Handlebars.cache) {
        Handlebars.cache = {};
    }

    if (Handlebars.cache[f.getPath()] != null) {
        return Handlebars.cache[f.getPath()];
    }

    f.open('r');
    //log.debug('[' + requestId + '] reading file "' + f.getPath() + '"');
    var content = f.readAll().trim();
    f.close();
    var compiled = Handlebars.compile(content);
    Handlebars.cache[f.getPath()] = compiled;
    return compiled;
};
Handlebars.registerHelper('equal', function(lvalue, rvalue, options) {
    if (arguments.length < 3)
        throw new Error("Handlebars Helper equal needs 2 parameters");
    if( lvalue!=rvalue ) {
        return options.inverse(this);
    } else {
        return options.fn(this);
    }
});
Handlebars.registerHelper('unequal', function(lvalue, rvalue, options) {
    if (arguments.length < 3)
        throw new Error("Handlebars Helper equal needs 2 parameters");
    if( lvalue == rvalue ) {
        return options.inverse(this);
    } else {
        return options.fn(this);
    }
});
