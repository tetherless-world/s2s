/**
 * Create Namespaces
 */
if (window.edu == undefined || typeof(edu) != "object") edu = {};
if (edu.rpi == undefined || typeof(edu.rpi) != "object") edu.rpi = {};
if (edu.rpi.tw == undefined || typeof(edu.rpi.tw) != "object") edu.rpi.tw = {};
if (edu.rpi.tw.sesf == undefined || typeof(edu.rpi.tw.sesf) != "object") edu.rpi.tw.sesf = {};
if (edu.rpi.tw.sesf.s2s == undefined || typeof(edu.rpi.tw.sesf.s2s) != "object") edu.rpi.tw.sesf.s2s = {};
if (edu.rpi.tw.sesf.s2s.utils == undefined || typeof(edu.rpi.tw.sesf.s2s.utils) != "object") edu.rpi.tw.sesf.s2s.utils = {};

(function(s2s, $) {
	
	/**
	 * Widget utilities
	 */
	s2s.utils.getWidgetFrame = function()
	{
		//TODO: make more efficient
		var div = $("<div class=\"widget\"></div>");
		var title = $("<span class=\"widget-title\"></span>");
	 	var widgetPanel = $("<div class=\"widget-panel\"><span>Loading...</span></div>");
		var buttonPanel = $("<div class=\"button-panel\" align=\"right\"></div>");
		$(div).append(title).append(buttonPanel).append(widgetPanel);
		return div;
	}


	s2s.utils.getWidgetSelectorFrame = function()
	{
		//TODO: make more efficient
		var select = $("<select class=\"widget-selector-list\" width=\"inherit\"><option value='blank'>Loading widget options...</option></select>");
		var span = $("<span class=\"widget-selector-info\"></span>");
		var submit = $("<button class=\"widget-selector-submit\">OK</button>");
		var table = $("<table class=\"widget-selector\"></table>").
			append($("<tr></tr>").append(select)).
			append($("<tr></tr>").append(span)).
			append($("<tr></tr>").append(submit));
		return table;
	}

	s2s.utils.getPagedResultWidgetFrame = function()
	{
		var div = $("<div><div style=\"float:right;\" class=\"paging-panel\"></div><br/><div style=\"margin-top:3px\" class=\"widget\"><span>Loading...</span></div></div>");
		var title = $("<span class=\"widget-title\"></span>");
		var widgetPanel = $("<div class=\"widget-panel\"><span>Loading...</span></div>");
		var buttonPanel = $("<div class=\"button-panel\" align=\"right\"></div>");
		$(div).append(title).append(buttonPanel).append(widgetPanel);
		return div;
	}

	s2s.utils.jsLoad = function(js,callback)
	{
		load.apply(null,js).thenRun(callback);
	}

	s2s.utils.cssLoad = function(css,callback)
	{
	    loadCss.apply(null,css);
	    setTimeout(callback,500);
	}

	s2s.utils.parseJsonConfiguration = function(defaults,panel)
	{
		var paradigm = defaults['paradigm'];
		$(defaults['inputs']).each(function() {
			if (this['enabled']) {
				panel.config.positions.push(this['name']);
				panel.config.enabled.push(panel.config.positions.length - 1);
			} else if (!this['ignore']) {
				panel.config.positions.push(this['name']);
			}
			if (this['widget'] != undefined) {
				panel.config.defaults[this['name']] = this['widget'];
			}
			if (this['ignore']) {
				panel.config.ignore.push(this['name']);
			}
		});
		
		//give remaining inputs a position
		for (var p in panel.config.inputs) 
		{
		    if ($.inArray(p,panel.config.positions) < 0 && $.inArray(p,panel.config.ignore) < 0)
			{
				panel.config.positions.push(p);
			}
		}
	}

	/**
	 * Configuration parsers
	 */
	s2s.utils.parseXmlConfiguration = function(defaults,panel)
	{
		var paradigm = $(defaults).find("Paradigm");
		if ($(paradigm).length > 0)
		{
			$(paradigm).each(function()
			{
				panel.paradigm = $(this).attr("name");
			});
		}
		var inputs = $(defaults).find("Inputs");
		if ($(inputs).length > 0)
		{
			$(inputs).children().each(function() 
			{
				if ($(this).is("Input"))
				{
					if ($(this).attr("enabled") != undefined && $(this).attr("enabled") != "false")
					{
						panel.config.positions.push($(this).attr("name"));
						panel.config.enabled.push((panel.config.positions.length - 1));
					}
					else if ($(this).attr("ignore") == undefined || $(this).attr("ignore") != "true")
					{
						panel.config.positions.push($(this).attr("name"));
					}
					if ($(this).attr("widget") != undefined)
					{
						panel.config.defaults[$(this).attr("name")] = $(this).attr("widget");
					}
					if ($(this).attr("ignore") != undefined && $(this).attr("ignore") == "true")
					{
					    panel.config.ignore.push($(this).attr("name"));
					}
				}
			});
		}

		//give remaining inputs a position
		for (var p in panel.config.inputs) 
		{
		    if ($.inArray(p,panel.config.positions) < 0 && $.inArray(p,panel.config.ignore) < 0)
			{
				panel.config.positions.push(p);
			}
		}
	}

	s2s.utils.ajax = function(method,url,data,callback,error)
	{
		if (window.XDomainRequest) {
			// Use Microsoft XDR
			var xdr = new XDomainRequest();
			xdr.open(method,url);
			xdr.onload = function () {
				callback(xdr.responseText);
			}
			xdr.onerror = function () {
				error(xdr);
			}
			xdr.ontimeout = function () {
				error(xdr);
			}
			xdr.send(data);
		} else {
			$.ajax({
				url: url,
				type: method,
				dataType: 'text',
				data: data,
				success: callback,
				error: error
		    });
		}
	}

	s2s.utils.parseXml = function(text) {
	    if (window.DOMParser) {
	        parser=new DOMParser();
			xmlDoc=parser.parseFromString(text,"text/xml");
			return xmlDoc;
	    } else { // Internet Explorer
			xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
			xmlDoc.async="false";
			xmlDoc.loadXML(txt);
			return xmlDoc;
	    }
	}

	/**
	 * Initialization Options
	 */

	s2s.utils.checkSessionHash = function()
	{
		if (window.location.hash.length > 0) {
			return window.location.hash;
		} else {
			return null;
		}
	}

	s2s.utils.getSessionData = function(callback)
	{
	    var session = s2s.utils.checkSessionHash();
		var url = s2s.utils.sessionService + "?key=" + session;
		s2s.utils.ajax('get',url,'',callback);
	}
})(edu.rpi.tw.sesf.s2s, jQuery);

