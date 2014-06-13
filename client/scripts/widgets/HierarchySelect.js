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
edu.rpi.tw.sesf.s2s.widgets.HierarchySelect = function(panel) 
{
    this.panel = panel;
    var freetext = jQuery("<input type=\"text\"/>");
    jQuery(freetext).autocomplete({source: edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.autocompleteSource, select: edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.autocompleteSelect });
    var content = jQuery("<div style=\"height:200px;overflow:auto;border:solid 1px;border-color:#A9A9A9\" class=\"data-selector\"><span>Loading...</span></div>");
   	this.div = jQuery("<div class=\"facet-content\" style=\"width:100%\"></div>");
	panel.setParameterData(parameter.getId(), this.getParameterData);
    jQuery(this.div).append(freetext);
    jQuery(this.div).append("<br/>");
    jQuery(this.div).append(content);
}

edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.prototype.reset = function()
{
    jQuery(this.div).children().remove();
    jQuery(this.div).append("<span>Loading...</span>");
}

edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.prototype.update = function(data)
{
	data = JSON.parse(data);
	jQuery(this.div).children().remove();
	var tree = edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.buildTree(data);
	jQuery(tree).css("margin-left","0px");
	jQuery(this.div).append(tree);
	var self = this;
	jQuery(this.div).find("input").click(function() {
		self.panel.notify();
	});
	jQuery(div).find("li").click(edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.expandCollapse);
}

edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.prototype.getParameterData = function()
{
    var parameter = this.panel.getParameter();
	var arr = [];
    jQuery(this.div).find("input:checked").each(function() {
	    arr.push(jQuery(this).val());
    });
	return arr;
}

edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.autocompleteSource = function(term, callback)
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
		    var j = 0;
		    for ( ; j < data.length; ++j) {
			if (data[j]["myval"] == jQuery(o).find("input").val()) {
			    	break;
			}
		    }
		    if ( j == data.length )
		    {
			data.push({"label":o.html(),"myval":jQuery(o).find("input").val(),"value":"","option":o[0]});
		    }	
	      }
	}
    callback(data);
}

edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.autocompleteSelect = function(event, ui)
{
    var item = ui.item;
    item.option.checked = true;
    jQuery(item.input).click();
    event.target.value = '';
    event.stopPropagation();
}

edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.buildTree = function(tree, root)
{
	var list = jQuery("<ul style=\"list-style-type:none;padding-left:0px;margin-left:15px;\"></ul>");
    if (tree != null)
    {
		for (var i = 0; i < tree.length; ++i)
		{
	    	if (tree[i]['parent'] == root) 
	    	{
				var item = tree.splice(i--,1)[0];
				var li = jQuery("<li><input type=\"checkbox\" value=\"" + item['id'] + "\">" + item['label'] + "</input></li>");
				var subtree = edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.buildTree(tree,item['id']);
				if (jQuery(subtree).children().length > 0) {
					jQuery(li).append(subtree);
					jQuery(li).addClass("less");
				}
				else {
					jQuery(li).addClass("leaf");
				}
				jQuery(list).append(li);
	    	}
		}
    }
    return list;
}

edu.rpi.tw.sesf.s2s.widgets.HierarchySelect.expandCollapse = function(eventObject)
{
	var obj = eventObject.target;
    if (jQuery(obj).hasClass("less"))
	{
		jQuery(obj).children("ul").hide(); jQuery(obj).addClass("more").removeClass("less");
    }
    else if (jQuery(obj).hasClass("more"))
	{
		jQuery(obj).children("ul").show(); jQuery(obj).addClass("less").removeClass("more");
    }
    eventObject.stopPropagation();
}