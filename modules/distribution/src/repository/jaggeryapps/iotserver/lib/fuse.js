//public function declarations
var getHbsFile, getFile, toRelativePath, cleanupAncestors,
    getUnitPath, getMatchedUnitDefinitions, getZoneDefinition, getUnitDefinition,
    getUnitDefinitions, getLayoutPath, readUnitDefinitions;

(function () {
    //private
    var log = new Log('fuse.core');
    var lookUpTable = null;
    var definitions = null;

    var initLookUp = function (definitions) {
        if (lookUpTable === null) {
            lookUpTable = {};
            for (var i = 0; i < definitions.length; i++) {
                var definition = definitions[i];
                lookUpTable[definition.name] = i;
            }
        }
    };

    var isMatched = function (definition, layout) {
        var urlMatch = function (pattern) {
            var uriMatcher = new URIMatcher(request.getRequestURI());
            return Boolean(uriMatcher.match('/{appName}' + pattern));
        };
        var permission = function (permissionStr) {
            var carbonModule = require("carbon");
            var carbonServer = application.get("carbonServer");
            var carbonUser = session.get("USER");
            if (carbonUser) {
                var userManager = new carbonModule.user.UserManager(carbonServer, carbonUser.tenantId);
                var user = new carbonModule.user.User(userManager, carbonUser.username);
                return user.isAuthorized(permissionStr, "ui.execute");
            }
            return false;
        };
        var config = {'theme': 'default'};
        var predicateStr = definition.definition.predicate;
        if (predicateStr) {
            var js = 'function(config,urlMatch,permission,layout){ return ' + predicateStr + ';}';
            return Boolean(eval(js)(config, urlMatch,permission, layout ? layout : NaN));
        }
        return false;
    };

    var getAncestorModels = function (unit) {
        var unitModel = getUnitDefinition(unit);
        var ancestors = [unitModel];
        var parentName;
        while ((parentName = unitModel.definition.extends) != null) {
            unitModel = getUnitDefinition(parentName);
            ancestors.push(unitModel);
        }
        return ancestors;
    };

    addDependencies = function (unitModels) {
        var resolved = {};
        for (var i = 0; i < unitModels.length; i++) {
            resolved[unitModels[i].name] = true;
        }

        for (i = 0; i < unitModels.length; i++) {
            var unitModel = unitModels[i];
            var dependencies = unitModel.definition.dependencies;
            if (dependencies) {
                for (var j = 0; j < dependencies.length; j++) {
                    var dependencyName = dependencies[j];
                    unitModels.push(getUnitDefinition(dependencyName));
                    resolved[dependencyName] = true;
                }
            }
        }


    };


    //public
    getMatchedUnitDefinitions = function () {
        //TODO: return map not list
        var unitDefinitions = getUnitDefinitions();
        var matched = [];
        var unMatched = [];
        var layout = null;
        var mainUnit = null;

        var addToMatched = function (model) {
            matched.push(model);
            if (model.layout) {
                if (layout == null) {
                    layout = model.layout;
                    mainUnit = model.name;
                } else {
                    log.warn(
                        '[' + requestId + '] multiple layouts ' + mainUnit + ':' +
                        layout + ' vs ' + model.name + ':' + model.layout
                    );
                }
            }
        };


        // first pass
        for (var i = 0; i < unitDefinitions.length; i++) {
            var unitDefinition = unitDefinitions[i];
            if (isMatched(unitDefinition)) {
                addToMatched(unitDefinition);
            } else {
                unMatched.push(unitDefinition);
            }
        }

        fuseState.layout = layout;

        // second pass : we have to do this two passes since we don't know the layout
        // first time around
        if (layout) {
            for (i = 0; i < unMatched.length; i++) {
                unitDefinition = unMatched[i];
                if (isMatched(unitDefinition, layout)) {
                    addToMatched(unitDefinition)
                }
            }
        }


        var toDelete = [];

        for (i = 0; i < matched.length; i++) {
            var ancestors = getAncestorModels(matched[i].name);
            for (var j = 1; j < ancestors.length; j++) {
                var ancestor = ancestors[j];
                toDelete.push(ancestor.name);
            }
        }


        for (i = matched.length - 1; i >= 0; i--) {
            //log.info(matched[i].name);
            if (toDelete.indexOf(matched[i].name) >= 0) {
                matched.splice(i, 1);
            }
        }

        return matched;
    };

    getUnitDefinition = function (unit) {
        var definitions = getUnitDefinitions();
        initLookUp(definitions);
        var model = definitions[lookUpTable[unit]];
        if (!model) {
            log.warn('[' + requestId + '] unit "' + unit + '" does not exits');
            throw '[' + requestId + '] unit "' + unit + '" does not exits';
        }
        return model;
    };

    var flattenAllInheritance = function (unitModels) {
        var hasFlattend = {};
        for (var i = 0; i < unitModels.length; i++) {
            var model = unitModels[i];
            if (!hasFlattend[model]) {
                var ancestors = getAncestorModels(model.name);
                for (var j = ancestors.length - 1; j >= 1; j--) {
                    flattenInheritance(ancestors[j], ancestors[j - 1]);
                }
            }
        }
    };

    var flattenInheritance = function (parent, child) {
        var parentZones = parent.zones;
        for (var i = 0; i < parentZones.length; i++) {
            var parentZone = parentZones[i];
            child.zones.push(parentZone);
        }
    };

    getUnitDefinitions = function () {
        if (definitions !== null) {
            return definitions;
        } else {
            definitions = [];
        }

        var unitDirs = new File('/units').listFiles();

        definitions = readUnitDefinitions("",unitDirs,definitions);
        //log.info(definitions);

        addPageUnitDefinitions(definitions);

        initLookUp(definitions);
        flattenAllInheritance(definitions);

        return definitions;
    };

    readUnitDefinitions = function(basePath, unitDirs, definitions){
        for (var i = 0; i < unitDirs.length; i++) {

            var unitDir = unitDirs[i];
            if (unitDir.isDirectory()) {
                var unitName = unitDir.getName();
                //log.info("reading: "+unitName + " basePath:"+basePath);
                var definitionFile = new File(fuse.getUnitPath(basePath+unitName) + '/' + unitName + '.json');

                if(definitionFile.isExists()) {
                    var unitModel = {
                        name: unitName,
                        path: unitDir.getPath()
                    };
                    if(basePath!=""){
                        unitModel.name = basePath + unitName;
                    }

                    var path = definitionFile.getPath();
                    log.debug('[' + requestId + '] reading file "' + path + '"');
                    unitModel.definition = require(path);

                    // add the information derived by parsing hbs file to the same model
                    var hbsMetadata = getHbsMetadata(unitModel);
                    unitModel.zones = hbsMetadata.zones;
                    if (hbsMetadata.layout) {
                        unitModel.layout = hbsMetadata.layout;
                    }

                    definitions.push(unitModel);

                }else{
                    var unitSubDirs = new File(fuse.getUnitPath(basePath+"/"+unitName)).listFiles();
                    readUnitDefinitions(basePath+unitName+"/",unitSubDirs,definitions);
                }
            }
        }
        return definitions;
    };

    addPageUnitDefinitions = function (unitModels, dir) {
        var pageFiles = new File(dir || '/pages').listFiles();
        for (var i = 0; i < pageFiles.length; i++) {
            var pageFile = pageFiles[i];
            var fileName = pageFile.getName();
            if (pageFile.isDirectory()) {
                addPageUnitDefinitions(unitModels, pageFile.getPath())
            } else if (fileName.indexOf('.hbs', fileName.length - 4) !== -1) { // File name ends with '.hbs'

                var isLeaf = true;
                //path relative to app root
                var relativePath = pageFile.getPath()
                    .substring(6 + pageFile.getPath().indexOf('/pages/'), pageFile.getPath().length - 4);

                if (relativePath.match(/\/index$/)) {
                    relativePath = relativePath.replace(/\/index$/, '');
                    var parentFile = new File(pageFile.getPath().substr(0, pageFile.getPath().lastIndexOf('/')));
                    var hasSiblings = parentFile.listFiles().length != 1;
                    if (hasSiblings) {
                        isLeaf = false;
                    }
                }

                //this will be used as a name for the virtual unit, useful for debugging purposes.
                var unitName = (relativePath == '' ? 'index' : relativePath.substr(1).replace(/\//, '-') ) + '-page';

                var predicate = "urlMatch('" + relativePath + "')";
                // leaf is page that can handle multiple URLs. in this case it should have a wildcard at end.
                // but since our current matcher doesn't support {/wildcard*} patten, "OR" ( || ) is used
                if (isLeaf) {
                    predicate += " || urlMatch('" + relativePath + "/{+wildcard}')";
                }
                var unitModel = {
                    name: unitName,

                    path: pageFile.getPath(),
                    definition: {predicate: predicate}
                };
                var hbsMetadata = getHbsMetadata(unitModel);
                unitModel.zones = hbsMetadata.zones;
                if (hbsMetadata.layout) {
                    unitModel.layout = hbsMetadata.layout;
                }

                unitModels.push(unitModel);
            }
        }
    };


    getLayoutPath = function (layout) {
        return '/layouts/' + layout + '.hbs';
    };

    getHbsFile = function (unit) {
        // we determining if it's page unit or a proper unit
        // by checking if path ends with '.hbs'
        // TODO: improve getFile to do include  this logic
        if (unit.path.indexOf('.hbs', unit.path.length - 4) !== -1) {
            return new File(unit.path);
        } else {
            if(unit.name.indexOf('/') !== -1){//a subcategory unit
                var rawParts = unit.name.split("/");
                return new File(unit.path + '/' + rawParts[rawParts.length-1] + '.hbs');
            }else {
                return new File(unit.path + '/' + unit.name + '.hbs');
            }
        }
    };

    var getHbsMetadata = function (unit) {
        var zoneDef = {'zones': []};
        var hbsFile = getHbsFile(unit);
        if (!hbsFile.isExists()) {
            log.error("Couldn't find .hbs file at: `" + unit.path + "`");
            return zoneDef;
        }
        var output = handlebars.Handlebars.compileFile(hbsFile)({});
        var zonesAndLayouts = output.trim().split(/\s+/gm);
        for (var i = 0; i < zonesAndLayouts.length; i++) {
            var name = zonesAndLayouts[i];
            if (name.lastIndexOf('zone_', 0) === 0) {
                zoneDef.zones.push({name: name.substr(5), origin: unit.name});
            } else if (name.lastIndexOf('layout_', 0) === 0) {
                zoneDef.layout = name.substr(7);
            }
        }
        return zoneDef;
    };


    getUnitPath = function (unit) {
        return '/units/' + unit;
    };

    cleanupAncestors = function (units) {
        var toDelete = {};
        var len = units.length;
        for (var i = 0; i < len; i++) {
            var unit = units[i];
            if (!toDelete[unit]) {
                var ancestors = getAncestorModels(unit.name);
                for (var j = 1; j < ancestors.length; j++) {
                    toDelete[ancestors[j].name] = unit;
                }
            }
        }
        while (len--) {
            if (toDelete[units[len]]) {
                log.debug(
                    '[' + requestId + '] unit "' + units[len] +
                    '" is overridden by "' + toDelete[units[len]] + '"'
                );
                units.splice(len, 1);
            }
        }
    };

    toRelativePath = function (path) {
        var start = 0;
        if (path.lastIndexOf('/units/', 0) == 0) {
            start = 7; // len('/units/')
        }
        var slashPos = path.indexOf('/', 7);
        return {
            unit: path.substring(start, slashPos),
            path: path.substr(slashPos)
        }
    };

    /**
     * Get a file inside a unit by relative path. if the file is not available in the given unit,
     * the closest ancestor's file will be returned. if an optional suffix is used the relative path is
     * calculated as ( path + < unit name > + opt_suffix ). if no such a file exists a returned file object will
     * point to provided unit's non-existing file location (not to any ancestors).
     *
     * @param unitName name of the unit
     * @param path path relative to unit root.
     * @param opt_suffix
     * @returns {File}
     */
    getFile = function (unitName, path, opt_suffix) {
        var slashPath = ((path[0] === '/') ? '' : '/') + path;
        var selfFileName = '';
        var fileName = '';
        if (opt_suffix) {
            if(unitName.indexOf('/') !== -1) {//a subcategory unit
                var rawParts = unitName.split("/");
                selfFileName = rawParts[rawParts.length - 1];
            }else {
                selfFileName = unitName;
            }
            selfFileName = selfFileName + opt_suffix;
            slashPath = slashPath + ((slashPath[slashPath.length - 1] === '/') ? '' : '/');
        }

        //TODO: remove this hack that makes in page-unit, any file is same
        var unitDef = getUnitDefinition(unitName);
        if (unitDef.path.indexOf('.hbs', unitDef.path.length - 4) !== -1) {
            if (opt_suffix.indexOf('.hbs', opt_suffix.length - 4) !== -1) {
                return new File(unitDef.path);
            } else {
                return new File(unitDef.path.replace(/.hbs$/, opt_suffix));
            }
        }

        var selfFile = new File(getUnitPath(unitName) + slashPath + selfFileName);
        if (selfFile.isExists()) {
            log.debug(
                '[' + requestId + '] for unit "' + unitName + '" file resolved : "'
                + slashPath + selfFileName + '" -> "' + selfFile.getPath() + '"'
            );

            return selfFile;
        }

        var ancestors = getAncestorModels(unitName);
        for (var i = 1; i < ancestors.length; i++) {
            var ancestorName = ancestors[i].name;
            if(ancestorName.indexOf('/') !== -1) {//a subcategory unit
                var rawParts = ancestorName.split("/");
                ancestorName = rawParts[rawParts.length - 1];
            }
            if (opt_suffix) {
                fileName = ancestorName + opt_suffix;
            }
            var file = new File(getUnitPath(ancestorName) + slashPath + fileName);
            if (file.isExists()) {
                log.debug(
                    '[' + requestId + '] for unit "' + unitName + '" file resolved : "'
                    + slashPath + selfFileName + '" -> "' + file.getPath() + '"'
                );
                return file;
            }
        }
        log.debug(
            '[' + requestId + '] for unit "' + unitName + '" (non-excising) file resolved : "'
            + slashPath + selfFileName + '" -> "' + selfFile.getPath() + '"'
        );
        return selfFile;
    };

})();