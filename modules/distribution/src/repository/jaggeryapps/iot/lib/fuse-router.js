//public function declarations
var route;

(function () {

    //public
    /**
     * front controller entity point. acts as the main function for every request.
     */
    route = function () {
        //lets assume URL looks like https://my.domain.com/app/{one}/{two}/{three}/{four}
        var uri = request.getRequestURI(); // = app/{one}/{two}/{three}/{four}
        var parts = splitFirst(uri);
        fuseState.appName = parts.head;
        var path = parts.tail; // = /{one}/{two}/{three}/{four}
        var handled = false;

        parts = splitFirst(path);
        if (parts.head == 'public') { // {one} == 'public'
            parts = splitFirst(parts.tail);
            if (splitFirst(parts.tail).head == 'less') { // {three} == 'less'
                handled = renderLess(parts.head, parts.tail);  // renderLess({two},{three}/{four})
            } else {
                handled = renderStatic(parts.head, parts.tail);
            }
        } else {
            handled = renderPage(path);
            if (!handled) {
                handled = renderUnit(path);
            }
        }

        if (!handled) {
            response.sendError(404, 'Requested resource not found');
        }

    };


    //private
    var log = new Log('fuse.router');

    var getMime = function (path) {
        var index = path.lastIndexOf('.') + 1;
        var knowMime = {
            'js': 'application/javascript',
            'html': 'text/html',
            'htm': 'text/html',
            'woff': 'application/x-font-woff',
            "png": "image/png",
            "css": "text/css",
            "hbs": "text/x-handlebars-template",
            "apk": "application/vnd.android.package-archive",
            "ipa": "application/octet-stream"
        };
        var mime;
        if (index >= 0) {
            mime = knowMime[path.substr(index)];
        }
        return mime || 'text/plain';
    };

    /**
     * '/a/b/c/d' -> {'a','b/c/d'}
     * @param path URI part, should start with '/'
     * @returns {{head: string, tail: string}}
     */
    var splitFirst = function (path) {
        var firstSlashPos = path.indexOf('/', 1);
        var head = path.substring(1, firstSlashPos);
        var tail = path.substring(firstSlashPos);
        return {head: head, tail: tail};
    };

    /**
     * @param str
     * @param prefix
     * @returns {boolean} true iif str starts with prefix
     */
    var startsWith = function (str, prefix) {
        return (str.lastIndexOf(prefix, 0) === 0);
    };

    var renderStatic = function (unit, path) {
        var unitModel = null;
        var unitName = "";
        var parts = (unit + path).split("/");

        //'unitName' name can be "unitA" or "categoryA/unitA" or "categoryA/categoryAb/unitB"...etc
        //incrementally trying to resolve a unit using url path parts.
        for (var i = 0; i < parts.length; i++) {

            if (unitName == "") {
                unitName = parts[i];
            } else {
                unitName += "/" + parts[i];
            }

            try {
                var model = fuse.getUnitDefinition(unitName);
                if (model) {
                    unitModel = {
                        name: model.name,
                        path: parts.splice(i + 1).join("/")
                    };
                    break;
                }
            } catch (err) {
                //unit not found, ignore error
            }
        }

        if (unitModel == null) {
            throw '[' + requestId + '] unit "' + unit + path + '" does not exits';
        }

        var staticFile = fuse.getFile(unitModel.name, 'public' + "/" + unitModel.path);

        if (staticFile.isExists() && !staticFile.isDirectory()) {
            response.addHeader('Content-type', getMime(path));
            response.addHeader('Cache-Control', 'public,max-age=12960000');
            staticFile.open('r');
            var stream = staticFile.getStream();
            print(stream);
            staticFile.close();
            return true;
        }
        return false;
    };

    var renderPage = function (path) {
        var jagFile;
        if (path.indexOf('/', path.length - 1) !== -1) {
            jagFile = new File('/pages' + path + 'index.jag');
        } else {
            jagFile = new File('/pages' + path + '.jag');
        }
        if (jagFile.isExists()) {
            include(jagFile.getPath());
            return true;
        }else{
            return false;
        }
    };

    var renderUnit = function (path) {
        var mainUnit = null;
        var matchedUnits = fuse.getMatchedUnitDefinitions();
        fuse.addDependencies(matchedUnits);
        var zones = fuseState.zones;

        // A map of maps. this is used to ensure same zone is not render twice in to same definition.
        // zonesAdded = { titleZone : { zoneFromA : true, zoneFromB : true } }
        var zonesAdded = {};

        for (var i = 0; i < matchedUnits.length; i++) {
            var definition = matchedUnits[i];

            for (var j = 0; j < definition.zones.length; j++) {
                var zone = definition.zones[j];
                if (!zones[zone.name]) {
                    zones[zone.name] = [];
                    zonesAdded[zone.name] = {};
                }
                var zoneKey = zone.origin + ':' + zone.name; // temp unique key to identify zone form a given unit.
                if (!zonesAdded[zone.name][zoneKey]) {
                    var zoneInfo = {unitName: definition.name};
                    if (zone.origin != definition.name) {
                        zoneInfo.originUnitName = zone.origin;
                    }
                    zones[zone.name].push(zoneInfo);
                    zonesAdded[zone.name][zoneKey] = true;
                }
            }

        }

        var layout = fuseState.layout;
        if (layout !== null) {
            log.debug(
                '[' + requestId + '] request for "' + path + '" will be rendered using layout "' +
                layout + '" (defined in "' + mainUnit + '") and zones ' +
                stringify(zones)
            );

            var output = handlebars.Handlebars.compileFile(fuse.getLayoutPath(layout))({});
            response.addHeader('Content-type', 'text/html');
            print(output);
            return true;
        } else {
            log.debug(
                '[' + requestId + '] request for "' + path + '" will can\'t be rendered, since no layout is defined' +
                'in any of the units ' + stringify(zones));
            return false;
        }
    };

    function fileToString(path) {
    }

    /**
     * convert less file to css and print to output. add '?nocache=true' to force regenerate.
     * @param unit name of the unit
     * @param path the path to the less file relative to unit root (should start with slash)
     * @returns {boolean} is successfully rendered.
     */
    function renderLess(unit, path) {
        //TODO: fix - incorrect less files makes it respond the old less even if it is nocahce.
        log.debug('[' + requestId + '] for unit "' + unit + '" a request received for a less file "' + path + '"');
        var cacheKey = '/tmp/cached_' + unit + path.replace(/[^\w\.-]/g, '_');
        fuseState.currentUnit = unit;
        var cachedCss = new File(cacheKey);

        //TODO: move this check to caller function ??
        if (fuseDebug || request.getParameter('nocache') == 'true' || !cachedCss.isExists()) {
            var parts = splitFirst(path);
            var lessPath = '/public/less' + parts.tail.replace(/\.css$/, '') + '.less';
            var lessFile = fuse.getFile(unit, lessPath);

            if (lessFile.isExists()) {
                var x = require('less-rhino-1.7.5.js');
                x.compile([lessFile.getPath(), cacheKey]);
                log.debug('[' + requestId + '] for unit "' + unit + '" request for "' + path + '" is cached as "' + cacheKey + '"');
            }
        }


        if (cachedCss.isExists()) {
            response.addHeader('Content-type', 'text/css');
            response.addHeader('Cache-Control', 'public,max-age=12960000');
            cachedCss.open('r');
            var stream = cachedCss.getStream();
            print(stream);
            cachedCss.close();
            return true;
        }
        return false;

    }


})();
