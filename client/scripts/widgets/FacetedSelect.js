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
edu.rpi.tw.sesf.s2s.widgets.FacetedSelect = function(panel) 
{
	this.panel = panel;
	var input = panel.getInput();
    var freetext = jQuery("<input type=\"text\"></input>");
    jQuery(freetext).autocomplete({source: edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.autocompleteSource , select: edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.autocompleteSelect });
    var select = jQuery("<select class=\"data-selector\" multiple size=\"8\" style=\"width:100%\"><option value=\"blank\">Loading...</option></select>");
    this.div = jQuery("<div class=\"facet-content\" style=\"width:100%\"></div>");
	panel.setInputData(input.getId(), function() {
		var arr = [];
        jQuery(select).find(":selected").each(function() {
            arr.push(jQuery(this).val());
        });
		return arr;
	});
	var self = this;
    jQuery(select).change(function() {
		self.updateState();
        if (jQuery(select).val() != "blank");
        {
            panel.notify();
        }
    });
    jQuery(this.div).append(freetext);
    jQuery(this.div).append("<br/>");
    jQuery(this.div).append(select);
}


edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.prototype.updateState = function(clicked)
{
	var val = jQuery(clicked).val();
	if (this.state == null) this.state = {};
	if (this.state[val] == null) this.state[val] = 1;
	else delete this.state[val];
}

edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.prototype.getState = function()
{
	return this.state;
}

edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.prototype.setState = function(state)
{
	var self = this;
	jQuery(Object.keys(state)).each(function() {
		jQuery(self.div).find("option[value=\"" + this + "\"]").attr("selected","selected");
	});
}

edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.prototype.reset = function()
{
	var select = jQuery(this.div).find("select.data-selector");
	jQuery(select).children().remove();
    jQuery(select).append("<option value=\"blank\">Loading...</option>");
}

edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.prototype.update = function(data)
{
	var f = function(o1,o2) {
        return (o1.count - o2.count) * -1;
    }
	data = JSON.parse(data);
    data.sort(f);
	var select = jQuery(this.div).find("select");
    jQuery(select).children().remove();
    jQuery(data).each(function() 
    {
        var label = this.label && this.label != null ? this.label : this.id;
        if (this.id != null && this.count != null) {
            select.
                append(jQuery("<option></option>").
                    attr("value",this.id).
                    attr("title",'(' + this.count + ') ' + label).
                    text('(' + this.count + ') ' + label));
        }
    });
}

edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.autocompleteSource = function(term, callback)
{
    var t = term;
    var input = this.element;
    var parent = jQuery(input).parent();
    var options = jQuery(input).parent().find("select option");
    var data = [];
    for (var i = 0; i < options.length; ++i)
    {
        var o = jQuery(options[i]);
        if (o.html().toLowerCase().indexOf(term.term.toLowerCase()) > -1)
        {
            data.push({"label":o.html(),"value":"","option":o[0]});
        }
    }
    callback(data);
}

edu.rpi.tw.sesf.s2s.widgets.FacetedSelect.autocompleteSelect = function(event, ui)
{
    var item = ui.item;
    item.option.selected = true;
    jQuery(item.option).parent().change();
    event.target.value = '';
    event.stopPropagation();
}