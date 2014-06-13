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
edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect = function(panel) 
{
	var input = widget.getInput();
	this.panel = panel;
    var freetext = jQuery("<input type=\"text\"/>");
    jQuery(freetext).autocomplete({source: edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.autocompleteSource, select: edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.autocompleteSelect });
    this.selectbox = jQuery("<div style=\"height:200px;overflow:auto;border:solid 1px;border-color:#A9A9A9\" class=\"data-selector\"><span>Loading...</span></div>");
    this.div = jQuery("<div class=\"facet-content\" style=\"width:100%\"></div>");
	var self = this;
	panel.setInputData(input.getId(), function() {
		var arr = [];
	    self.selectbox.find("input:checked").each(function() {
		    arr.push(jQuery(this).val());
	    });
		return arr;
	});
    this.div.append(freetext);
    this.div.append("<br/>");
    this.div.append(this.selectbox);
}

edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.prototype.reset = function()
{
    this.selectbox.children().remove();
    this.selectbox.append("<span>Loading...</span>");
}

edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.prototype.update = function(data)
{
	data = JSON.parse(data);
	this.selectbox.children().remove();
	var tree = edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.buildTree(data['tree']);
	//edit this
	edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.filterTree(jQuery(tree).find("li")[0], data['data']);
	jQuery(tree).css("margin-left","0px");
	this.selectbox.append(tree);
	var self = this;
	this.selectbox.find("input").click(function() {
		self.panel.notify()
	});
	this.selectbox.find("li").click(edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.expandCollapse);
}

edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.autocompleteSource = function(term, callback)
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
				if (data[j]["myval"] == jQuery(o[i]).val()) {
			    	break;
				}
		    }
		    if ( j == data.length )
			{
			    data.push({"label":o.parent().children("a").html(),"myval":jQuery(o[i]).val(),"value":"","option":o[0]});
			}
		}
	}
    callback(data);
}

edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.autocompleteSelect = function(event, ui)
{
    var item = ui.item;
    item.option.checked = true;
    jQuery(item.input).click();
    event.target.value = '';
    event.stopPropagation();
}

edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.buildTree = function(tree, root)
{
	var list = jQuery("<ul style=\"list-style-type:none;padding-left:0px;margin-left:15px;\"></ul>");
    if (tree != null)
    {
		for (var i = 0; i < tree.length; ++i)
		{
	    	if (tree[i]['parent'] == root) 
	    	{
				//var item = tree.splice(i--,1)[0];
				var item = tree[i];
				var li = jQuery("<li><table><tr><td><input type=\"checkbox\" value=\"" + item['id'] + "\" /></td><td><span>(" + item['count'] + ") " + item['label'] + "</span></td></tr></table></li>");
				var subtree = edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.buildTree(tree,item['id']);
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

edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.expandCollapse = function(eventObject)
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

edu.rpi.tw.sesf.s2s.widgets.FullHierarchySelect.expandCollapse.filterTree = function(root, data)
{
	var rootCount = 0;
	//get count for root from data (must loop over data)
	for (var i in data)
	{
		var item = data[i];
		if (item["id"] == jQuery(root).find("input").val())
		{
			rootCount = item.count;
		}
	}
	
	//if root is a leaf node
	if ( jQuery(root).hasClass("leaf") )
	{
		if ( rootCount == 0 ) { jQuery(root).remove(); }//delete me if 0
		return rootCount;
	}
	
	//else get count of children
	else
	{
		var childCount = 0;
		//loop through children
		jQuery(root).find("li").each(function() {
			childCount +=fullHierarchySelectFilterTree(this, data);
		});
		//convert current root to leaf if childCount is 0
		if ( childCount == 0 ) 
		{
			jQuery(root).find("ul").remove();
			if ( rootCount == 0 ) { jQuery(root).remove(); }
			else { jQuery(root).addClass("leaf"); }
		}
		return childCount + rootCount;
	}
}