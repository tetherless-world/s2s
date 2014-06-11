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
edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect = function(panel) 
{
    var input = panel.getInput();
    this.panel = panel;
    var freetext = jQuery("<input type=\"text\"></input>");
    jQuery(freetext).autocomplete({source: edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.autocompleteSource, select: edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.autocompleteSelect });
    this.selectbox = jQuery("<div style=\"height:200px;overflow:auto;border:solid 1px;border-color:#A9A9A9\" class=\"data-selector\"><span>Loading...</span></div>");
   	this.div = jQuery("<div class=\"facet-content\" style=\"width:100%\"></div>");
	var self = this;
	panel.setInputData(input.getId(), function() {
		var arr = [];
	    self.selectbox.find("input.x-selected").each(function() {
		    arr.push(jQuery(this).val());
	    });
		return arr;
	});
    this.div.append(freetext);
    this.div.append("<br/>");
    this.div.append(this.selectbox);
}

edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.prototype.reset = function()
{
    this.selectbox.children().remove();
    this.selectbox.append("<span>Loading...</span>");
}

edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.prototype.update = function(data)
{
	data = JSON.parse(data);
	this.selectbox.children().remove();
	var tree = edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.buildTree(data);
	jQuery(tree).css("margin-left","0px");
    this.selectbox.append(tree);
	var self = this;
	this.selectbox.find("span").click(function() {
		self.updateClick(this);
        });
	this.selectbox.find("img").click(edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.expandCollapse);
}

edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.prototype.updateClick = function(elem)
{
    var li = jQuery(elem).parents("li")[0];
    if (jQuery(jQuery(li).find("input")[0]).hasClass("x-selected"))
    {
		jQuery(jQuery(li).find("input")[0]).removeClass("x-selected");
		var padding = jQuery(li).css("padding-left");
        jQuery(li).attr("style","background:inherit;border:none");	
		jQuery(li).css("padding-left",padding);
    }
    else
    {
        jQuery(jQuery(li).find("input")[0]).addClass("x-selected");
		var padding = jQuery(li).css("padding-left");
		jQuery(li).attr("style","background:#C7DFFC;border:1px solid white;");
		jQuery(li).css("padding-left",padding);
    }
    this.panel.notify();
}

edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.autocompleteSource = function(term, callback)
{
    var t = term;
    var input = this.element;
    var parent = jQuery(input).parent();
    var options = jQuery(input).parent().find("li");
    var data = [];
    for (var i = 0; i < options.length; ++i)
	{
	    var o = jQuery(options[i]);
	    if (jQuery(o).find("span").html().toLowerCase().indexOf(term.term.toLowerCase()) > -1)
		{
		    var j = 0;
		    for ( ; j < data.length; ++j) {
			 if (data[j]["myval"] == jQuery(o).find("input").val()) {
			    	break;
			 }
		    }
		    if ( j == data.length )
		    {
			data.push({"label":jQuery(o).find("span").html(),"myval":jQuery(o).find("input").val(),"value":"","option":jQuery(o).find("span")[0]});
		    }
		}
	}
    callback(data);
}

edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.autocompleteSelect = function(event, ui)
{
    var item = ui.item;
    jQuery(item.option).click();
    event.target.value = '';
    event.stopPropagation();
}

edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.buildTree = function(tree, root)
{
    var list = jQuery("<ul style=\"list-style-type:none;padding-left:0px;margin-left:15px;\"></ul>");
    if (tree != null)
    {
		for (var i = 0; i < tree.length; ++i)
		{
	    	if (tree[i]['parent'] == root) 
	    	{
				var item = tree[i];
				var expandCollapsePlus = "http://aquarius.tw.rpi.edu/s2s/2.0/ui/images/ExpandCollapsePlus.png";
				var expandCollapseMinus = "http://aquarius.tw.rpi.edu/s2s/2.0/ui/images/ExpandCollapseMinus.png";
				var li = jQuery("<li><table><tr><td><input type=\"hidden\" value=\"" + item['id'] + "\" /><img style=\"cursor:pointer\" width=\"10\" class=\"more-icon\" src=\""+expandCollapsePlus+"\"/><img style=\"cursor:pointer\" width=\"10\" class=\"less-icon\" src=\""+expandCollapseMinus+"\"/></td><td><span title=\""+item['label']+' ('+item['count']+')'+"\"> " + item['label'] + " (" + item['count'] + ")</span></td></tr></table></li>");
				var subtree = edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.buildTree(tree,item['id']);
				li.find(".more-icon").hide();
				li.find(".less-icon").hide();
				li.find(".no-expand").hide();
				if (jQuery(subtree).children().length > 0) {
					jQuery(li).append(subtree);
                                        subtree.hide();
					jQuery(li).addClass("more");
					jQuery(jQuery(li).find(".more-icon")[0]).show();
				}
				else {
					jQuery(li).addClass("leaf");
					jQuery(li).css("padding-left","10px");
				}
				jQuery(list).append(li);
	    	}
		}
    }
    return list;
}

edu.rpi.tw.sesf.s2s.widgets.HierarchyFacetedSelect.expandCollapse = function(eventObject)
{
	var obj = jQuery(eventObject.target).parents("li")[0];
    if (jQuery(obj).hasClass("less")) {
		jQuery(obj).children("ul").hide(); jQuery(obj).addClass("more").removeClass("less");
		jQuery(jQuery(obj).find(".less-icon")[0]).hide(); jQuery(jQuery(obj).find(".more-icon")[0]).show();
    }
    else if (jQuery(obj).hasClass("more")) {
		jQuery(obj).children("ul").show(); jQuery(obj).addClass("less").removeClass("more");
		jQuery(jQuery(obj).find(".less-icon")[0]).show(); jQuery(jQuery(obj).find(".more-icon")[0]).hide();
    }
    eventObject.stopPropagation();
}