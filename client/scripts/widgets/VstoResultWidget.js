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
edu.rpi.tw.sesf.s2s.widgets.VstoResultWidget = function(panel) {
	this.panel = panel;
	this.div = jQuery("<div><div style=\"float:right;\" class=\"paging-panel\"></div><br/><div style=\"margin-top:3px\" class=\"html\"><span>Loading...</span></div></div>");
	var offsetInput = edu.rpi.tw.sesf.s2s.utils.offsetInput;
	var limitInput = edu.rpi.tw.sesf.s2s.utils.limitInput;
	var self = this;
	panel.setInputData(offsetInput, function() {
		return self.offset;
	});
	panel.setInputData(limitInput, function() {
		return self.limit;
	});
	this.limit = this.panel.getInterface().getDefaultLimit();
	this.offset = 0;
}

edu.rpi.tw.sesf.s2s.widgets.VstoResultWidget.prototype.updateState = function()
{
	this.state = {"limit":this.limit,"offset",this.offset};
}

edu.rpi.tw.sesf.s2s.widgets.VstoResultWidget.prototype.setState = function(state)
{
	this.state = {"limit":this.limit,"offset",this.offset};
	this.panel.notify(true, true, true);
}

edu.rpi.tw.sesf.s2s.widgets.VstoResultWidget.prototype.getState = function()
{
	return this.state;
}

edu.rpi.tw.sesf.s2s.widgets.VstoResultWidget.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.VstoResultWidget.prototype.myReset = function()
{
	jQuery(this.div).find(".html").children().remove()
	jQuery(this.div).find(".html").append("<span>Loading...</span>");
	jQuery(this.div).find(".paging-panel").children().remove();
}

edu.rpi.tw.sesf.s2s.widgets.VstoResultWidget.prototype.reset = function()
{
	this.myReset();
	this.limit = this.panel.getInterface().getDefaultLimit();
	this.offset = 0;
}

edu.rpi.tw.sesf.s2s.widgets.VstoResultWidget.prototype.update = function(data)
{
	jQuery(this.div).find(".html").children().remove();

	/**
	 * Create JavaScript object from data and serialize HTML list
	 */
		jQuery(this.div).find(".html").children().remove();
		data = JSON.parse('(' + data + ')');
	
		//write page HTML
		var ul = jQuery("<ul></ul>");
		jQuery(this.div).find(".html").append(ul);
		for (var i = 0; i < data.length; ++i)
		{
			var item = data[i];
			
			//set default values to display
			var inst = (item.inst != null) ? item.inst : 'n/a';
			var kinst = (item.kinst != null) ? item.kinst : inst;
			var begin = (item.begin_date != null) ? item.begin_date : "";
			var end = (item.end_date != null) ? item.end_date : "";			

			//store data files as hidden inputs
			var datafiles = "";
			if (data[i].datafiles)
			{
				for (var j = 0; j < data[i].datafiles.length; ++j)
				{
					datafiles += "<input class=\"vsto-datafile\" type=\"hidden\" value=\""+data[i].datafiles[j]+"\" />";
				}
			}

			//Build parameter panel
			var params = "";
			if (data[i].parameters)
			{
				params += "<ul style=\"display:table;\">";
				for (var j = 0; j < data[i].parameters.length; ++j)
				{
					params += "<li style=\"list-style-type:none;\"><input type=\"checkbox\" value=\""+data[i].parameters[j]['id']+"\">"+data[i].parameters[j]['label']+"</input></li>";
				}
				params += "</ul>";
			}
			
			//build data product panel
			var products = "";
			if (data[i].products)
			{
				products += "<ul style=\"display:inline;\">";
				for (var j = 0; j < data[i].products.length; ++j)
				{
					products += "<li style=\"display:inline;list-style-type:none;padding-right:2em;\"><a target=\"_blank_\" href=\"#\" value=\""+data[i].products[j]+"\">"+data[i].products[j]+"</a></li>";
				}	
				products += "</ul>";
			}
			
			//create show parameters button
			var show = jQuery("<a class=\"more\" href=\"#\">Show</a>");
			
			
			//write item HTML
			var txt = "<li style=\"border:solid 1px;list-style-type:none;margin-top:0.5em;padding-left:0.25em;padding-right:0.25em\"><span><b>Get Data:</b><br/>" + datafiles + products + "<br/>" +
				"<span><b>Instrument:</b><span title=\""+kinst+"\"> "+inst+"</span><br/>" +
				"<span><b>Date Range: </b><input type=\"text\" size=\"10\" value=\""+begin+"\" /> to <input type=\"text\" size=\"10\" value=\""+end+"\" /><br/>" +
				"<span><b>Parameters: </b><span class=\"vsto-parameters-toggle\"></span><br/><div class=\"vsto-parameters\"></div></span><br/></li>";
			
			//add parameters and hide
			jQuery(show).click(function(e) {
				if (jQuery(this).hasClass("more"))
				{
					((jQuery(this).parent()).parent()).find(".vsto-parameters").show();
					jQuery(this).removeClass("more");
					jQuery(this).addClass("less");
					jQuery(this).html("Hide");
				}
				else
				{
					((jQuery(this).parent()).parent()).find(".vsto-parameters").hide();
					jQuery(this).removeClass("less");
					jQuery(this).addClass("more");	
					jQuery(this).html("Show");
				}
				e.stopPropagation();
			});
	
			//append li to ul
			var li = jQuery(txt);
			jQuery(li).find(".vsto-parameters-toggle").append(show);
			jQuery(li).find(".vsto-parameters").append(params);
			jQuery(ul).append(li);
			jQuery(li).find(".vsto-parameters").hide();
		}
    /**
     * Define callback functions for paging
     */
	data['limit'] = parseInt(data['limit']);
    data['offset'] = parseInt(data['offset']);
    data['total'] = parseInt(data['total']);

	self = this;
    var nextCallback = function() {	
		if (self.limit == null)
		{
	    	self.limit = data['limit'];
	    	self.offset = 0 + data['offset'] + data['limit'];
        }
		else
		{
	    	self.offset = self.offset + self.limit
		}
		self.myReset();
		self.panel.notify(true, true, true);
    }
    var prevCallback = function() 
    {
		if (self.limit == null)
		{
        	self.limit = data['limit'];
	    	self.limit = 0 + data['offset'] - data['limit'];
		}
		else
		{
			self.offset = self.offset - self.limit;
		}
		self.myReset();
		self.panel.notify(true, true, true);
    }

    /**
     * Create paging panel
     */
    var prev = jQuery("<div title=\"prev\" style=\"margin-left:3px;border:1px solid;background:#E6E6E6;display:inline\"><span style=\"width:4em;align:center\">&nbsp;&lt;&nbsp;</span></div>");
    var next = jQuery("<div title=\"next\" style=\"margin-left:1px;border:1px solid;background:#E6E6E6;display:inline\"><span style=\"width:4em;align:center\">&nbsp;&gt;&nbsp;</span></div>");
    var start = jQuery("<b></b>");
    var end = jQuery("<b></b>");
    var total = jQuery("<b></b>");
    var paging = jQuery("<span></span>").append(start).append("-").append(end).append(" of ").append(total);
    var config = jQuery("<select style=\"margin-left:5px\"><option>10</option><option>20</option><option>50</option><option>100</option><option>200</option></select>");
    var panel = jQuery("<span></span>").append(paging).append(prev).append(next).append(config);

    jQuery(config).change(function() {
 		self.limit = parseInt(jQuery(this).val());
		self.offset = 0;
		self.myReset();
		self.updateState();
		self.panel.notify(true, true, true);
    });

    /**
     * Add callbacks to paging buttons
     */
    jQuery(next).click(nextCallback);
    jQuery(next).css('cursor','pointer');
    jQuery(prev).click(prevCallback);
    jQuery(prev).css('cursor','pointer');

    if (self.offset != null && self.offset == 0) {
		jQuery(prev).unbind('click');
		jQuery(prev).css('opacity','0.4');
		jQuery(prev).css('filter','alpha(opacity=40)');
		jQuery(prev).css('cursor','auto');
    } else if (self.offset == null) {
		self.offset = data['offset'];
		if (offset == 0) {
	    	jQuery(prev).unbind('click');
	    	jQuery(prev).css('opacity','0.4');
	    	jQuery(prev).css('filter','alpha(opacity=40)');
	    	jQuery(prev).css('cursor','auto');
 		}
    }

    var results = data['total'];
    if (results != null)
    {
		self.limit = data['limit'];
		self.offset = data['offset'];
		jQuery(config).children().each(function() {
			if (parseInt(jQuery(this).val()) == limit) jQuery(this).attr("selected","selected");
		});
		jQuery(start).html("" + (offset + 1));
		jQuery(end).html("" + (offset + limit));
		jQuery(total).html("" + results);
		if ((offset + limit) >= results) {
	    	jQuery(next).unbind('click');
	    	jQuery(next).css('opacity','0.4');
	    	jQuery(next).css('filter','alpha(opacity=40)');
	    	jQuery(next).css('cursor','auto');
	    	jQuery(end).html("" + results);
		}
		jQuery(div).find(".paging-panel").append(panel);
    }
}