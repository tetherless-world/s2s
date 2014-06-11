/**
 * s2s.Interface class
 * @author Eric Rozell
 */

(function(s2s,$) {
	
	/**
	 * Constructor for Interface instances
	 * @param data an interface object
	 */
	s2s.Interface = function(data) 
	{
		this.obj = data;
	}

	/**
	 * Constructs a label for the Interface
	 * @return a string label
	 */
	s2s.Interface.prototype.getLabel = function()
	{
		return this.obj["label"];
	}

	/**
	 * Gets an identifier for the Interface
	 * @return a string ID
	 */
	s2s.Interface.prototype.getId = function()
	{
		return this.obj.uri;
	}

	/**
	 * Gets the type of the Interface
	 * @return a string ID for the interface type
	 */
	s2s.Interface.prototype.getTypes = function()
	{
		return this.obj['types'];
	}

	/**
	 * Gets the description of the Interface
	 * @return a string description
	 */
	s2s.Interface.prototype.getDescription = function()
	{
		return this.obj.comment;
	}

	/**
	 * Gets the output of the Interface
	 * @return a string ID for the output of the interface
	 */
	s2s.Interface.prototype.getOutput = function()
	{
		return this.obj.output;
	}

	/**
	 * Gets the input supported by the Interface
	 * @return a string ID for the input supported by the interface (may be null)
	 */
	s2s.Interface.prototype.getInput = function()
	{
		return this.obj.input;
	}

	/**
	 * Gets the default limit for the Interface
	 * @return an integer value for the default limit of the interface (may be null)
	 */
	s2s.Interface.prototype.getDefaultLimit = function()
	{
		return this.obj.limit;
	}

	/**
	 * Gets the class of the Object
	 * @return a class
	 */
	s2s.Interface.prototype.getClass = function()
	{
		return s2s.Interface;
	}
	
})(edu.rpi.tw.sesf.s2s,jQuery);
