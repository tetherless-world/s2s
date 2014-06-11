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
edu.rpi.tw.sesf.s2s.widgets.OpenLayersBoundingBox = function(panel) {
    this.panel = panel;
	var input = panel.getInput();
    this.div = jQuery("<div class=\"facet-content\"><span><button style=\"float: right;\" class=\"map-button\">Use Map</button></span></div>");
    var table = jQuery("<table></table>");
    var row1 = jQuery("<tr><td><span>North:</span></td><td><input style=\"width: 100px;\" class=\"gbb north\" type=\"text\"/></td><td><span>East:</span></td><td><input style=\"width: 100px;\" class=\"gbb east\" type=\"text\"/></td></tr>");
    var row2 = jQuery("<tr><td><span>West:</span></td><td><input style=\"width: 100px;\" class=\"gbb west\" type=\"text\"/></td><td><span>South:</span></td><td><input style=\"width: 100px;\" class=\"gbb south\" type=\"text\"/></td></tr>");
    var dialog = jQuery("<div class=\"map-dialog\" title=\"Bounding Box: (Hold Shift and Draw Box)\"><div id=\"openlayers-map-canvas\" style=\"width: inherit; height: 90%;\"></div></div>");
    jQuery(this.div).append(jQuery(table).append(row1).append(row2));
    jQuery(this.div).append(dialog);
    jQuery(dialog).dialog({autoOpen: false, width: 500, height: 400 });
    
    //set up dialog box with OpenLayers map
    jQuery(this.div).find(".map-button").click(function() {
        $(dialog).dialog('open');
       	this.config();
    });
	
	//set up function to get widget data
	panel.setInputData(input.getId(), function() {
		var self = this; 
		var bool = true;
		var str = "";
		jQuery(this.div).find(".gbb").each(function() { if (jQuery(this).val() == "") bool = false; });
		if (bool) 
		{	
	    	var boxed = true;
	    	jQuery(this.div).find(".gbb").each(function() {
				if (jQuery(this).val() == "") boxed = false;
	    	});
	    	if (boxed) 
			{
				str = jQuery(this.div).find(".west").val() + ',' + jQuery(div).find(".south").val() + ',' + jQuery(div).find(".east").val() + ',' + jQuery(div).find(".north").val());
			}
		}
		return str;
	});
	
    //update if inputs changed
	var self = this;
    jQuery(this.div).find(".gbb").change(function() {
		panel.notify();
    });
}

edu.rpi.tw.sesf.s2s.widgets.OpenLayersBoundingBox.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.OpenLayersBoundingBox.prototype.config = function() {
	var lon = 0;
	var lat = 0;
	var zoom = 2;
	var map, layer;
	
	map = new OpenLayers.Map('openlayers-map-canvas');
	layer = new OpenLayers.Layer.WMS( "OpenLayers WMS", "http://vmap0.tiles.osgeo.org/wms/vmap0", {layers: 'basic'} );
	
	var control = new OpenLayers.Control();
	OpenLayers.Util.extend(control, {
		draw: function () {
			// this Handler.Box will intercept the shift-mousedown
			// before Control.MouseDefault gets to see it
			this.box = new OpenLayers.Handler.Box(control,
			{"done": this.update},
			{keyMask: OpenLayers.Handler.MOD_SHIFT});
			this.box.activate();
		},
 		update: function (bounds) {
 			var ll = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.bottom));
			var ur = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.top));
			jQuery(this.div).find(".west").val(ll.lon.toFixed(4));
			jQuery(this.div).find(".south").val(ll.lat.toFixed(4));
			jQuery(this.div).find(".east").val(ur.lon.toFixed(4));
			jQuery(this.div).find(".north").val(ur.lat.toFixed(4));
			jQuery(this.div).find(".north").change();
		}
	});
	
	map.addLayer(layer);
	map.addControl(control);
	map.addControl(new OpenLayers.Control.MousePosition());
	map.setCenter(new OpenLayers.LonLat(lon, lat), zoom);
}
