function onRequest(context){
    var dcProps = require('/config/dc-props.js').config();
    context.appContext = dcProps.appContext;
    return context;
}
