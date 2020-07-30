var fs = require('fs');
var path = require("path");
var plist = require('plist');
var Utilities = require("./lib/utilities");

var IOS_DIR = 'platforms/ios';
var PLUGIN_ID = 'cordova-plugin-image-picker';

var appName = Utilities.getAppName();
var appPlistPath = IOS_DIR + '/' + appName + '/'+appName+'-Info.plist';

function parseConfigVariables () {
    var variables = {};
    var config = Utilities.parseConfigXml();

    (config.widget.plugin ? [].concat(config.widget.plugin) : []).forEach(function(plugin){
        (plugin.variable ? [].concat(plugin.variable) : []).forEach(function(variable){
            if((plugin._attributes.name === PLUGIN_ID || plugin._attributes.id === PLUGIN_ID) && variable._attributes.name && variable._attributes.value){
                variables[variable._attributes.name] = variable._attributes.value;
            }
        });
    });
    return variables;
}


module.exports = function (context) {
    var platforms = context.opts.platforms;
    if (platforms.indexOf('ios') !== -1 && Utilities.directoryExists(IOS_DIR)){
        Utilities.log('Preparing image picker on iOS');

        var appPlistModified = false;
        var appPlist = plist.parse(fs.readFileSync(path.resolve(appPlistPath), 'utf8'));

        var pluginVariables = parseConfigVariables();

        Utilities.log('pluginVariables: ' + JSON.stringify(pluginVariables));

        if(typeof pluginVariables['UI_THEME_COLOR'] !== 'undefined'){
            appPlist["uiThemeColor"] = pluginVariables['UI_THEME_COLOR'];
            appPlistModified = true;
        }

        if(appPlistModified) {
            fs.writeFileSync(path.resolve(appPlistPath), plist.build(appPlist));
        }
    }
}
