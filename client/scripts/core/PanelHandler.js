

/**
 * Defines the PanelFactory, Panel Adapter and PanelRegistry classes
 */

if (window.edu == undefined || typeof(edu) != "object") edu = {};
if (edu.rpi == undefined || typeof(edu.rpi) != "object") edu.rpi = {};
if (edu.rpi.tw == undefined || typeof(edu.rpi.tw) != "object") edu.rpi.tw = {};
if (edu.rpi.tw.sesf == undefined || typeof(edu.rpi.tw.sesf) != "object") edu.rpi.tw.sesf = {};
if (edu.rpi.tw.sesf.s2s == undefined || typeof(edu.rpi.tw.sesf.s2s) != "object") edu.rpi.tw.sesf.s2s = {};
if (edu.rpi.tw.sesf.s2s.utils == undefined || typeof(edu.rpi.tw.sesf.s2s.utils) != "object") edu.rpi.tw.sesf.s2s.utils = {};

var s2sNS = edu.rpi.tw.sesf.s2s.utils
s2sNS.PanelAdapterFactory = {
	getInstance : function() {
		return new PanelAdapter();
	}
};

s2sNS.PanelAdapter = function() {
	
};

s2sNS.PanelRegistry = function() {
	
};