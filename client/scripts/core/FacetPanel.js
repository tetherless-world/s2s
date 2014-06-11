/**
 * FacetPanel class
 * @author Eric Rozell
 */

(function(s2s, $) {
	
	/**
	 * Constructor for FacetPanel instances
	 */
	s2s.utils.FacetPanel = function(defaults)
	{
		this.div = jQuery("<div name=\"search-widget-panel\"><span>Loading...</span></div>");
		this.defaults = defaults;
		this.paradigm = s2s.utils.hierarchicalSearch;
	}

	///
	/// Local API Function
	///

	/**
	 * Function used by the SearchService to get a XHTML representation of the FacetPanel
	 * @return a XHTML div container for the SearchPanel
	 */
	s2s.utils.FacetPanel.prototype.get = function()
	{
		return this.div;
	}

	/**
	 * Sets the parent service for this panel
	 * @param service the parent service managing this panel
	 */ 
	s2s.utils.FacetPanel.prototype.setParent = function(panel)
	{
		this.parentPanel = panel;
	}

	/**
	 * Serialize the state of this panel
	 * @return the state of the panel
	 */
	s2s.utils.FacetPanel.prototype.getState = function()
	{
		var state = {};
		state.enabled = this.config.enabled;
		state.positions = this.config.positions;
		state.ignore = this.config.ignore;
		state.defaults = this.config.defaults;
		state.panels = {};
		for (var input in this.config.panels)
		{
			state.panels[input] = this.config.panels[input].getState();
		}
		return state;
	}

	/**
	 * Callback used by panel to notify the entire system of a change
	 * @param widget the Widget object that has been updated or the index of the updated widget object
	 */
	s2s.utils.FacetPanel.prototype.notify = function(caller, local)
	{
		var inputs = this.getInputValues();
		this.parentPanel.notify(this, true);
		this.myUpdate(inputs, caller, local);
	}

	/**
	 * Callback when a widget has been updated
	 * @param widget the Widget object that has been updated or the index of the updated widget object
	 */
	s2s.utils.FacetPanel.prototype.myUpdate = function(inputs, caller, local)
	{
		var index = -1;

		//get index to update
		if (this.paradigm == s2s.utils.hierarchicalSearch && caller != null && typeof caller == "object")
		{
		    var widgetPanel = caller;
		    var input = widgetPanel.getInput().getId();
		    for (var i = 0; i < this.config.enabled.length; ++i)
		    {
				if (this.config.enabled[i] > index)
				{
					index = this.config.enabled[i];
				}
				var p = this.config.positions[this.config.enabled[i]];
				if (p == input)
				{
					index = this.config.enabled[i];
					break;
				}
		    }
		}
		else if (this.paradigm == s2s.utils.hierarchicalSearch && caller != null && typeof caller == "number")
		{
			index = caller;
		}

		for (var i = index + 1; i < this.config.positions.length; ++i)
		{
			if (jQuery.inArray(i,this.config.enabled) >= 0)
			{
				var input = this.config.positions[i];
				var widgetPanel = this.config.panels[input];
				if (this.paradigm == s2s.utils.hierarchicalSearch) inputs = this.getInputValues(input)
				if (widgetPanel != caller || !local) widgetPanel.update(inputs);
			}
		}
	}

	/**
	 * Callback when a widget has been updated
	 * @param widget the Widget object that has been updated or the index of the updated widget object
	 */
	s2s.utils.FacetPanel.prototype.update = function(inputs)
	{
		for (var i = 0; i < this.config.positions.length; ++i)
		{
			if (jQuery.inArray(i,this.config.enabled) >= 0)
			{
				var input = this.config.positions[i];
				var widgetPanel = this.config.panels[input];
				widgetPanel.update(inputs);
			}
		}
	}

	///
	/// Builder Functions
	///

	/**
	 * Callback when SearchService metadata is available to generate panel
	 * @param data object containing the SearchService metadata
	 */
	s2s.utils.FacetPanel.prototype.generate = function(state)
	{
		this.config = { "enabled" : [], "positions" : [], "defaults" : {}, "panels" : {}, "ignore" : []};

		//build the interface either from state object or default configuration
		if (state == null && this.defaults == null)
		{
			//get SearchService defaults from XML configuration
			var url = s2s.utils.metadataService + "?type=defaults&service=" + escape(this.getSearchService().getId()); 
			var self = this;
			var callback = function(response) {
				var defaults = jQuery.parseXML(response);
				//parse the default configuration                                                                                                                  
				s2s.utils.parseXmlConfiguration(defaults,self);
				//build the search panel once defaults are set                                                                                                     
				self.buildPanel();
			}
		    var error = function(jqXHR, textStatus, errorThrown) {
				alert("Unable to retrieve document at:" + url);
			}
		    s2s.utils.ajax('get',url,'',callback,error);
		} else if (state == null) {
			s2s.utils.parseJsonConfiguration(this.defaults,this);
			this.buildPanel();
		} else {
			//get SearchService defaults from state object
			this.config.enabled = state.enabled;
			this.config.positions = state.positions;
			this.config.ignore = state.ignore;
			this.config.defaults = state.defaults;

			this.buildPanel(state.panels);
		}
	}


	/**
	 * This function is for internal use only, use "generate" to create the panel
	 */
	s2s.utils.FacetPanel.prototype.buildPanel = function(panelStates)
	{
		jQuery(this.div).children().remove();
		var widgetPanel = jQuery("<div name=\"search-widget-panel\"></div>");
		for (var i = 0; i < this.config.positions.length; ++i)
		{
			var input = this.config.positions[i];
			var widgetHeader = jQuery("<h3><a style=\"display:inline\" href=\"#\">" + this.getSearchService().getInput(input).getLabel() + "</a></h3>");
			var widgetBody = jQuery("<div style=\"display:none\" name=\""+ input +"\"></div>");
			if (jQuery.inArray(i,this.config.enabled) >= 0)
			{
				widgetBody.append(jQuery("<span>Loading...</span>"));
				var state = (panelStates != null) ? panelStates[input] : null;
				this.initializeWidget(widgetBody, state);
			}
			//jQuery(widgetPanel).append(widgetHeader).append(widgetBody);
			jQuery(widgetPanel).append(jQuery("<div class=\"widget-divider\"></div>").append(widgetHeader).append(widgetBody));
		}
		jQuery(this.div).append(widgetPanel);
		//TODO: implement module for creating different panels
		var self = this;
		jQuery(widgetPanel).multiOpenAccordion({ 
			active: this.config.enabled, 
			click: function(event,ui) {
				var param = jQuery(ui["content"]).attr("name");
				var pos = jQuery.inArray(param, self.config.positions);
				//if the accordion was enabled
				if (jQuery.inArray(pos,self.config.enabled) >= 0)
				{
						var panel = self.config.panels[param];
						panel.reset();
						var idx = jQuery.inArray(pos, self.config.enabled);
						self.config.enabled.splice(idx,1);
						self.myUpdate(null,idx);
				}
				//if the accordion was disabled
				else
				{
					self.config.enabled.push(pos);
					//if widget has not been initialized
					if (self.config.panels[param] == null)
					{
						//jQuery(ui["content"]).append(jQuery("<span>Loading...</span>"));
						var state = (panelStates != null) ? panelStates[input] : null;
						self.initializeWidget(ui["content"], state);
					}
					//update widget
					else
					{
						var panel = self.config.panels[param];
						panel.update();
					}
				}
			}
		});
		jQuery(widgetPanel).find(".widget-divider span").remove();
		jQuery(widgetPanel).sortable({ 
			axis: 'y', 
			handle: '> h3', 
			update: function(event, ui)
			{
				var param = ui.item.find("div").attr("name");
				var next = ui.item.next();
				var nextParam = next.find("div").attr("name");
				var prevPos = jQuery.inArray(param, self.config.positions);
				var newPos = jQuery.inArray(nextParam, self.config.positions);
				var x = self.config.positions.splice(prevPos,1);
				if (prevPos < newPos)
				{
					self.config.positions.splice((newPos-1),0,param);
					var firstParam = self.config.positions[prevPos];
					for (var i = 0; i < self.config.enabled.length; i++)
					{
					    if (self.config.enabled[i] > prevPos && self.config.enabled[i] <= newPos-1) 
					    { 
							self.config.enabled[i]--;
					    }
					    else if (self.config.enabled[i] == prevPos)
					    {
							self.config.enabled[i] = newPos-1;
					    }
					}
					self.update(prevPos-1);
				}
				else
				{
					self.config.positions.splice(newPos,0,param);
					var panel = self.config.panels[param];
					for (var i = 0; i < self.config.enabled.length; i++)
					{
					    if (self.config.enabled[i] >= newPos && self.config.enabled[i] < prevPos) 
					    { 
							self.config.enabled[i]++;
					    }
					    else if (self.config.enabled[i] == prevPos)
					    {
							self.config.enabled[i] = newPos;
					    }
					}
					panel.update();
					self.update(newPos);
				}
				//event.stopPropagation();

			} 
		});
	}

	/**
	 * This function is primarily for internal use.
	 * @param div location to attach widget
	 */
	s2s.utils.FacetPanel.prototype.initializeWidget = function(div, state)
	{
		var input = jQuery(div).attr("name");
		var widgetPanel = new s2s.utils.WidgetPanel(this.getSearchService().getInput(input));
		this.config.panels[input] = widgetPanel;
		widgetPanel.setParent(this);
		jQuery(div).html("");
		jQuery(div).append(widgetPanel.get());
		if (this.config.defaults[input] != null)
		{
			widgetPanel.selectWidget(this.config.defaults[input]);
		}
		widgetPanel.generate(state);
	}

	///
	/// Proxy API Functions
	///

	/**
	 * Gets the SearchService
	 */
	s2s.utils.FacetPanel.prototype.getSearchService = function()
	{
		return this.parentPanel.getSearchService();
	}

	/**
	 * Proxy function to update data for managing service
	 * @param input the input to be updated
	 * @param data data object to be updated
	 */
	s2s.utils.FacetPanel.prototype.setInputData = function(input, f)
	{
		this.parentPanel.setInputData(input, f);
	}

	/**
	 * Proxy function to get input data from managing service
	 * @param input input to retrieve data for
	 * @return the data object for that input
	 */
	s2s.utils.FacetPanel.prototype.getInputData = function(input)
	{
		return this.parentPanel.getInputData(input);
	}

	/**
	 * Gets all input values, specific to the search paradigm
	 * @param input (optional) may specify a stopping point for 
	 *                  collecting data, i.e., for hierarchical search.
	 */
	s2s.utils.FacetPanel.prototype.getInputValues = function(input)
	{
		if (input && this.paradigm == s2s.utils.hierarchicalSearch)
		{
	    	var data = {};
			for (var i = 0; i < this.config.positions.length; ++i)
			{
				var p = this.config.positions[i];
				if (input != null && p == input)
				{
					break;
				}
				if (jQuery.inArray(i, this.config.enabled) >= 0)
				{
					data[p] = this.parentPanel.getInputValues(p);
				}
			}
			return data;
		}
		else
		{
			return this.parentPanel.getInputValues();
		}	
	}

	/**
	 * Function that returns the search paradigm of the current interface
	 */
	s2s.utils.FacetPanel.prototype.getSearchParadigm = function()
	{
		return this.paradigm;
	}

	
})(edu.rpi.tw.sesf.s2s, jQuery);
