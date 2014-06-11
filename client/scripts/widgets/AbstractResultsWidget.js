/**
 * Create Namespace
 */
if (window.org == undefined || typeof(org) != "object") org = {};
if (org.essilod == undefined || typeof(org.essilod) != "object") org.essilod = {};
if (org.essilod.s2s == undefined || typeof(org.essilod.s2s) != "object") org.essilod.s2s = {};

/**
 * Construct from Widget object
 */
org.essilod.s2s.AbstractResultsWidget = function(panel) {
	this.panel = panel;
	this.div = jQuery("<div style=\"display:inline\"><div style=\"float:right;\" class=\"paging-panel\"></div><br/><div style=\"margin-top:3px\" class=\"html\"><span>Loading...</span></div></div>");
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

org.essilod.s2s.AbstractResultsWidget.prototype.updateState = function()
{
	this.state = {"limit":this.limit,"offset":this.offset};
}

org.essilod.s2s.AbstractResultsWidget.prototype.setState = function(state)
{
	this.state = {"limit":this.limit,"offset":this.offset};
	this.panel.notify(true, true, true);
}

org.essilod.s2s.AbstractResultsWidget.prototype.getState = function()
{
	return this.state;
}

org.essilod.s2s.AbstractResultsWidget.prototype.get = function()
{
	return this.div;
}

org.essilod.s2s.AbstractResultsWidget.prototype.myReset = function()
{
	jQuery(this.div).find(".html").children().remove()
	jQuery(this.div).find(".html").append("<span>Loading...</span>");
	jQuery(this.div).find(".paging-panel").children().remove();
}

org.essilod.s2s.AbstractResultsWidget.prototype.reset = function()
{
	this.myReset();
	this.limit = this.panel.getInterface().getDefaultLimit();
	this.offset = 0;
}

org.essilod.s2s.AbstractResultsWidget.prototype.update = function(data)
{
	this.currData = data;
	jQuery(this.div).find(".html").children().remove();
	jQuery(this.div).find(".page").children().remove();
	
	//Create list from JSON
	var lessLen = 250;
	data = JSON.parse( data );
	var list = jQuery("<div></div>");
	for ( var i = 0; i < data.entries.length; ++i ) {
		var item = data.entries[i];
		var itemEl = jQuery("<div></div>");
		itemEl.append("<a target=\"_blank_\" href=\""+item['abstract']+"\">"+item['title']+"</a>");
		var abstract = jQuery("<div><span class=\"abstract more-text\"></span><span class=\"abstract less-text\"></span><br/><a style=\"cursor:pointer;color:blue\" class=\"expand more-button\">[More]</a></div>");
    	abstract.find(".more-text").append(item['text']);
    	abstract.find(".less-text").text(item['text'].substring(0,lessLen) + "...");
    	abstract.find("a").click(function() {
	    	if (jQuery(this).hasClass("less-button")) {
				jQuery(this).removeClass("less-button");
				jQuery(this).addClass("more-button");
				jQuery(this).html("[More]");
				jQuery(this).parent().find(".more-text").hide();
				jQuery(this).parent().find(".less-text").show();
	    	} else {
				jQuery(this).removeClass("more-button");
				jQuery(this).addClass("less-button");
				jQuery(this).html("[Less]");
				jQuery(this).parent().find(".less-text").hide();
				jQuery(this).parent().find(".more-text").show();
	    	}
    	});
    	abstract.find(".more-text").hide();
    	if (item['text'].length <= lessLen) {
			abstract.find(".expand").remove();
			abstract.find(".abstract .less-text").remove();
			abstract.find(".abstract .more-text").show();
    	}
    	itemEl.append(abstract).append("<br/>");
		list.append(itemEl);
	}
	jQuery(this.div).find(".html").append(list);

	//Handle paging
	var self = this;
    var nextCallback = function() 
	{
		if (self.limit == null)
		{
			if (data.limit != null) self.limit = data.limit;
			if (data.offset != null) self.offset = data.offset + self.limit;
		}
		else
		{
			self.offset = self.offset + self.limit;
		}
		self.myReset();
		self.updateState();
		self.panel.notify(true, true, true);
    }

    var prevCallback = function() 
    {
		if (self.limit == null)
		{
			if (data.limit != null) self.limit = data.limit;
			if (data.offset != null) self.offset = data.offset - self.limit;
		}
		else
		{
			self.offset = self.offset - self.limit;
		}
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
 		self.limit = parseInt(jQuery(this).val());
		self.offset = 0;
		self.myReset();
		self.updateState();
		self.panel.notify(true, true, true);
    });

	/**
	 * Get page info
	 */
	if (this.offset == null && data.offset != null) this.offset = data.offset;
	
	if (this.offset != null && this.offset > 0) {
		jQuery(prev).click(prevCallback);
	    jQuery(prev).css('cursor','pointer');
	} else {
		jQuery(prev).unbind('click');
    	jQuery(prev).css('opacity','0.4');
    	jQuery(prev).css('filter','alpha(opacity=40)');
    	jQuery(prev).css('cursor','auto');
	}

	if (this.limit == null && data.limit != null) this.limit = data.limit;
		
	if (this.limit != null) {
		jQuery(next).click(nextCallback);
	    jQuery(next).css('cursor','pointer');
	} else {
		jQuery(prev).unbind('click');
    	jQuery(prev).css('opacity','0.4');
    	jQuery(prev).css('filter','alpha(opacity=40)');
    	jQuery(prev).css('cursor','auto');
	}

    if (data.count != null)
    {
		jQuery(config).children().each(function() {
			if (parseInt(jQuery(this).val()) == self.limit) jQuery(this).attr("selected","selected");
		});
		jQuery(start).html("" + (this.offset + 1));
		jQuery(end).html("" + (this.offset + this.limit));
		jQuery(total).html("" + data.count);
		if ((this.offset + this.limit) >= data.count) {
	    	jQuery(next).unbind('click');
	    	jQuery(next).css('opacity','0.4');
	    	jQuery(next).css('filter','alpha(opacity=40)');
	    	jQuery(next).css('cursor','auto');
	    	jQuery(end).html("" + data.count);
		}
		jQuery(this.div).find(".paging-panel").append(panel);
    }
}