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
edu.rpi.tw.sesf.s2s.widgets.DateWidget = function(panel) {
	this.panel = panel;
	var input = panel.getInput();
	var input = jQuery("<input type=\"text\"></input>");
	jQuery(input).datepicker({ dateFormat: 'yy-mm-dd' });
	this.div =  jQuery("<div class=\"facet-content\"></div>");
	panel.setInputData(input.getId(), function() {
		return jQuery(input).val();
	});
	var self = this;
    jQuery(input).change(function() {
		this.updateState();
		panel.notify();
    });
	jQuery(this.div).append(input);
}

edu.rpi.tw.sesf.s2s.widgets.DateWidget.prototype.updateState = function(clicked)
{
	this.state = this.div.find("input").val();
}

edu.rpi.tw.sesf.s2s.widgets.DateWidget.prototype.getState = function()
{
	return this.state;
}

edu.rpi.tw.sesf.s2s.widgets.DateWidget.prototype.setState = function(state)
{
	this.div.find("input").val(state);
}

edu.rpi.tw.sesf.s2s.widgets.DateWidget.prototype.get = function()
{
	return this.div;
}