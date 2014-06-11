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
edu.rpi.tw.sesf.s2s.widgets.FacetOntologyResultsTable = function(panel) {
	this.panel = panel;
	this.div = jQuery("<div class=\"facet-ontology-results-table\" style=\"display:inline\"><div style=\"float:right;\" class=\"paging-panel\"></div><br/><div style=\"margin-top:3px\" class=\"html\"><span>Loading...</span></div></div>");
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

edu.rpi.tw.sesf.s2s.widgets.FacetOntologyResultsTable.prototype.updateState = function()
{
	this.state = {"limit":this.limit,"offset":this.offset};
}

edu.rpi.tw.sesf.s2s.widgets.FacetOntologyResultsTable.prototype.setState = function(state)
{
	this.panel.notify(true, true, true);
}

edu.rpi.tw.sesf.s2s.widgets.FacetOntologyResultsTable.prototype.getState = function()
{
	return this.state;
}

edu.rpi.tw.sesf.s2s.widgets.FacetOntologyResultsTable.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.FacetOntologyResultsTable.prototype.myReset = function()
{
	jQuery(this.div).find(".html").html("");
	jQuery(this.div).find(".html").append("<span>Loading...</span>");
	jQuery(this.div).find(".paging-panel").children().remove();
}

edu.rpi.tw.sesf.s2s.widgets.FacetOntologyResultsTable.prototype.reset = function()
{
	this.myReset();
	this.limit = this.panel.getInterface().getDefaultLimit();
	this.offset = 0;
}

edu.rpi.tw.sesf.s2s.widgets.FacetOntologyResultsTable.prototype.update = function(data)
{
	jQuery(this.div).find(".html").children().remove();
	jQuery(this.div).find(".page").children().remove();
    data = JSON.parse(data);
	var results = data;
	var limit = null;
	var offset = null;
	var total = null;
	if (!jQuery.isArray(data)) {
		results = data["results"];
		limit = data["limit"];
		offset = data["offset"];
		total = data["total"];
	}

	//Build the table
	var headers = results[0];
	var paging = (limit != null && offset != null && total != null) ?
		"<div><input type=\"hidden\" name=\"itemsPerPage\" value=\"" + limit + "\"/><input type=\"hidden\" name=\"startIndex\" value=\"" + offset + "\"/><input type=\"hidden\" name=\"totalResults\" value=\"" + total + "\"/></div>" :
		"";
	var table = "<table><thead><tr><th>Info</th>";
	for (var i = 0; i < headers.length; ++i) {
		if (headers[i] != "goal") table += "<th>" + headers[i] + "</th>";
	}
	table += "</tr></thead><tbody>"
	for (var i = 1; i < results.length; ++i) {
		table += "<tr><td><a target=\"_blank_\" href=\"" + results[i]["goal"] + "\"><img src=\"http://aquarius.tw.rpi.edu/s2s/2.0/ui/images/icon_info_gray.gif\" /></a></td>";
		for (var j = 0; j < headers.length; ++j) {
			var content = (results[i][headers[j]] != null) ? results[i][headers[j]] : "";
			if (headers[j] != "Link" && headers[j] != "goal") table += "<td>" + content + "</td>";
			else if (headers[j] == "Link") table += "<td><a target=\"_blank_\" href=\""+content+"\">link</a></td>";
		}
		table += "</tr>";
	}
	table += "</tbody></table>";
	jQuery(this.div).find(".html").append(paging + table);

	var self = this;
    var nextCallback = function() 
	{
		if (self.limit == null)
		{
			var limit = jQuery(self.div).find("[name=\"itemsPerPage\"]");
			var offset = jQuery(self.div).find("[name=\"startIndex\"]");
			if (limit.length > 0) 
			{
				self.limit = parseInt(limit.val());
			}
			if (offset.length > 0)
			{
				self.offset = parseInt(offset.val()) + self.limit;
			}
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
			var limit = jQuery(self.div).find("[name=\"itemsPerPage\"]");
			var offset = jQuery(self.div).find("[name=\"startIndex\"]");
			if (limit.length > 0) 
			{
				self.limit = parseInt(limit.val());
			}
			if (offset.length > 0)
			{
				self.offset = parseInt(offset.val()) - self.limit;
			}
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
	if (this.offset == null) {
		var offset = jQuery(this.div).find("[name=\"startIndex\"]");
		if (offset.length > 0) {
			this.offset = parseInt(offset.val());
		}
	}
	
	if (this.offset != null && this.offset > 0) {
		jQuery(prev).click(prevCallback);
	    jQuery(prev).css('cursor','pointer');
	} else {
		jQuery(prev).unbind('click');
    	jQuery(prev).css('opacity','0.4');
    	jQuery(prev).css('filter','alpha(opacity=40)');
    	jQuery(prev).css('cursor','auto');
	}

	if (this.limit == null) {
		var limit = jQuery(this.div).find("[name=\"itemsPerPage\"]");
		if (limit.length > 0) {
			this.limit = parseInt(limit.val());
		}
	}
	
	if (this.limit != null) {
		jQuery(next).click(nextCallback);
	    jQuery(next).css('cursor','pointer');
	} else {
		jQuery(prev).unbind('click');
    	jQuery(prev).css('opacity','0.4');
    	jQuery(prev).css('filter','alpha(opacity=40)');
    	jQuery(prev).css('cursor','auto');
	}

    var results = jQuery(this.div).find("[name=\"totalResults\"]");
    if (results != null)
    {
		results = parseInt(results.val());
		jQuery(config).children().each(function() {
			if (parseInt(jQuery(this).val()) == self.limit) jQuery(this).attr("selected","selected");
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