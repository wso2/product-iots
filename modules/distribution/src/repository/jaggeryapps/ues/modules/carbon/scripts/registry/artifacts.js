(function (server, registry) {

    var log = new Log();

    var GenericArtifactManager = Packages.org.wso2.carbon.governance.api.generic.GenericArtifactManager;
    var GenericArtifactFilter = Packages.org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
    var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
    var QName = Packages.javax.xml.namespace.QName;
    var IOUtils = Packages.org.apache.commons.io.IOUtils;
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext; //Used regard tenant details
    var CarbonContext = Packages.org.wso2.carbon.context.CarbonContext;
    var MultitenantConstants = Packages.org.wso2.carbon.utils.multitenancy.MultitenantConstants;
    var List = java.util.List;
    var Map = java.util.Map;
    var ArrayList = java.util.ArrayList;
    var HashMap = java.util.HashMap;

    var GovernanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;//Used to obtain Asset Types
    var DEFAULT_MEDIA_TYPE = 'application/vnd.wso2.registry-ext-type+xml';//Used to obtain Asset types
    var PaginationContext = Packages.org.wso2.carbon.registry.core.pagination.PaginationContext;//Used for pagination on register

    var REGISTRY_ABSOLUTE_PATH = "/_system/governance";

    var HISTORY_PATH_SEPERATOR = '_';
    var ASSET_PATH_SEPERATOR = '/';
    var lcHistoryRegExpression = new RegExp(ASSET_PATH_SEPERATOR, 'g');
    var HISTORY_PATH = '/_system/governance/_system/governance/repository/components/org.wso2.carbon.governance/lifecycles/history/';


    var buildArtifact = function (manager, artifact) {
        return {
            id: String(artifact.id),
            type: String(manager.type),
            path: "/_system/governance" + String(artifact.getPath()),
            lifecycle: artifact.getLifecycleName(),
            lifecycleState: artifact.getLifecycleState(),
            mediaType: String(artifact.getMediaType()),
            attributes: (function () {
                var i, name,
                    names = artifact.getAttributeKeys(),
                    length = names.length,
                    attributes = {};
                for (i = 0; i < length; i++) {
                    name = names[i];

                    var data = artifact.getAttributes(name);

                    //Check if there is only one element
                    if (data.length == 1) {
                        attributes[name] = String(artifact.getAttribute(name));
                    }
                    else {
                        attributes[name] = data;
                    }
                }
                return attributes;
            }()),
            content: function () {
                return new Stream(new ByteArrayInputStream(artifact.getContent()));
            }
        };
    };

    var createArtifact = function (manager, options) {
        var name, attribute, i, length, lc,
            artifact = manager.newGovernanceArtifact(new QName(options.name)),
            attributes = options.attributes;
        for (name in attributes) {
            if (attributes.hasOwnProperty(name)) {
                attribute = attributes[name];
                if (attribute instanceof Array) {
                    /*length = attribute.length;
                     for (i = 0; i < length; i++) {
                     artifact.addAttribute(name, attribute[i]);
                     }*/
                    artifact.setAttributes(name, attribute);
                } else {
                    artifact.setAttribute(name, attribute);
                }
            }
        }
        if (options.id) {
            artifact.id = options.id;
        }
        if (options.content) {
            if (options.content instanceof Stream) {
                artifact.setContent(IOUtils.toByteArray(options.content.getStream()));
            } else {
                artifact.setContent(new java.lang.String(options.content).getBytes());
            }
        }
        lc = options.lifecycles;
        if (lc) {
            length = lc.length;
            for (i = 0; i < length; i++) {
                artifact.attachLifeCycle(lc[i]);
            }
        }
        return artifact;
    };

    var ArtifactManager = function (registry, type) {
        this.registry = registry;
        this.manager = new GenericArtifactManager(registry.registry.getChrootedRegistry("/_system/governance"), type);
        this.type = type;
    };
    registry.ArtifactManager = ArtifactManager;

    ArtifactManager.prototype.find = function (fn, paging) {
        var i, length, artifacts,
            artifactz = [];
        artifacts = this.manager.findGenericArtifacts(new GenericArtifactFilter({
            matches: function (artifact) {
                return fn(buildArtifact(this, artifact));
            }
        }));
        length = artifacts.length;
        for (i = 0; i < length; i++) {
            artifactz.push(buildArtifact(this, artifacts[i]));
        }
        return artifactz;
    };


    /*
     * this funtion is used ArtifactManager find with map for query for solr basicly
     * query - for maping attribute of resource
     * pagin - pagination details
     * return - list of artifacts under the seach request
     *
     */
    ArtifactManager.prototype.search = function (query, paging) {

        var list, map, key, artifacts, pagination, value, that,
            artifactz = [];
        pagination = generatePaginationForm(paging);
        try {
            PaginationContext.init(pagination.start, pagination.count, pagination.sortOrder,
                pagination.sortBy, pagination.paginationLimit);
            map = HashMap();
            //case senstive search as it using greg with solr 1.4.1
            if (!query) {
                //listing for sorting
                map = java.util.Collections.emptyMap();
            } else if (query instanceof String || typeof query === 'string') {
                list = new ArrayList();
                list.add('*' + query + '*');
                map.put('overview_name', list);
            } else {
                //support for only on name of attribut -
                for (key in query) {
                    // if attribute is string values
                    if (query.hasOwnProperty(key)) {
                        value = query[key];
                        list = new ArrayList();
                        if (value instanceof Array) {
                            value.forEach(function (val) {
                                //solr config update need have '*' as first char in below line
                                //check life_cycle state
                                list.add(key == 'lcState' ? val : '*' + val + '*');
                            });
                        } else {
                            //solr config update need have '*' as first char in below line
                            list.add(key == 'lcState' ? value : '*' + value + '*');
                        }
                        map.put(key, list);
                    }
                }//end of attribut looping (all attributes)
            }
            artifacts = this.manager.findGenericArtifacts(map);
            that = this;
            artifacts.forEach(function (artifact) {
                artifactz.push(buildArtifact(that, artifact));
            });
        } finally {
            PaginationContext.destroy();
        }
        return artifactz;
    };

    ArtifactManager.prototype.get = function (id) {
        return buildArtifact(this, this.manager.getGenericArtifact(id))
    };

    ArtifactManager.prototype.count = function () {
        return this.manager.getAllGenericArtifactIds().length;
    };

    /**
     * @deprecated Please use search method instead
     * @param paging
     * @return {*}
     */
    ArtifactManager.prototype.list = function (paging) {
        return this.search(null, paging);
    };

    /*
     The function returns an array of asset types
     @mediaType - The media type of the assets
     @return An array of strings containing the asset paths
     */
    ArtifactManager.prototype.getAssetTypePaths = function (mediaType) {

        //Use the default media type if one is not provided
        if (!mediaType) {
            mediaType = DEFAULT_MEDIA_TYPE;
        }

        //var assetArray=GovernanceUtils.findGovernanceArtifacts(mediaType,this.registry);
        var result = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.findGovernanceArtifacts(mediaType, registry.registry);

        return result;
        //Create an empty array if no asset types are found
        //return (!assetArray)?[]:assetArray;
    };

    /*
     {
     name: 'AndroidApp1',
     attributes: {
     overview_status: "CREATED",
     overview_name: 'AndroidApp1',
     overview_version: '1.0.0',
     overview_url: 'http://overview.com',
     overview_provider: 'admin',
     images_thumbnail: 'http://localhost:9763/portal/gadgets/co2-emission/thumbnail.jpg',
     images_banner: 'http://localhost:9763/portal/gadgets/electric-power/banner.jpg'
     },
     lifecycles : ['lc1', 'lc2'],
     content : '<?xml ....>'
     }
     */
    ArtifactManager.prototype.add = function (options) {
        var asset=createArtifact(this.manager, options);
        this.manager.addGenericArtifact(asset);
        return asset.getId();
    };

    ArtifactManager.prototype.update = function (options) {
        this.manager.updateGenericArtifact(createArtifact(this.manager, options));
    };

    ArtifactManager.prototype.remove = function (id) {
        this.manager.removeGenericArtifact(id);
    };

    /*
     Attaches the provided lifecycle name to the artifact
     @lifecycleName: The name of a valid lifecycle.The lifecycle should be visible to the
     registry.
     @options: The artifact to which the life cycle must be attached.
     */
    ArtifactManager.prototype.attachLifecycle = function (lifecycleName, options) {
        var artifact = getArtifactFromImage(this.manager, options);
        if (!artifact) {
            throw new Error('Specified artifact cannot be found : ' + JSON.stringify(options));
        }
        artifact.attachLifecycle(lifecycleName);
    };

    /*
     Removes the attached lifecycle from the artifact
     @options: The artifact from which the life cycle must be removed
     */
    ArtifactManager.prototype.detachLifecycle = function (options) {
        var artifact = getArtifactFromImage(this.manager, options);
        if (!artifact) {
            throw new Error('Specified artifact cannot be found : ' + JSON.stringify(options));
        }
	    var lifecycleName = getLifecycleName(artifact);
        artifact.detachLifecycle(lifecycleName);
    };

    /*
     Promotes the artifact to the next stage in its life cycle
     @options: An artifact image (Not a real artifact)
     */
    ArtifactManager.prototype.promoteLifecycleState = function (state, options) {
        var checkListItems,
            artifact = getArtifactFromImage(this.manager, options);
        if (!artifact) {
            throw new Error('Specified artifact cannot be found : ' + JSON.stringify(options));
        }
        //checkListItems = artifact.getAllCheckListItemNames();
	    var lifecycleName = getLifecycleName(artifact);
        artifact.invokeAction(state,lifecycleName);
    };

    /*
     Gets the current lifecycle state
     @options: An artifact object
     @returns: The life cycle state
     */
    ArtifactManager.prototype.getLifecycleState = function (options) {
        var artifact = getArtifactFromImage(this.manager, options);
        if (!artifact) {
            throw new Error('Specified artifact cannot be found : ' + JSON.stringify(options));
        }
        return artifact.getLifecycleState();
    };

    /*
     The function returns the list of check list items for a given state
     @options: The artifact
     @returns: A String array containing the check list items.(Can be empty if no check list items are present)
     */
    ArtifactManager.prototype.getCheckListItemNames = function (options) {
        var artifact = getArtifactFromImage(this.manager, options);
	    var lifecycleName = getLifecycleName(artifact);
        var checkListItems = artifact.getAllCheckListItemNames(lifecycleName) || [];

        var checkListItemArray = [];

        //Go through each check list item
        for (var index in checkListItems) {
            //Get whether the check list item is checked
            var state = artifact.isLCItemChecked(index);
            checkListItemArray.push({ 'name': checkListItems[index], 'checked': state });
        }

        return checkListItemArray;
    };

    /*
     The function checks whether a given check list item at the provided index is checked for the current state
     @index: The index of the check list item.This must be a value between 0 and the maximum check list item
     that appears in the lifecycle definition
     @options: An artifact object
     @throws Exception: If the index is not within 0 and the max check list item or if there is an issue ticking the item
     */
    ArtifactManager.prototype.isItemChecked = function (index, options) {

        var artifact = getArtifactFromImage(this.manager, options);
	    var lifecycleName = getLifecycleName(artifact);
        var checkListItems = artifact.getAllCheckListItemNames();

        var checkListLength = checkListItems.length;

        if ((index < 0) || (index > checkListLength)) {
            throw "The index value: " + index + " must be between 0 and " + checkListLength + ".Please refer to the lifecycle definition in the registry.xml for the number of check list items.";
        }
        var result = artifact.isLCItemChecked(index,lifecycleName);

        return result;
    };

    /*
     The method enables the check list item and the given index
     @index: The index of the check list item.This must be a value between 0 and the maximum check list item
     that appears in the lifecycle definition.
     @options: An artifact object
     @throws Exception: If the index is not within 0 and max check list item or if there is an issue ticking the item.
     */
    ArtifactManager.prototype.checkItem = function (index, options) {

        var artifact = getArtifactFromImage(this.manager, options);
	    var lifecycleName = getLifecycleName(artifact);
        var checkListItems = artifact.getAllCheckListItemNames();

        var checkListLength = checkListItems.length;

        if ((index < 0) || (index > checkListLength)) {
            throw "The index value: " + index + " must be between 0 and " + checkListLength + ".Please refer to the lifecycle definition in the registry.xml for the number of check list items.";
        }
        artifact.checkLCItem(index,lifecycleName);
    };

    /*
     The method disables the check list item at the given index
     @index: The index of the check list item.This must be a value between 0 and the maximum check list item
     that appears in the lifecycle definition
     @options: An artifact object
     @throws Exception: If the index is not within 0 and max check list item or if there is an issue ticking the item
     */
    ArtifactManager.prototype.uncheckItem = function (index, options) {

        var artifact = getArtifactFromImage(this.manager, options);
	    var lifecycleName = getLifecycleName(artifact);
        var checkListItems = artifact.getAllCheckListItemNames();

        var checkListLength = checkListItems.length;

        if ((index < 0) || (index > checkListLength)) {
            throw "The index value: " + index + " must be between 0 and " + checkListLength + ".Please refer to the lifecycle definition in the registry.xml for the number of check list items.";
        }
        artifact.uncheckLCItem(index,lifecycleName);
    };

    /*
     The method obtains the list of all available actions for the current state of the asset
     @options: An artifact object
     @returns: The list of available actions for the current state,else false
     */
    ArtifactManager.prototype.availableActions = function (options) {
        var artifact = getArtifactFromImage(this.manager, options);
        if (!artifact) {
            throw new Error('Specified artifact cannot be found : ' + JSON.stringify(options));
        }
	    var lifecycleName = getLifecycleName(artifact);
        return artifact.getAllLifecycleActions(lifecycleName) || [];
    };

    /*
     The function returns the life-cycle history path using
     the provided asset.
     @options: An asset.
     @return: A string path of the life-cycle history.
     */
    ArtifactManager.prototype.getLifecycleHistoryPath = function (options) {

        return getHistoryPath(options.path);
    };

    /*
    The function obtains the lifecycle history for the provided asset
    @options: An asset with a valid path.(A path which exists in the registry
    @return: A resource object containing the history as an xml
     */
    ArtifactManager.prototype.getLifecycleHistory=function(options){
        var historyPath=getHistoryPath(options.path);
        return this.registry.get(historyPath);
    };

    /*
     The function returns the life-cycle attached to the provided artifact
     @options: An asset as returned by the ArtifactManager get method
     @return: A string indicating the lifecycle name.If the artifact does not
     have a life-cycle then an empty string is returned.
     */
    ArtifactManager.prototype.getLifeCycleName = function (options) {

        var artifact = getArtifactFromImage(this.manager, options);

        var lifecycleName = '';

        if (artifact != null) {
            lifecycleName = artifact.getLifecycleName();
        }

        return lifecycleName;
    };

    /*
     The function returns all versions of the provided artifact
     @options: The artifact to be checked
     @return: A list of all the different versions of the provided asset
     */
    ArtifactManager.prototype.getAllAssetVersions = function (assetName) {

        var matchingArtifacts = [];

        var pred = {
            overview_name: assetName || ''
        };

        this.find(function (artifact) {

            //Add to the matches if the artifact exists
            if (assert(artifact.attributes, pred)) {

                //We only need the id and version
                matchingArtifacts.push({id: artifact.id, version: artifact.attributes.overview_version});
            }
        });

        return matchingArtifacts;
    };

    /*
     The function checks if the two objects a and b are equal.If a property in b is not
     in a, then both objects are assumed to be different.
     @a: The object to be compared
     @b: The object containing properties that must match in a
     @return: True if the objects are equal,else false.
     */
    var assert = function (a, b) {

        //Assume the objects will be same
        var equal = true;

        for (var key in b) {


            if (a.hasOwnProperty(key)) {

                //If the two keys are not equal
                if (a[key] != b[key]) {
                    return false;
                }
            }
            else {
                return false;
            }
        }

        return equal;
    };

    /*
     The function generates the history path of a given asset
     using its path
     @assetPath:The path of the asset to be retrieved.
     @return: The path of lifecycle history information
     */
    var getHistoryPath = function (assetPath) {

        //Replace the / in the assetPath
        var partialHistoryPath = assetPath.replace(lcHistoryRegExpression, HISTORY_PATH_SEPERATOR);

        var fullPath = HISTORY_PATH + partialHistoryPath;

        return fullPath;
    };

    /*
     generatePaginationForm will genrate json for registry pagination context, (pagination consistent handling)
     @pagin:The pagination details from UI
     @
     */
    var generatePaginationForm = function (pagin) {

        //pagination context for default
        var paginationLimit = 300;
        var paginationForm = {
            'start': 0,
            'count': 12,
            'sortOrder': 'ASC',
            'sortBy': 'overview_name',
            'paginationLimit': 2147483647
        };

        if (!pagin) {
            return paginationForm;
        }

        if (pagin.count != null) {
            paginationForm.count = pagin.count;
        }
        if (pagin.start != null) {
            paginationForm.start = pagin.start;
        }
        if (pagin.paginationLimit != null) {
            paginationForm.paginationLimit = pagin.paginationLimit;
        }
        if (pagin.sortBy != null) {
            paginationForm.sortBy = pagin.sortBy;
        }
        if (paginationForm.sortOrder != null) {
            paginationForm.sortOrder = pagin.sortOrder;
        }
        return paginationForm;

    };

    /*
     Helper function to create an artifact instance from a set of options (an image).
     */
    var getArtifactFromImage = function (manager, options) {

        var path = options.path || '';
        var lcName = options.lifecycle || '';
        var artifact = createArtifact(manager, {
            id: options.id,
            attributes: options.attributes
        });

        path = path.replace(REGISTRY_ABSOLUTE_PATH, '');

        artifact.setArtifactPath(path);
        artifact.setLcName(lcName);

        return artifact;
    };

    /**
     * The function was introduced as a fix to accomadate the goveerance API
     * changes to support multiple lifecycles
     * @param  {Object artifact A governance artifact instance
     * @return {String}          The name of the default lifecycle
     */
    var getLifecycleName = function(artifact){
        var lifecycleName;
        if(!artifact){
            throw "The artifact is null, cannot retrieve the lifecycle name for lifecycle operations";
        }
        try {
            lifecycleName = artifact.getLifecycleName();
        }
        catch(e){
            log.error(e);
            throw e;
        }
        return lifecycleName;
    };
}(server, registry));
