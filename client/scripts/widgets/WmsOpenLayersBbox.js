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
edu.rpi.tw.sesf.s2s.widgets.WmsOpenLayersBoundingBox = function(panel) {
    this.panel = panel;
    var input = panel.getInput();
    this.div = jQuery("<div class=\"facet-content\"><span><button style=\"float: left;\" class=\"clear-button\">Clear</button></span><span><button style=\"float: right;\" class=\"map-button\">Use Map</button></span><br/></div>");
    var table = jQuery("<table></table>");
    var row1 = jQuery("<tr><td><span>North:</span></td><td><input style=\"width: 100px;\" class=\"gbb north\" type=\"text\"/></td></tr>");
    var row2 = jQuery("<tr><td><span>East:</span></td><td><input style=\"width: 100px;\" class=\"gbb east\" type=\"text\"/></td></tr>");
    var row3 = jQuery("<tr><td><span>West:</span></td><td><input style=\"width: 100px;\" class=\"gbb west\" type=\"text\"/></td></tr>");
    var row4 = jQuery("<tr><td><span>South:</span></td><td><input style=\"width: 100px;\" class=\"gbb south\" type=\"text\"/></td></tr>");
    var dialog = jQuery("<div class=\"map-dialog\" title=\"Bounding Box: (Hold Shift and Draw Box)\"><div id=\"openlayers-map-canvas\" style=\"width: inherit; height: 100%;\"></div></div>");
    jQuery(this.div).append(jQuery(table).append(row1).append(row2).append(row3).append(row4));
    jQuery(this.div).append(dialog);
    //jQuery(dialog).dialog({autoOpen: false, width: 500, height: 400 });
    //set up dialog box with OpenLayers map
    var self = this; //Added by Benno Lee
    jQuery(this.div).find(".map-button").click(function() {
        //$(dialog).dialog('open');
        if (jQuery("#openlayers-map-canvas").children().length == 0) {
            jQuery(dialog).css("height", "200px");
       	    self.config();
	    jQuery(self.div).append(jQuery("<div>(Hold shift and draw a bounding box)</div>"));
	}
    });
	
	//set up function to get widget data
    panel.setInputData(input.getId(), function() {
        //var self = this; 
        var bool = true;
        var str = "";
        jQuery(self.div).find(".gbb").each(function() { if (jQuery(this).val() == "") bool = false; });
        if (bool) 
        {
            var boxed = true;
            jQuery(self.div).find(".gbb").each(function() { if (jQuery(this).val() == "") boxed = false; });
            if (boxed) 
            {
                var west = jQuery(self.div).find(".west").val();
                var south = jQuery(self.div).find(".south").val();
                var east = jQuery(self.div).find(".east").val();
                var north = jQuery(self.div).find(".north").val();
                str = west + ',' + south + ',' + east + ',' + north;
                //str = jQuery(this.div).find(".west").val() + ',' + jQuery(this.div).find(".south").val() + ',' + jQuery(this.div).find(".east").val() + ',' + jQuery(this.div).find(".north").val();
            }
        }
        return str;
    });
	
    //update if inputs changed
    var self = this;
    jQuery(this.div).find(".gbb").change(function() {
		panel.notify();
    });

    jQuery(this.div).find(".clear-button").click(function() {
        jQuery(self.div).find(".gbb").each(function() { jQuery(this).val('').change(); });
    });
}

edu.rpi.tw.sesf.s2s.widgets.WmsOpenLayersBoundingBox.prototype.getState = function()
{
	return this.state;
}

edu.rpi.tw.sesf.s2s.widgets.WmsOpenLayersBoundingBox.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.WmsOpenLayersBoundingBox.prototype.config = function() {
	var lon = 0;
	var lat = 0;
	var zoom = 1;
	var map, layer;
	var epsg900913 = new OpenLayers.Projection("EPSG:900913");
	var epsg4326 = new OpenLayers.Projection("EPSG:4326");
	
	map = new OpenLayers.Map({
	    div: 'openlayers-map-canvas',
	    displayProjection: epsg4326 
	});
	//layer = new OpenLayers.Layer.WMS( "OpenLayers WMS", "http://vmap0.tiles.osgeo.org/wms/vmap0", {layers: 'basic'} );
	layer = new OpenLayers.Layer.OSM("OpenStreetMap");	

	var control = new OpenLayers.Control();
	var self = this;
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
 			var ll = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.bottom)).transform(epsg900913, epsg4326);
			var ur = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.top)).transform(epsg900913, epsg4326);
			jQuery(self.div).find(".west").val(ll.lon.toFixed(4));
			jQuery(self.div).find(".south").val(ll.lat.toFixed(4));
			jQuery(self.div).find(".east").val(ur.lon.toFixed(4));
			jQuery(self.div).find(".north").val(ur.lat.toFixed(4));
			jQuery(self.div).find(".north").change();
		}
	});
	
	map.addLayer(layer);
	map.addControl(control);
	map.addControl(new OpenLayers.Control.MousePosition());
	map.setCenter(new OpenLayers.LonLat(lon, lat).transform(epsg900913, epsg4326), zoom);
}

