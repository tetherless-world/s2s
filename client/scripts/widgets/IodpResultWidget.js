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
edu.rpi.tw.sesf.s2s.widgets.IodpResultWidget = function(panel) {
	this.panel = panel;
	var html = '<table id="rounded-corner" summary="Log DB results" style="margin-top:15px;margin-bottom:15px;margin-left:0px;width:800px" name="tshow" class="tshow">' +
		'<thead><tr>' +
		'<th scope="col" class="rounded-company">Resource</th>' +
		'<th scope="col" class="rounded-q1">Title</th>' +
		'<th scope="col" class="rounded-q4">Tool</th>' +
		'</tr></thead>' +
		'<tbody id="show"></tbody>' +
		'<tfoot><tr>' +
		'<td colspan="2" class="rounded-foot-left"><em>Log File Data from LDEO</em></td>' +
		'<td class="rounded-foot-right">&nbsp;</td>' +
		'</tr></tfoot></table>';
	this.div = jQuery(html);
}

edu.rpi.tw.sesf.s2s.widgets.IodpResultWidget.prototype.get = function()
{
	return this.div;
}

edu.rpi.tw.sesf.s2s.widgets.IodpResultWidget.prototype.reset = function()
{
	jQuery(this.div).find(".html").children().remove()
	jQuery(this.div).find(".html").append("<span>Loading...</span>");
	jQuery(this.div).find(".page").children().remove();
	this.limit = this.panel.getInterface().getDefaultLimit();
	this.offset = 0;
}

edu.rpi.tw.sesf.s2s.widgets.IodpResultWidget.prototype.update = function(data)
{
    data = JSON.parse(data);
    var textToInsert = '';
    var parent = jQuery(this.div).find('#show');

    jQuery("#rounded-corner tr").remove(".rdata");  //  remove rdata class elements from any previous search

    jQuery.each(data, function (count, value) {
		var parts = data[count].uri.split("/");    //  get the URL, split it up so I can put the last element in the A tag below
		var toolparts = data[count].tool.split("/");
		textToInsert += '<tr class="rdata">' +
			'<td style="width:300px"><a target="_blank" href="' + data[count].uri + '">' + parts[parts.length - 1] + '</a>' +
			'<span style="font-size: 12px;color:#999900;"><a style="font-size: 12px;color:#999900;" target="_blank" href="http://data.oceandrilling.org:8890/describe/?url=' + data[count].uri + '">Describe Resource</a></span></td>' +
			'<td>' + data[count].title + '</td>' +
			'<td style="width:200px"> <a target="_blank" href="' + data[count].tool + '">' + toolparts[toolparts.length - 1] + '</a>' +
			'<span style="font-size: 12px;color:#999900;"><a style="font-size: 12px;color:#999900;" target="_blank" href="http://data.oceandrilling.org:8890/describe/?url=' + data[count].tool + '"> Describe Resource</a></span></td></tr>';
	});
	
    jQuery(textToInsert).appendTo(parent);
}