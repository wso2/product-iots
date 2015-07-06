var cache = {};
$.template = function (name, location, callback) {
    var template = cache[name];
    if (!template) {
        $.get(location, function (data) {
            var compiledTemplate = Handlebars.compile(data);
            cache[name] = compiledTemplate;
            callback(compiledTemplate);
        });
    } else {
        callback(template);
    }
};
Handlebars.registerHelper('equal', function(lvalue, rvalue, options) {
    if (arguments.length < 3)
        throw new Error("Handlebars Helper equal needs 2 parameters");
    if( lvalue != rvalue ) {
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
