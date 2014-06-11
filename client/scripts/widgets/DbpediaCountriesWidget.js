/**
 * Create Namespace
 */
if (window.edu == undefined || typeof(edu) != "object") edu = {};
if (edu.rpi == undefined || typeof(edu.rpi) != "object") edu.rpi = {};
if (edu.rpi.tw == undefined || typeof(edu.rpi.tw) != "object") edu.rpi.tw = {};
if (edu.rpi.tw.sesf == undefined || typeof(edu.rpi.tw.sesf) != "object") edu.rpi.tw.sesf = {};
if (edu.rpi.tw.sesf.s2s == undefined || typeof(edu.rpi.tw.sesf.s2s) != "object") edu.rpi.tw.sesf.s2s = {};
if (edu.rpi.tw.sesf.s2s.widgets == undefined || typeof(edu.rpi.tw.sesf.s2s.widgets) != "object") edu.rpi.tw.sesf.s2s.widgets = {};

/**
 * Construct from Widget object
 */
edu.rpi.tw.sesf.s2s.widgets.DbpediaCountriesWidget = function(panel) {
	this.panel = panel;
    var input = panel.getInput();
    var content = jQuery("<div style=\"height:200px;overflow:auto;border:solid 1px;border-color:#A9A9A9\" class=\"data-selector\"><span>Loading...</span></div>");
    var div = jQuery("<div class=\"facet-content\" style=\"width:100%\"></div>");
	panel.setInputData(input.getId(), this.getInputData);
    jQuery(this.div).append(content);
}

edu.rpi.tw.sesf.s2s.widgets.DbpediaCountriesWidget.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.DbpediaCountriesWidget.prototype.reset = function()
{
	var select = jQuery(this.div).find("select.data-selector");
	jQuery(select).children().remove();
    jQuery(select).append("<span>Loading...</span>");
}

edu.rpi.tw.sesf.s2s.widgets.DbpediaCountriesWidget.prototype.update = function(data)
{
    var f = function(o1,o2) 
	{
        return (o1.count - o2.count) * -1;
    }
    var div = jQuery(this.div).find("div.data-selector");
    data = JSON.parse(data);
	jQuery(div).children().remove();
	var callback = function(finalData)
	{
		finalData.sort(f);
		for (var i = 0; i < finalData.length; ++i) 
		{
			var item = data[i];
			var input = jQuery("<div><input type=\"checkbox\" value=\""+item.id+"\">("+item.count+") "+item.label+" </input><img height=\"12\" src=\""+item.flag+"\" /></div><br/>");
			var self = this;
			jQuery(input).click(function() {
				self.updateState();
		    	self.panel.notify();
			});
			jQuery(div).append(input);
		}
	}
	edu.rpi.tw.sesf.s2s.widgets.DbpediaCountriesWidget.runQuery(data,callback);
}

edu.rpi.tw.sesf.s2s.widgets.DbpediaCountriesWidget.prototype.getInputData = function()
{
	var arr = [];
    jQuery(this.div).find("input:checked").each(function() {
	    arr.push(jQuery(this).val());
    });
	return arr;
}

edu.rpi.tw.sesf.s2s.widgets.DbpediaCountriesWidget.runQuery = function(data, callback)
{
	var hash = {};
    for (var i = 0; i < data.length; ++i) 
	{
		var item = data[i];
		if (item.id != undefined)
		{
	    	hash[item.id] = { "count" : item.count };
		}
	}
    var file = "http://logd.tw.rpi.edu/s2s/countries.json";
    var cb = function(text) 
	{
		var response = eval('(' + text + ')');
		for (var i = 0; i < response.items.length; ++i) {
			var item = response.items[i];
			if (hash[item.uri] != undefined) 
			{
				hash[item.uri].label = item.label;
				hash[item.uri].flag = item.flag;
	     	}
		}
		var retObj = [];
		for (var uri in hash) {
			var country = hash[uri];
			country.id = uri;
			retObj.push(country);
		}
		callback(retObj);
    }
    edu.rpi.tw.sesf.s2s.utils.ajax('get',file,null,cb);
}
