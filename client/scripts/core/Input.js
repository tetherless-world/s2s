/**
 * s2s.Input class
 * @author Eric Rozell
 */

(function(s2s,$) {
	//TODO: add "imports", or automatic pull of dependencies

	/**
	 * Constructor for Input instances
	 * @param data an input object or array (in the case of hierarchical Inputs)
	 */
	s2s.Input = function(data)
	{
		this.obj = data;
	}

	/**
	 * Constructs a label for the Input
	 * @return a string label
	 */
	s2s.Input.prototype.getLabel = function()
	{
		if (typeof(this.obj) == "array")
		{
			return this.obj[0]["label"] + " (with " + this.obj[1]["label"] + ")";
		}
		else if (typeof(this.obj) == "object")
		{
			return this.obj["label"];
		}
	}

	/**
	 * Gets the ID for the Input
	 * @return a string ID
	 */
	s2s.Input.prototype.getId = function()
	{
		return this.obj.uri;
	}

	/**
	 * Gets the description for the Input
	 * @return a string description
	 */
	s2s.Input.prototype.getDescription = function()
	{
		return this.obj.comment;
	}

	/**
	 * Gets the delimiter for the Input
	 * @return a delimiter used for the input
	 */
	s2s.Input.prototype.getDelimiter = function()
	{
	        return this.obj.delimiter != null ? this.obj.delimiter : ',';
	}

	/**
	 * Gets the class for the Object
	 * @return the class
	 */
	s2s.Input.prototype.getClass = function()
	{
		return s2s.Input;
	}
	
})(edu.rpi.tw.sesf.s2s,jQuery);

