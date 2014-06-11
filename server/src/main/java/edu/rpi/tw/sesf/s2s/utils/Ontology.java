package edu.rpi.tw.sesf.s2s.utils;

public class Ontology {
	/**
	 * Namespaces
	 */
	private static String s2sNS = Namespace.s2s.getURI();
	private static String coreNS = Namespace.core.getURI();
	private static String rdfsNS = Namespace.rdfs.getURI();
	private static String rdfNS = Namespace.rdf.getURI();
	private static String facetNS = Namespace.facet.getURI();
	private static String opensearchNS = Namespace.opensearch.getURI();
	
	/**
	 * Datatype Properties
	 */
	public static String hasDefaultConfiguration = coreNS + "hasDefaultConfiguration";
	public static String hasDefaultLimit = coreNS + "hasDefaultLimit";
	public static String hasDelimiter = coreNS + "hasDelimiter";
	public static String hasJavaScriptPrototype = coreNS + "hasJavaScriptPrototype";
	public static String hasOpenSearchDescriptionDocument = coreNS + "hasOpenSearchDescriptionDocument";
	public static String requiresJavaScript = coreNS + "requiresJavaScript";
	public static String requiresStylesheet = coreNS + "requiresStylesheet";
	public static String label = rdfsNS + "label";
	public static String comment = rdfsNS + "comment";
	public static String seeAlso = rdfsNS + "seeAlso";
	public static String location = coreNS + "location";
	public static String defaultGraph = coreNS + "defaultGraph";
	public static String format = coreNS + "format";
	
	/**
	 * Object Properties
	 */
	public static String hasOutput = s2sNS + "hasOutput";
	public static String supportsOutput = s2sNS + "supportsOutput";
	public static String supportsParadigm = s2sNS + "supportsParadigm";
	public static String supportsInput = s2sNS + "supportsInput";
	public static String hasFacetCollection = coreNS + "hasFacetCollection";
	public static String forInput = s2sNS + "forInput";
	public static String type = rdfNS + "type";
	public static String hasDataSource = coreNS + "hasDataSource";
	public static String sparqlType = coreNS + "hasSparqlType";

	
	/**
	 * Classes
	 */
	public static String Output = s2sNS + "Output";
	public static String Paradigm = s2sNS + "Paradigm";
	public static String Input = s2sNS + "Input";
	public static String Service = s2sNS + "Service";
	public static String Widget = s2sNS + "Widget";
	public static String InputWidget = s2sNS + "InputWidget";
	public static String ResultsWidget = s2sNS + "ResultsWidget";
	public static String JavaScriptWidget = coreNS + "JavaScriptWidget";
	public static String OpenSearchService = coreNS + "OpenSearchService";
	public static String FacetOntologyService = coreNS + "FacetOntologyService";
	public static String Interface = s2sNS + "Interface";
	public static String InputValuesInterface = s2sNS + "InputValuesInterface";
	public static String SearchResultsInterface = s2sNS + "SearchResultsInterface";
	public static String SparqlEndpoint =  coreNS + "SparqlEndpoint";
	public static String LinkedData = coreNS + "LinkedData";
	public static String RdfFile = coreNS + "RdfFile";
	
	/**
	 * Instances
	 */
	public static String LabelIdCountJsonArray = coreNS + "LabelIdCountJsonArray";
	public static String VirtuosoSparql = coreNS + "VirtuosoSparql";

	
	/**
	 * FacetOntology properties
	 */
	public static String facetLabel = facetNS + "label";
	public static String facetNextPredicate = facetNS + "nextpredicate";
	public static String facetClass = facetNS + "class";
	public static String facetFilter = facetNS + "filter";
	public static String facetItemLabel = facetNS + "labelpredicate";
	public static String facetFacetUri = facetNS + "faceturi";
	public static String facetFilterValue = facetNS + "filtervalue";
	public static String facetPredicate = facetNS + "predicate";
	public static String facetReverse = facetNS + "reverse";
	public static String facetType = facetNS + "type";
	public static String facetSparqlBinding = facetNS + "sparqlBinding";
	public static String facetSparqlSelVar = facetNS + "sparqlSelectVariable";
	public static String facetSparqlAggVar = facetNS + "sparqlAggregateVariable";
	public static String facetContextPredicate = facetNS + "contextpredicate";
	public static String facetSparqlGraph = facetNS + "sparqlGraph";
	public static String facetTransitive = facetNS + "transitive";
	public static String facetOptional = facetNS + "optional";
	public static String facetCodependentFacet = facetNS + "codependentfacet";
	
	/**
	 * FacetOntology classes
	 */
	public static String facetConnectedFacet = facetNS + "ConnectedFacet";
	public static String facetFacetCollection = facetNS + "FacetCollection";
	public static String facetFirstOrderFacet = facetNS + "FirstOrderFacet";
	public static String facetFilterClass = facetNS + "Filter";
	public static String facetPredicateClass = facetNS + "Predicate";
	
	/**
	 * FacetOntology instances
	 */
	public static String facetLiteralFacetType = facetNS + "TypeLiteral";
	public static String facetObjectFacetType = facetNS + "TypeObject";
	public static String facetOntologyResultsJson = facetNS + "FacetOntologyResultsJson";

	/**
	 * OpenSearch instances
	 */
	public static String opensearchCount = opensearchNS + "count";
	public static String opensearchStartIndex = opensearchNS + "startIndex";
}
