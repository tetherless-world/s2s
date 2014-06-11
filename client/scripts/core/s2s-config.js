(function(s2s,$) {
	/**
	 * Globals
	 */
	s2s.utils.servletRoot = "http://aquarius.tw.rpi.edu/projects/s2s/tomcat/s2s/";
	s2s.utils.metadataService = s2s.utils.servletRoot + "metadata";
	s2s.utils.proxyService = s2s.utils.servletRoot + "proxy";
	s2s.utils.sessionService = "http://aquarius.tw.rpi.edu/projects/s2s/tomcat/s2s/session";
	s2s.utils.searchWidgetClass = "http://escience.rpi.edu/ontology/sesf/s2s/4/0/InputWidget";
	s2s.utils.resultsWidgetClass = "http://escience.rpi.edu/ontology/sesf/s2s/4/0/ResultsWidget";
	s2s.utils.resultsQueryUri = "http://escience.rpi.edu/ontology/sesf/s2s/4/0/SearchResultsInterface";
	s2s.utils.hierarchicalSearch = "http://escience.rpi.edu/ontology/sesf/s2s-core/4/0/HierarchicalSearch";
	s2s.utils.facetedSearch = "http://escience.rpi.edu/ontology/sesf/s2s-core/4/0/FacetedSearch";

	/**
	 * Built-in behavior for list-style results
	 */
	s2s.utils.limitInput = "http://a9.com/-/spec/opensearch/1.1/count";
	s2s.utils.offsetInput = "http://a9.com/-/spec/opensearch/1.1/startIndex";
	s2s.utils.defaultLimit = 10;
	s2s.utils.defaultOffset = 0;
	
	/**
	 * Other settings
	 */
	$.support.cors = true;
})(edu.rpi.tw.sesf.s2s,jQuery);
