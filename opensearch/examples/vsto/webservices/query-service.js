if (window.org == undefined || typeof(org) != "object") org = {};
if (org.vsto == undefined || typeof(org.vsto) != "object") org.vsto = {};
if (org.vsto.webservice == undefined || typeof(org.vsto.webservice) != "object") org.vsto.webservice = {};

org.vsto.webservice.s2sProxyService = "http://aquarius.tw.rpi.edu/s2s/2.0/services/proxy.php";
org.vsto.webservice.s2sMetadataService = "http://aquarius.tw.rpi.edu/s2s/2.0/services/metadata.php";
org.vsto.webservice.openSearchDocumentParsingService = "http://leo.tw.rpi.edu/projects/nahgnaw/vsto_web_services/opensearch-doc-parser.php";
org.vsto.webservice.vstoOpenSearchService = "http://aquarius.tw.rpi.edu/s2s/VSTO/search.php";
org.vsto.webservice.vstoOpenSearchDocumentURL = "http://aquarius.tw.rpi.edu/s2s/VSTO/2.0/opensearch.xml";
org.vsto.webservice.vstoOpenSearchInstrumentsQuery = "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/InstrumentsQuery";
org.vsto.webservice.vstoOpenSearchParametersQuery = "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/ParametersQuery";
org.vsto.webservice.vstoOpenSearchInstrumentClassesQuery = "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/InstrumentClassesQuery";
org.vsto.webservice.vstoOpenSearchParameterClassesQuery = "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/ParameterClassesQuery";
org.vsto.webservice.vstoOpenSearchDatesQuery = "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/DatesQuery";
org.vsto.webservice.vstoOpenSearchResultsQuery = "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/VstoResultsQuery";
org.vsto.webservice.postData = {"service":null, "query":null, "parameters":{}};

function loadMetadata(divID, serviceURI) 
{
	$j = jQuery.noConflict();
	jQuery.support.cors = true;
	jQuery("#" + divID).children().remove();
	
	var metadataServiceURL = org.vsto.webservice.s2sMetadataService + "?type=services&instances=" + escape(serviceURI);	
	var callback = function(response)
	{
		var data = jQuery.parseJSON(response);
		if (data == null)
			console.error("Failed to get metadata for SearchService."); 
		var intro = jQuery("<div style='margin:10px auto'>The following are the web services available through a OpenSearch \
			interface designed and developed for use by the S2S Faceted Search capabilities.</div>");
		var serviceTable = 
			jQuery("<table border='1px solid grey' style='font-size:12px;border-collapse:collapse;width:580px;margin:5px auto'>\
			<tr><th colspan='2' style='background-color:#1d00cb;color:#ffffff'>Service</th></tr></table>");
		var parameterTable = 
			jQuery("<table border='1px solid grey' style='font-size:12px;border-collapse:collapse;width:580px;margin:5px auto'>\
			<tr><th colspan='2' style='background-color:#1d00cb;color:#ffffff'>Parameters</th></tr></table>");
		var queryTable = 
			jQuery("<table border='1px solid grey' style='font-size:12px;border-collapse:collapse;width:580px;margin:5px auto'>\
			<tr><th colspan='2' style='background-color:#1d00cb;color:#ffffff'>Queries</th></tr></table>");
		var service = data[serviceURI]["service"];
		var parameters = data[serviceURI]["parameters"];
		var queries = data[serviceURI]["queries"];

		for (var i in service) {
			var tr = jQuery("<tr><td>" + i + "</td>" + "<td>" + service[i] + "</td></tr>");
			jQuery(serviceTable).append(tr);
		}
		for (var i in parameters) {
			jQuery(parameterTable)
				.append("<tr><td colspan='2' style='background-color:#c0bdff'>" + parameters[i]["label"] + "</td></tr>");
			for (var j in parameters[i]) {
				var tr = jQuery("<tr><td>" + j + "</td>" + "<td>" + parameters[i][j] + "</td></tr>");
				jQuery(parameterTable).append(tr);
			}
		}
		for (var i in queries) {
			jQuery(queryTable).append("<tr><td colspan='2' style='background-color:#c0bdff'>" + queries[i]["uri"] + "</td></tr>");
			for (var j in queries[i]) {
				var tr = jQuery("<tr><td>" + j + "</td>" + "<td>" + queries[i][j] + "</td></tr>");
				jQuery(queryTable).append(tr);
			}
		}
		if (jQuery("#" + divID)) {
			jQuery("#" + divID).append(intro).append(serviceTable).append(parameterTable).append(queryTable);
		}
		else 
			alert(divID + "not found!");
	};
	org.vsto.webservice.ajax("GET", metadataServiceURL, "", callback);
}


function loadQuery(divID, serviceURI, queryURI) 
{
	$j = jQuery.noConflict();
    jQuery.support.cors = true;
	jQuery("#" + divID).children().remove();
	
	org.vsto.webservice.postData["service"] = serviceURI;
	var parsingServiceURL = 
		org.vsto.webservice.openSearchDocumentParsingService + "?request=" + 
		escape(org.vsto.webservice.vstoOpenSearchDocumentURL) + "&q=" + escape(queryURI);
	var callback = function(response)
	{
		var data = jQuery.parseJSON(response);
		org.vsto.webservice.postData["query"] = data["query"]["uri"];
		
		var constraints = jQuery("<div id='query-constraints' style='float:left;margin:5px'></div>")
			.append("<div id='query-title' style='text-align:center;font-size:16px'><b>" + data.query.label + "</b></div>");
		jQuery("#" + divID).append(constraints);
		
		jQuery.each(data.query.parameters, function(index, element) {
			if (index == "start" || index == "ndays") {
				org.vsto.webservice.getTextInput(constraints, index, element);
			}
			if (index == "instrumentClasses" || index == "parameterClasses") {
				var cb = function(r)
				{
					var d = jQuery.parseJSON(r);
					org.vsto.webservice.getHierarchyFacetedSelect(constraints, index, element, d.tree);
				};
				org.vsto.webservice.getOpenSearchServiceData(index, cb);
			}	
			if (index == "instruments" || index == "parameters") {
				var cb = function(r)
				{
					var d = jQuery.parseJSON(r);
					org.vsto.webservice.getFacetedSelect(constraints, index, element, d);
				};
				org.vsto.webservice.getOpenSearchServiceData(index, cb);
			}	
		});	
		// Submit button
		var button = jQuery("<div id='submit' style='text-align:right;margin:5px'><button type='button'>submit</button></div>");
		jQuery("#query-title").after(button);
		jQuery(button).click(function() {
			// Generate service URL for query
			org.vsto.webservice.getQueryServiceURL(serviceURI);
			// Get query results
			org.vsto.webservice.getQueryResults();
			// Clear previous input values on UI
			org.vsto.webservice.resetUI(data.query.parameters);
			// Reset the post data
			org.vsto.webservice.postData["parameters"] = {};
		});	
	};
	org.vsto.webservice.ajax("GET", parsingServiceURL, "", callback);
}

org.vsto.webservice.getFacetedSelect = function(div, index, element, data)
{
	
	var select = 
			jQuery("<select name='" + index + "' multiple='multiple' style='width:260px;height:120px'></select>");

	for (var i = 0; i < data.length; i++) {
		var option = jQuery("<option value=" + data[i].id + ">" + data[i].label + "</option>");
		jQuery(select).append(option);
	}
	var alias = jQuery("<div class='alias' style='background-color:#c0bdff'>" + index + "</div>");
	var input = jQuery("<div class='input' style='font-size:8px'></div>").append(select);
	var widget = jQuery("<div class='widget' id='" + index + "' style='width:260px;margin-bottom:10px'></div>")
		.append(alias).append(input);
	jQuery(div).append(widget);
	
	// Set the post data 
	jQuery(select).change(function() {
		var obj = {};
		jQuery("#" + index).find("option:selected").each(function(key) {
			obj[key] = jQuery(this).val();
		});
		org.vsto.webservice.postData["parameters"][element] = obj;
	});
};

org.vsto.webservice.getTextInput = function(div, index, element)
{
	if (index == "start")
		var alias = jQuery("<div class='alias' style='background-color:#c0bdff'>" + index + " (yyyy-mm-dd)</div>");
	if (index == "ndays")
		var alias = jQuery("<div class='alias' style='background-color:#c0bdff'>" + index + " (e.g. 15)</div>");
	var text = jQuery("<input style='width:256px' type='text' name='" + index + "'>");
	var input = 
		jQuery("<div class='input'></div>").append(text);
	var widget = jQuery("<div class='widget' id='" + index + "' style='width:260px;margin-bottom:10px'></div>")
		.append(alias).append(input);
	jQuery(div).append(widget);
	
	// Set the post data 
	jQuery(text).change(function() {
		var obj = {};
		jQuery("#" + index).find("input[type='text']").each(function() {
			obj["0"] = jQuery(this).val();
		});
		org.vsto.webservice.postData["parameters"][element] = obj;
	});
};

org.vsto.webservice.getHierarchyFacetedSelect = function(div, index, element, data)
{
	var tree = org.vsto.webservice.buildHierarchyTree(data, index);
	jQuery(tree).css({"margin-left": "0px", "margin-top": "0px"});
	var alias = jQuery("<div class='alias' style='background-color:#c0bdff'>" + index + "</div>");
	var input = 
		jQuery("<div class='input' style='width:258px;height:120px;overflow:auto;border:solid 1px;border-color:gray;font-size:8px'></div>")
			.append(tree);
	var widget = jQuery("<div class='widget' id='" + index + "' style='width:260px;margin-bottom:10px'></div>")
		.append(alias).append(input);
	jQuery(div).append(widget);
	
	// Set the post data	
	jQuery(tree).change(function() {
		var obj = {};
		jQuery("#" + index).find("input:checked").each(function(key) {
			obj[key] = jQuery(this).val();
		});
		org.vsto.webservice.postData["parameters"][element] = obj;
	});
};

org.vsto.webservice.buildHierarchyTree = function(tree, param, root)
{
	var list = jQuery("<ul style='list-style-type:none;padding-left:0px;margin-left:15px'></ul>");
    if (tree != null)
    {
		for (var i = 0; i < tree.length; ++i)
		{
	    	if (tree[i]["parent"] == root) 
	    	{
				var item = tree.splice(i--,1)[0];
				var li = 
					jQuery("<li><input type='checkbox' name='" + param + "' value='" + item["id"] + "'>"+ 
					item["label"] + "</input></li>");
				var subtree = org.vsto.webservice.buildHierarchyTree(tree, param, item["id"]);
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
};

org.vsto.webservice.getOpenSearchServiceData = function(param, callback)
{
	var openSearchServiceURL = org.vsto.webservice.vstoOpenSearchService + "?request=" + param;
	org.vsto.webservice.ajax("GET", openSearchServiceURL, "", callback);
};

org.vsto.webservice.getQueryServiceURL = function(serviceURI)
{
	var service = "";
	var type = "";
	var params = "";
	var url = "";
	if (serviceURI == "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/VstoSearchService")
		service = org.vsto.webservice.vstoOpenSearchService;
	switch (org.vsto.webservice.postData["query"])
	{
		case org.vsto.webservice.vstoOpenSearchInstrumentsQuery:
			type = "instruments";
			break;
		case org.vsto.webservice.vstoOpenSearchParametersQuery:
			type = "parameters";
			break;
		case org.vsto.webservice.vstoOpenSearchInstrumentClassesQuery:
			type = "instrumentClasses";
			break;
		case org.vsto.webservice.vstoOpenSearchParameterClassesQuery:
			type = "parameterClasses";
			break;
		case org.vsto.webservice.vstoOpenSearchDatesQuery:
			type = "dates";
			break;	
		case org.vsto.webservice.vstoOpenSearchResultsQuery:
			type = "results";
			break;
	}
	jQuery.each(org.vsto.webservice.postData["parameters"], function(index, element) {
		switch (index)
		{
			case "http://a9.com/-/opensearch/extensions/time/1.0/start":
				params += "&start=";
				break;
			case "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/NumDays":
				params += "&ndays=";
				break;
			case "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/Instruments":
				params += "&instruments=";
				break;
			case "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/Parameters":
				params += "&parameters=";
				break;
			case "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/InstrumentClasses":
				params += "&instrumentClasses=";
				break;
			case "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/ParameterClasses":
				params += "&parameterClasses=";
				break;
		}
		jQuery.each(element, function(ind, ele) {
			params += escape(ele) + ",";
		});
	});
	url = org.vsto.webservice.vstoOpenSearchService + "?request=" + type + params;
	
	jQuery("#query-constraints").next().remove();
	jQuery("#query-constraints")
		.after("<div id='query-results' style='float:left;margin-left:15px;margin-bottom:10px;word-wrap:break-word'></div>");
	jQuery("#query-results")
		.append("<div id='service-url' style='width:445px;margin-bottom:10px;padding:3px'>\
		<b>Web Service URL:</b><br /><i><small><a target='_blank' href='" + url + "'>" + url + "</a></small></i></div>");
}

org.vsto.webservice.getQueryResults = function()
{
	console.log(JSON.stringify(org.vsto.webservice.postData));
	jQuery("#service-url")
		.after("<div id='results' style='height:640px;overflow-x:auto'><div style='text-align:center'>Loading...</div></div>");
	var cb = function(r)
	{
		var results = jQuery("<table style='width:432px;font-size:12px;table-layout:fixed' border='1px solid grey'></table>");
		
		var data = jQuery.parseJSON(r);
		if (data == "" || data["data"] == "")
			jQuery(results).append("<tr><th style='background-color:#1d00cb;color:#ffffff'>Query Results</th></tr>")
				.append("<tr><td align='center'><i>No result!</i></td></tr>");
		else {
			if (org.vsto.webservice.postData["query"] == org.vsto.webservice.vstoOpenSearchInstrumentClassesQuery ||
				org.vsto.webservice.postData["query"] == org.vsto.webservice.vstoOpenSearchParameterClassesQuery) {
				jQuery(results).append("<tr><th style='background-color:#1d00cb;color:#ffffff'>Query Results</th></tr>")
					.append("<tr><td style='background-color:#c0bdff;text-align:center'><b>ID</b></td></tr>");
				for (var i = 0; i < data["data"].length; i++) {
					var tr = jQuery("<tr></tr>").append("<td style='word-wrap:break-word'>" + data["data"][i].id + "</td>");
					jQuery(results).append(tr);
				}
			}
			else if (org.vsto.webservice.postData["query"] == org.vsto.webservice.vstoOpenSearchDatesQuery) {
				jQuery(results).append("<tr><th style='background-color:#1d00cb;color:#ffffff'>Query Results</th></tr>")
					.append("<tr><td style='background-color:#c0bdff;text-align:center'><b>Dates</b></td></tr>");
				for (var i = 0; i < data.length; i++) {
					var tr = jQuery("<tr></tr>").append("<td>" + data[i].date + "</td>");
					jQuery(results).append(tr);
				}
			}
			else if (org.vsto.webservice.postData["query"] == org.vsto.webservice.vstoOpenSearchResultsQuery) {
				jQuery(results).append("<tr><th style='background-color:#1d00cb;color:#ffffff'>Query Results</th></tr>");
				for (var i = 0; i < data.length; i++) {
					var tr = jQuery("<tr></tr>");
					var td = jQuery("<td></td>");
					var products = jQuery("<ul style='list-style-type:none;margin:0px;padding-left:20px'></ul>");
					for (var j = 0; j < data[i]["products"].length; j++) {
						jQuery(products)
							.append("<li style='display:inline;padding-right:10px'><i><small>" + data[i]["products"][j] + "</small></i></li>");
					}
					var inst = jQuery("<div>Instrument: <i><small>" + data[i]["inst"] + "</small></i></div>");
					var dateRange = 
						jQuery("<div>Date Range: <i><small>" + data[i]["begin_date"] + "</small></i> to <i><small>" + 
						data[i]["end_date"] + "</small></i></div>");
					jQuery(td).append("<div>Products:</div>").append(products).append(inst).append(dateRange);
					jQuery(tr).append(td);
					jQuery(results).append(tr);
				}
			}
			else {
				jQuery(results).append("<col width=20%><col width=80%>").append("<tr><th colspan='2' style='background-color:#1d00cb;color:#ffffff'>Query Results</th></tr>")
					.append("<tr><td style='background-color:#c0bdff'><b>ID</b></td>\
					<td style='background-color:#c0bdff'><b>Label</b></td></tr>");
				for (var i = 0; i < data.length; i++) {
					var tr = jQuery("<tr></tr>").append("<td>" + data[i].id + "</td>").append("<td>" + data[i].label + "</td>");
					jQuery(results).append(tr);
				}
			}
		}
		jQuery("#results").children().remove();
		jQuery("#results").append(results);
	};
	org.vsto.webservice.ajax("POST", org.vsto.webservice.s2sProxyService, JSON.stringify(org.vsto.webservice.postData), cb);
};

org.vsto.webservice.resetUI = function(object)
{
	jQuery.each(object, function(index) {
		if (index == "start" || index == "ndays")
			jQuery("div#" + index).find("input[type='text']").val("");
		if (index == "instrumentClasses" || index == "parameterClasses") 	
			jQuery("div#" + index).find("input:checked").removeAttr("checked");
		if (index == "instruments" || index == "parameters") 
			jQuery("div#" + index).find("option:selected").removeAttr("selected");
	});
}

org.vsto.webservice.ajax = function(method, url, data, callback)
{
	if (jQuery.browser.msie && parseInt(jQuery.browser.version, 10) >= 7 && window.XDomainRequest) {
		// Use Microsoft XDR
		var xdr = new XDomainRequest();
		xdr.open(method,url);
		xdr.onload = function () {
			callback(xdr.responseText);
		}
		xdr.send(data);
	} 
	else
		jQuery.ajax({
			url: url,
			type: method,
			dataType: "text",
			data: data,
			success: callback
		});
};