/**
 * HookPanel class
 * @author Eric Rozell
 */

(function(s2s, $) {
	/**
	 * Constructor for WidgetContainer instance
	 * @param arg a Input object or a Interface object
	 */
	s2s.utils.HookPanel = function(i,callback)
	{
		this["interface"] = i;
		this.callback = callback;
		this.updateCount = 0;
	}

	///
	/// Local API Functions
	///
	
	/**
	 * Callback used by widgets to notify the UI that something has changed
	 */
	s2s.utils.HookPanel.prototype.notify = function(local)
	{
		this.parentPanel.notify(this, true);
	}

	/**
	 * Callback used by parent panels to trigger widget update
	 */
	s2s.utils.HookPanel.prototype.update = function(inputs)
	{
		var localCount = ++this.updateCount;
		var self = this;

		var state = null;
		var data = { "service" : this.getSearchService().getId(), 
				 	 	 "interface" : this["interface"],
				 	  	 "inputs" : inputs };
		var callback = function(response)
		{
			if (self.updateCount == localCount) 
			{
				self.callback(response);
			}
		}
		
		s2s.utils.ajax('post',s2s.utils.proxyService,JSON.stringify(data),callback);
	}

	/**
	 * Set the Search/ResultPanel containing this Widget
	 */
	s2s.utils.HookPanel.prototype.setParent = function(panel)
	{ 
		this.parentPanel = panel;
	}

	/**
	 * Returns the Interface object for the HookPanel
	 * @return the Interface object for the HookPanel
	 */
	s2s.utils.HookPanel.prototype.getInterface = function()
	{
		if (this["interface"] == null)
		{
			alert("Interface for HookPanel does not exist.")
		}
		return this["interface"];
	}

	///
	/// Proxy API Functions
	///

	/**
	 * Gets the SearchService
	 */
	s2s.utils.HookPanel.prototype.getSearchService = function()
	{
		return this.parentPanel.getSearchService();
	}

	/**
	 * Proxy function to update data for managing service
	 * @param input the input to be updated
	 * @param data data object to be updated
	 */
	s2s.utils.HookPanel.prototype.setInputData = function(input, f)
	{
		this.parentPanel.setInputData(input, f);
	}

	/**
	 * Proxy function to get input data from managing service
	 * @param input input to retrieve data for
	 * @return the data object for that input
	 */
	s2s.utils.HookPanel.prototype.getInputData = function(input)
	{
		return this.parentPanel.getInputData(input);
	}

	/**
	 * Proxy to get all input values, specific to the search paradigm
	 * @param input (optional) may specify the URI of a input being updated
	 */
	s2s.utils.HookPanel.prototype.getInputValues = function(input)
	{
		return this.parentPanel.getInputValues(input);
	}

	/**
	 * Proxy function to get the search paradigm of the current interface
	 */
	s2s.utils.HookPanel.prototype.getSearchParadigm = function()
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
