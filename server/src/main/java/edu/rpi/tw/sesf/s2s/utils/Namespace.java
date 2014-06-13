package edu.rpi.tw.sesf.s2s.utils;

public enum Namespace {
	
	rdf ("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
	rdfs ("http://www.w3.org/2000/01/rdf-schema#"),
	owl ("http://www.w3.org/2002/07/owl#"),
	xsd ("http://www.w3.org/2001/XMLSchema#"),
	s2s ("http://escience.rpi.edu/ontology/sesf/s2s/4/0/"),
	facet ("http://danielsmith.eu/resources/facet/#"),
	opensearch ("http://a9.com/-/spec/opensearch/1.1/"),
	core ("http://escience.rpi.edu/ontology/sesf/s2s-core/4/0/");
	
	private String _uri;
	
	private Namespace(String uri) {
		_uri = uri;
	}
	
	public String getURI() {
		return _uri;
	}
	
	public static String getSparqlPrefixes() {
		Namespace[] curies = Namespace.values();
		String result = "";
        for (Namespace cury : curies) {
            result += "PREFIX " + cury + ": <" + cury.getURI() + ">\n";
        }
		return result;
	}
}
