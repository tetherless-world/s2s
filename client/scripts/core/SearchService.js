/**
 * SearchService class
 * @author Eric Rozell
 */

(function(s2s, $) {

	/**
	 * Constructor for SearchService instance
	 * @param service either the URI of the S2S SearchService or a S2S state object
	 * @param panels (optional) an array of panels to be managed by the SearchService
	 */
	s2s.SearchService = function(service, config, panels, serviceDiv)
	{
		this.defaults = config;
		this.panels = [];
		this.panelStates = [];
		this.loaded = false;
	
		if (panels != null) {
			for (var i = 0; i < panels.length; ++i)
			{
				this.panels.push(panels[i]);
			}
		}
	
		//initialize service from URI
		if (typeof service == "string") 
		{
			this.uri = service;
			this.panelStates = [];
			this.inputData = {};
		}
	
		//initialize service from state object
		else if (typeof service == "object")
		{
			for (var i in service) this.uri = i;
			this.panelStates = service[this.uri].panels;
			this.inputData = service[this.uri].inputData;
		}
	
		var self = this;
		var url = s2s.utils.metadataService + "?type=services&instance=" + escape(this.uri);
	
		//make Ajax request to metadata service
		var callback = function(response)
		{
			var data = eval( '(' + response + ')' );
			var error = function() { console.error("Failed to get metadata for SearchService via: " + url);	}
			if (data == null) error();
			self.metadata = data[self.uri];
		
			//build Input objects for all inputs
			self.inputs = {};
			for (var p in self.metadata.inputs)
			{
				self.inputs[p] = new s2s.Input(self.metadata.inputs[p]);
			}

			//build Interface objects for all interfaces
			self.interfaces = {};
			for (var q in self.metadata.interfaces)
			{
				self.interfaces[q] = new s2s.Interface(self.metadata.interfaces[q]);
			}
			
			self.init(serviceDiv);
		}
		s2s.utils.ajax('get',url,'',callback, true);
	}

	s2s.SearchService.prototype.initPanels = function(div) {
		var self = this;
		$(div).find("#s2s-facet-panel").each(function() {
			facetPanel = new s2s.utils.FacetPanel(self.defaults);
			self.addPanel(facetPanel);
			$(this).append(facetPanel.get());
		});
		$(div).find(".s2s-widget-panel").each(function() {
			var widgetURI = $(this).find("input[name=\"widget\"]").val();
			var inputURI = $(this).find("input[name=\"input\"]").val();
			var interfaceURI = $(this).find("input[name=\"interface\"]").val();
			var arg = (inputURI != null) ? self.getSearchService().getInput(inputURI) : self.getSearchService().getInterface(interfaceURI);
			var panel = new s2s.utils.WidgetPanel(arg);
			self.addPanel(panel);
			if (widgetURI != null)
			{
				panel.selectWidget(widgetURI);
			}
			$(this).append(panel.get());
		});
	}

	s2s.SearchService.prototype.init = function(div) {
		var self = this;
		if (div != null) {
			this.initPanels(div);
		}
		namedDivs = $("[name=\"" + this.uri + "\"]");
		namedDivs.each(function() {
			self.initPanels(this);
		});
	}

	/**
	 * Adds a panel that should be managed by the service
	 * @param panel the panel to be managed
	 * @return the panel index
	 */
	s2s.SearchService.prototype.addPanel = function(panel)
	{
		panel.setParent(this);
		for (var i = 0; i < this.panels.length; ++i)
		{
			if (this.panels[i] == null)
			{
				this.panels[i] = panel;
				if (panel.generate && this.panelStates != null && this.panelStates[i] != null) 
				{
					panel.generate(this.panelStates[i]);
				}
				else if (panel.generate)
				{
					panel.generate();
				}
				else if (self.panels[i].update)
				{
				    self.panels[i].update();
				}
				return i;
			}
		}
		this.panels.push(panel);
		if (panel.generate && this.panelStates != null && this.panelStates[this.panels.length - 1] != null) 
		{
			panel.generate(this.panelStates[this.panels.length - 1]);
		}
		else if (panel.generate)
		{
			panel.generate();
		}
		return this.panels.length - 1;
	}

	/**
	 * Removes the panel at the given index
	 * @param index the panel index to remove
	 */
	s2s.SearchService.prototype.removePanel = function(index)
	{
		this.panels[index] = null;
	}

	/**
	 * Returns the FacetPanel for this service
	 * By convention, this panel is at index 0
	 * @return the panel at index 0
	 */
	s2s.SearchService.prototype.getFacetPanel = function()
	{
		return this.getPanel(0);
	}

	/**
	 * Returns the panel at the given index
	 * @param index the index of a panel
	 * @return the panel at the index
	 */
	s2s.SearchService.prototype.getPanel = function(index)
	{
		return this.panels[index];
	}

	/**
	 * @return the URI of the SearchService
	 */
	s2s.SearchService.prototype.getId = function()
	{
		return this.uri;
	}

	/**
	 * Gets this SearchService object
	 */
	s2s.SearchService.prototype.getSearchService = function()
	{
		return this;
	}

	s2s.SearchService.prototype.getInterfaces = function()
	{
		if (this.interfaces == null)
		{
			alert("Interface metadata not available.");
		}
		return this.interfaces;
	}

	s2s.SearchService.prototype.getInterface = function(uri)
	{
		if (this.interfaces == null || this.interfaces[uri] == null)
		{
			alert("Interface, " + uri + ", does not exist.");
			return null;
		}
		else
		{
			return this.interfaces[uri];
		}
	}

	s2s.SearchService.prototype.getInterfaceForInput = function(uri)
	{
		for (var i in this.interfaces) 
		{
			if (this.interfaces[i].getInput() == uri) 
			{
				return this.interfaces[i];
			}
		}
		return null;
	}

	s2s.SearchService.prototype.getInputs = function()
	{
		if (this.inputs == null)
		{
			alert("Input metadata not available.");
		}
		return this.inputs;
	}

	s2s.SearchService.prototype.getInput = function(uri)
	{
		if (this.inputs == null || this.inputs[uri] == null)
		{
			alert("Input, " + uri + ", does not exist.");
			return null
		}
		else
		{
			return this.inputs[uri];
		}
	}

	/**
	 * Calls the update function for all child panels (except calling panel)
	 * @param caller the object that made the update call (may be null)
	 */
	s2s.SearchService.prototype.notify = function(caller, local) {
		this.update(caller,local);
	}

	/**
	 * Calls the update function for all child panels (except calling panel)
	 * @param caller the object that made the update call (may be null)
	 */
	s2s.SearchService.prototype.update = function(caller, local) {
		//update panels (except caller)
		for (var i = 0; i < this.panels.length; ++i)
		{
			if (this.panels[i] != null && (!local || this.panels[i] != caller))
			{
				this.panels[i].update(this.getInputValues());
			}
		}
		//this.updateState();
	}

	/**
	 * Updates input data from data sent by child
	 * @param data a data object set by the 
	 */
	s2s.SearchService.prototype.setInputData = function(input, f) {
		//note, this clobbers old input data, so be careful about updating
		this.inputData[input] = f;
	}

	/**
	 * Get input data for the service for a specific input
	 * @param input get data values for input
	 */
	s2s.SearchService.prototype.getInputData = function(input) {
		if (this.inputData[input] != null)
		{
			return this.inputData[input]();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Gets all input values, specific to the search paradigm
	 */
	s2s.SearchService.prototype.getInputValues = function(input)
	{
		if (input != null && this.inputs[input] != null)
		{
			var delim = this.inputs[input].getDelimiter();
			if (jQuery.isArray(this.getInputData(input)))
			{
				return this.getInputData(input).join(delim);
			}
			else if (typeof this.getInputData(input) == "string")
			{
				return this.getInputData(input);
			}
			//TODO: what to do with type "object"
			else
			{
				return "";
			}
		}
		else
		{
			var data = {};
			for (var i in this.inputData)
			{
				var delim = (this.inputs[i] != null) ? this.inputs[i].getDelimiter() : null;
				if (jQuery.isArray(this.getInputData(i)) && delim != null)
				{
					data[i] = this.getInputData(i).join(delim);
				}
				else if (typeof this.getInputData(i) == "string" ||
					 	typeof this.getInputData(i) == "number")
				{
					data[i] = this.getInputData(i);
				}
				//TODO: what to do with type "object"
				else
				{
					data[i] = "";
				}
			}	
			return data;
		}
	}

	/**
	 * Serialize the state of the interface and get session key
	 * Should only be called by update function...
	 */
	s2s.SearchService.prototype.updateState = function()
	{	
		//update panel information
		for (var i = 0; i < this.panels.length; ++i)
		{
			this.panelState[i] = this.panels[i].getState();
		}
	
		//create state object
		var obj = {};
		obj.inputs = this.inputData;
		obj.panels = this.panelState;
	
		//send state to server to get session key
		var callback = function(response) {
		    window.location.hash = response;
		}
	
		//update URL hash with session
		var url = s2s.utils.sessionService;
		s2s.utils.ajax("post",url,JSON.stringify(obj),callback);
	}

})(edu.rpi.tw.sesf.s2s, jQuery);