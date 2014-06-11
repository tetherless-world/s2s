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
edu.rpi.tw.sesf.s2s.widgets.IogdsResultWidget = function(panel) {
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

edu.rpi.tw.sesf.s2s.widgets.IogdsResultWidget.prototype.updateState = function()
{
	this.state = {"limit":this.limit,"offset",this.offset};
}

edu.rpi.tw.sesf.s2s.widgets.IogdsResultWidget.prototype.setState = function(state)
{
	this.state = {"limit":this.limit,"offset",this.offset};
	this.panel.notify(true, true, true);
}

edu.rpi.tw.sesf.s2s.widgets.IogdsResultWidget.prototype.getState = function()
{
	return this.state;
}

edu.rpi.tw.sesf.s2s.widgets.IogdsResultWidget.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.IogdsResultWidget.prototype.myReset = function()
{
	jQuery(this.div).find(".html").children().remove()
	jQuery(this.div).find(".html").append("<span>Loading...</span>");
	jQuery(this.div).find(".paging-panel").children().remove();
}

edu.rpi.tw.sesf.s2s.widgets.IogdsResultWidget.prototype.reset = function()
{
	this.myReset();
	this.limit = this.panel.getInterface().getDefaultLimit();
	this.offset = 0;
}

edu.rpi.tw.sesf.s2s.widgets.IogdsResultWidget.prototype.update = function(data)
{	
	jQuery(this.div).find(".html").children().remove();

	/**
	 * Create JavaScript object from data and serialize HTML list
	 */
	data = JSON.parse( data );
	var list = data['data'];
	var ol = jQuery("<div class=\"dataset-list\"></div>");
    
    var notNull = function(val) {
		return val && val != "" && val != "http://purl.org/twc/vocab/conversion/null" && val != "n/a";
    }

    for (var i in list)
    {
		var lesslen = 110;
		var item = list[i];
		var li = jQuery("<div class=\"dataset-list-item\"></div>");
		if (!notNull(item['dataset'])) continue;
		var html_title = "<span title=\"title\" class=\"dstitle\">";
		var title = notNull(item['title']) ? item['title'] : item['dataset'];
		if (notNull(item['url'])) html_title += "<a target=\"_blank_\" href=\"" + item['url'] + "\">" + title + "</a>";
		else html_title += title;
		html_title += "</span>";
		li.append(html_title + "<br/>");

		var agency = null;
		var catalog_country = null;

		if (notNull(item['catalog_homepage'])) catalog_country = "<a target=\"_blank_\" href=\"" + item['catalog_homepage'] + "\">" + item['catalog_homepage'] + "</a>";
		if (notNull(item['catalog_homepage']) && notNull(item['catalog_country'])) catalog_country += " (" + item['catalog_country'] + ")";
		else if (notNull(item['country'])) catalog_country = item['catalog_country'];

        if (notNull(item['agency'])) li.append("<span title=\"agency\">" + item['agency'] + "</span><br/>");
		if (notNull(item['category'])) li.append("<span title=\"category\" class=\"category\">" + item['category'] + "</span><br/>");
		if (catalog_country != null) li.append("<span title=\"catalog/country\">" + catalog_country + "</span><br/>");
	
		if (!notNull(item['title'])) li.append("<span title=\"LOGD URI\" class=\"homepage\">LOGD URI: " + item['dataset'] + "</span><br/>");
		if (notNull(item['description'])) {
	    	var d = item['description'];
	    	var description = jQuery("<div title=\"description\"><span class=\"dsdescription moredesc\"></span><span class=\"dsdescription lessdesc\"></span><br/><a class=\"descbutton morebutton\">[More]</a></div>");
	    	jQuery(description).find(".moredesc").append(d);
	    	jQuery(description).find(".lessdesc").text(d.substring(0,lesslen) + "...");
	    	jQuery(description).find("a").click(function() {
		    	if (jQuery(this).hasClass("lessbutton")) {
					jQuery(this).removeClass("lessbutton");
					jQuery(this).addClass("morebutton");
					jQuery(this).html("[More]");
					jQuery(this).parent().find(".moredesc").hide();
					jQuery(this).parent().find(".lessdesc").show();
		    	} else {
					jQuery(this).removeClass("morebutton");
					jQuery(this).addClass("lessbutton");
					jQuery(this).html("[Less]");
					jQuery(this).parent().find(".lessdesc").hide();
					jQuery(this).parent().find(".moredesc").show();
		    	}
	    	});
	    	jQuery(description).find(".moredesc").hide();
	    	if (item['description'].length <= lesslen) {
				jQuery(description).find(".descbutton").remove();
				jQuery(description).find(".lessdesc").remove();
				jQuery(description).find(".moredesc").show();
	    	}
	    	jQuery(li).append(description);
		}
		ol.append(li);//.append("<br/>");
    }
    
    jQuery(this.div).find(".html").append(ol);

    /**
     * Define callback functions for paging
     */
	data['limit'] = parseInt(data['limit']);
    data['offset'] = parseInt(data['offset']);
    data['total'] = parseInt(data['total']);

	var self = this;
    var nextCallback = function() {
		if (self.limit == null)
		{
	    	self.limit = data['limit'];
        }
	    self.offset += self.limit;
		self.myReset();
		self.updateState();
		self.panel.notify(true, true, true);
    }
    var prevCallback = function() 
    {
	    self.offset -= self.limit;
		self.myReset();
		self.updateState();
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
		var params = widget.getData();
 		self.limit = jQuery(this).val();
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

	if (data['limit'] != null && data['limit'] != this.limit) {
		this.limit = data['limit'];
	}

    if (this.offset == 0) {
		jQuery(prev).unbind('click');
		jQuery(prev).css('opacity','0.4');
		jQuery(prev).css('filter','alpha(opacity=40)');
		jQuery(prev).css('cursor','auto');
    }

    var results = data['total'];
    if (results != null)
    {
		jQuery(config).children().each(function() {
			if (parseInt(jQuery(this).val()) == this.limit) jQuery(this).attr("selected","selected");
		});
		jQuery(start).html("" + (this.offset + 1));
		jQuery(end).html("" + (this.offset + this.limit));
		jQuery(total).html("" + results);
		if ((this.offset + this.limit) >= results) {
	    	jQuery(next).unbind('click');
	    	jQuery(next).css('opacity','0.4');
	    	jQuery(next).css('filter','alpha(opacity=40)');
	    	jQuery(next).css('cursor','auto');
	    	jQuery(end).html("" + results);
		}
		jQuery(this.div).find(".paging-panel").append(panel);
    }
}