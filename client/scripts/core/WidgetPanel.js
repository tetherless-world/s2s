/**
 * Widget class
 * @author Eric Rozell
 */

(function(s2s, $) {

	/**
	 * Constructor for WidgetContainer instance
	 * @param arg a Input object or a Interface object
	 */
	s2s.utils.WidgetPanel = function(arg)
	{
		if (arg != undefined)
		{
			if (arg.getClass() == s2s.Input)
			{
				this.input = arg;
			}
			else if (arg.getClass() == s2s.Interface)
			{
				this["interface"] = arg;
			}
		}
		this.updateCount = 0;
		this.div = s2s.utils.getWidgetFrame();
		this.selector = s2s.utils.getWidgetSelectorFrame();
		jQuery(this.div).append(this.selector);
		jQuery(this.div).find(".button-panel").hide();
		jQuery(this.div).find(".widget-panel").hide();
		//TODO: implement better strategy for button panel/widget panel
	}

	///
	/// Local API Functions
	///

	/**
	 * Get the XHTML for the Widget
	 * @return a XHTML div container for the Widget
	 */
	s2s.utils.WidgetPanel.prototype.get = function()
	{
		return this.div;
	}

	s2s.utils.WidgetPanel.prototype.showSelectedWidget = function(state)
	{
		if (this.curr != null)
		{
			jQuery(this.selector).hide();
			var name = this.metadata[this.curr].prototype;
			//TODO: don't use eval
			this.widget = eval("new " + name + "(this)");
			jQuery(this.div).find(".widget-panel").children().remove();
			jQuery(this.div).find(".widget-panel").append(this.widget.get());
			jQuery(this.div).find(".widget-panel").show();
			this.update(this.parentPanel.getInputValues());
			if (state != null) { 
				//TODO: something
			}
		}
	}

	/**
	 * Callback used by widgets to notify the UI that something has changed
	 */
	s2s.utils.WidgetPanel.prototype.notify = function(local, noReset, noState)
	{
		if (local) {
			var inputs = this.parentPanel.getInputValues();
			this.update(inputs, noReset, noState);
		} else {
			this.parentPanel.notify(this, true);
		}
	}

	/**
	 * Callback used by parent panels to trigger widget update
	 */
	s2s.utils.WidgetPanel.prototype.update = function(inputs, noReset, noState)
	{
		var localCount = ++this.updateCount;
		var self = this;

		var state = null;
		if (this.getSearchParadigm() != null && this.getSearchParadigm() == s2s.utils.facetedSearch) state = this.widget.getState();
		if (this.widget != null && this.widget.reset != null && (noReset == null || (noReset != null && !noReset))) this.widget.reset();
		var isQuery = this["interface"] != null;
		if (this.widget != null && this.widget.update != null) {
			var data = { "service" : this.getSearchService().getId(), 
				 	 	 "interface" : (isQuery) ? this["interface"].getId() : this.getSearchService().getInterfaceForInput(this.input.getId()).getId(),
				 	  	 "inputs" : inputs };
			var callback = function(response) 
			{
		   		if (self.updateCount == localCount) 
				{
					self.widget.update(response);
					if (state != null && !noState && self.widget.setState != null) self.widget.setState(state);
		   		}
			}
			s2s.utils.ajax('post',s2s.utils.proxyService,JSON.stringify(data),callback);
		}
	}

	/**
	 * Called to generate the Widget once the JavaScript has been loaded
	 */
	s2s.utils.WidgetPanel.prototype.generate = function(state)
	{
		if (state != null)
		{
			this.curr = state.widget;
		}
		this.getMetadata(state);
	}

	/**
	 * Returns a state object for the widget
	 * @return a state object used by the widget
	 */
	s2s.utils.WidgetPanel.prototype.getState = function()
	{
		return this.curr;
	}

	/**
	 * Proxy function used to reset a widget
	 */
	s2s.utils.WidgetPanel.prototype.reset = function()
	{
	    if (this.widget.reset != null) this.widget.reset();
	}

	/**
	 * Set the Search/ResultPanel containing this Widget
	 */
	s2s.utils.WidgetPanel.prototype.setParent = function(panel)
	{ 
		this.parentPanel = panel;
	}

	/**
	 * Use the widget specified as input (from selector)
	 * @param widget URI of widget
	 */
	s2s.utils.WidgetPanel.prototype.selectWidget = function(widget,state)
	{
		if (widget != null)
		{
			this.curr = widget;
		}

		if (this.metadata == null)
		{
			//do nothing
		}
	    else if (this.curr != null && this.metadata[this.curr] == null) 
	    {
			var url = s2s.utils.metadataService + "?type=widgets&instance=" + escape(this.curr);
			var self = this;
			var cb = function(response) 
			{
		    	var obj = eval('(' + response + ')');
		    	if (obj[self.curr] != undefined) 
		    	{
					self.metadata[self.curr] = obj[self.curr];
					self.selectWidget(self.curr);
		    	}
		    	else 
		    	{
					alert("Could not retrieve metadata for widget: " + self.curr);
		    	}
			}
			s2s.utils.ajax("get",url,'',cb);
	    }
		//render the selected widget
	    else if (this.curr != null)
	    {
			jQuery(this.selector).find("option[selected]").removeAttr("selected");
			jQuery(this.selector).find(".widget-selector-list option[value=\""+this.curr+"\"]").attr("selected","selected");
			jQuery(this.selector).find(".widget-selector-info").html(this.metadata[this.curr].comment);
			var self = this;
			var jsCallback = function() 
			{ 
				self.showSelectedWidget(state); 
			}
			var cssCallback = function() 
			{
		    	s2s.utils.jsLoad(self.metadata[self.curr]['scripts'],jsCallback);
			}
			if (self.metadata[self.curr]['css'] != null && self.metadata[self.curr]['css'].length > 0)
			{
		    	s2s.utils.cssLoad(self.metadata[self.curr]['css'],cssCallback);
			}
			else
			{
		    	s2s.utils.jsLoad(self.metadata[self.curr]['scripts'],jsCallback);
			}
	    }
	}

	s2s.utils.WidgetPanel.prototype.getMetadata = function(state)
	{
		var url = s2s.utils.metadataService + "?type=widgets";
		if (this.input != null)
		{
			url += "&input=" + escape(this.input.getId()) + "&class=" + escape(s2s.utils.searchWidgetClass);
			for (var q in this.getSearchService().getInterfaces())
			{
				var query = this.getSearchService().getInterface(q);
				if (query.getInput() != undefined && query.getInput() == this.input.getId())
				{
					url += "&output=" + query.getOutput();
				}
			}
		}
		else if (this["interface"] != null)
		{
			url += "&class="+escape(s2s.utils.resultsWidgetClass)+"&output="+escape(this["interface"].getOutput());
		}
		var self = this;
		var callback = function(response)
		{
		    var data = eval( '(' + response + ')' );
		    self.setMetadata(data);
			self.selectWidget(null,state);
		}
		s2s.utils.ajax('get',url,'',callback);
	}

	/**
	 * Provides the metadata for the local Widget (triggers the loading of JavaScript and initialization)
	 */
	s2s.utils.WidgetPanel.prototype.setMetadata = function(data)
	{
		this.metadata = data;
		jQuery(this.selector).find(".widget-selector-list").children().remove();
		jQuery(this.selector).find(".widget-selector-list").append("<option value=\"blank\">Choose a S2S Widget.</option>");
		for (var uri in data)
		{
			var label = data[uri]["label"] != undefined ? data[uri]["label"] : uri;
			var option = jQuery("<option value=\""+uri+"\">" + label +"</option>");
			jQuery(this.selector).find(".widget-selector-list").append(option);	
		}
		var self = this;
		jQuery(this.selector).find(".widget-selector-list").change(function() {
			self.curr = jQuery(this).val();
			var info = self.selector.find(".widget-selector-info");
			if (self.curr != 'blank') jQuery(info).html(data[self.curr].comment ? data[self.curr].comment : '');
			else jQuery(info).html('');
		});
		jQuery(this.selector).find(".widget-selector-submit").click(function() {
			if (jQuery(self.selector).find(".widget-selector-list").val() == "blank")
			{
				alert("Please select an S2S Widget to use.");
			}
			else
			{
				var jsCallback = function() { self.selectWidget(); }
				var cssCallback = function() {
				    s2s.utils.jsLoad(self.metadata[self.curr]['scripts'],jsCallback);
				}
				if (self.metadata[self.curr]['css'] != null && self.metadata[self.curr]['css'].length > 0)
				    s2s.utils.cssLoad(self.metadata[self.curr]['css'],cssCallback);
				else
				    s2s.utils.jsLoad(self.metadata[self.curr]['scripts'],jsCallback);			    
			}
		});	
	}

	/**
	 * Returns the Input object for the WidgetPanel
	 * @return the Input object for the WidgetPanel
	 */
	s2s.utils.WidgetPanel.prototype.getInput = function()
	{
		if (this.input == null)
		{
			alert("Input for WidgetPanel does not exist.");
		}
		return this.input;
	}

	/**
	 * Returns the Interface object for the WidgetPanel
	 * @return the Interface object for the WidgetPanel
	 */
	s2s.utils.WidgetPanel.prototype.getInterface = function()
	{
		if (this["interface"] == null)
		{
			alert("Interface for WidgetPanel does not exist.")
		}
		return this["interface"];
	}

	///
	/// Proxy API Functions
	///

	/**
	 * Gets the SearchService
	 */
	s2s.utils.WidgetPanel.prototype.getSearchService = function()
	{
		return this.parentPanel.getSearchService();
	}

	/**
	 * Proxy function to update data for managing service
	 * @param input the input to be updated
	 * @param data data object to be updated
	 */
	s2s.utils.WidgetPanel.prototype.setInputData = function(input, f)
	{
		this.parentPanel.setInputData(input, f);
	}

	/**
	 * Proxy function to get input data from managing service
	 * @param input input to retrieve data for
	 * @return the data object for that input
	 */
	s2s.utils.WidgetPanel.prototype.getInputData = function(input)
	{
		return this.parentPanel.getInputData(input);
	}

	/**
	 * Proxy to get all input values, specific to the search paradigm
	 * @param input (optional) may specify the URI of a input being updated
	 */
	s2s.utils.WidgetPanel.prototype.getInputValues = function(input)
	{
		return this.parentPanel.getInputValues(input);
	}

	/**
	 * Proxy function to get the search paradigm of the current interface
	 */
	s2s.utils.WidgetPanel.prototype.getSearchParadigm = function()
	{
		if (this.parentPanel.getSearchParadigm != null)
		{
			return this.parentPanel.getSearchParadigm();
		}
		else
		{
			return null;
		}
	}
	
})(edu.rpi.tw.sesf.s2s, jQuery);
