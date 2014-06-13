package edu.rpi.tw.sesf.s2s.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.SparqlSource;

public class Queries {
		
	private static String prefixes = Namespace.getSparqlPrefixes();
	private static String from = "FROM <{graph}> ";
	private static String inference = "DEFINE input:inference \"{inference}\" ";
	
	private static String simpleQuery = prefixes +
			"SELECT ?o {from}WHERE { {bgp} }";
	
	/**
	 * S2S Simple Queries
	 */
	
	private static String widgetScriptsBGP = "<{uri}> <" + Ontology.requiresJavaScript + "> ?o .";
	private static String widgetStylesheetsBGP = "<{uri}> <" + Ontology.requiresStylesheet + "> ?o .";
	private static String widgetSupportBGP = "<{uri}> <" + Ontology.supportsOutput + "> ?o .";
	private static String widgetInputSupportBGP = "<{uri}> <" + Ontology.supportsInput + "> ?o .";
	private static String widgetParadigmSupportBGP = "<{uri}> <" + Ontology.supportsParadigm + "> ?o .";
	private static String labelBGP = "<{uri}> <" + Ontology.label + "> ?o .";
	private static String commentBGP = "<{uri}> <" + Ontology.comment + "> ?o .";
	private static String typeBGP = "<{uri}> <" + Ontology.type + "> ?o .";
	private static String seeAlsoBGP = "<{uri}> <" + Ontology.seeAlso + "> ?o .";
	private static String searchServiceConfigBGP = "<{uri}> <" + Ontology.hasDefaultConfiguration + "> ?o .";
	private static String searchServiceParadigmSupportBGP = "<{uri}> <" + Ontology.supportsParadigm + "> ?o .";
	private static String widgetPrototypeBGP = "<{uri}> <" + Ontology.hasJavaScriptPrototype + "> ?o .";
	private static String inputDelimiterBGP = "<{uri}> <" + Ontology.hasDelimiter + "> ?o .";
	
	/**
	 * FacetOntology Simple Queries
	 */
	
	private static String connectedFacetTestBGP = String.format("<{uri}> a <%s>", Ontology.facetConnectedFacet);
	private static String facetCollectionTestBGP = String.format("<{uri}> a <%s>", Ontology.facetFacetCollection);
	private static String filterTestBGP = String.format("<{uri}> a <%s>", Ontology.facetFilterClass);
	private static String firstOrderFacetTestBGP = String.format("<{uri}> a <%s>", Ontology.facetFirstOrderFacet);
	private static String predicateTestBGP = String.format("<{uri}> a <%s>", Ontology.facetPredicateClass);
	private static String connectedFacetLabelBGP = String.format("<{uri}> <%s> ?o", Ontology.facetLabel);
	private static String connectedFacetClassBGP = String.format("<{uri}> <%s> ?o", Ontology.facetClass);
	private static String connectedFacetContextBGP = String.format("<{uri}> <%s> ?o", Ontology.facetContextPredicate);
	private static String nextPredicateBGP = String.format("<{uri}> <%s> ?o", Ontology.facetNextPredicate);
	private static String connectedFacetFiltersBGP = String.format("<{uri}> <%s> ?o", Ontology.facetFilter);
	private static String facetItemLabelBGP = String.format("<{uri}> <%s> ?o", Ontology.facetItemLabel);
	private static String firstOrderFacetBGP = String.format("<{uri}> <%s> ?o . ?o a <%s>", Ontology.facetFacetUri, Ontology.facetFirstOrderFacet);
	private static String connectedFacetsBGP = String.format("<{uri}> <%s> ?o . ?o a <%s>", Ontology.facetFacetUri, Ontology.facetConnectedFacet);
	private static String filterValueBGP = String.format("<{uri}> <%s> ?o", Ontology.facetFilterValue);
	private static String predicateValueBGP = String.format("<{uri}> <%s> ?o", Ontology.facetPredicate);
	private static String predicateReverseBGP = String.format("<{uri}> <%s> ?o", Ontology.facetReverse);
	private static String predicateTransitiveBGP = String.format("<{uri}> <%s> ?o", Ontology.facetTransitive);
	private static String predicateOptionalBGP = String.format("<{uri}> <%s> ?o", Ontology.facetOptional);
	private static String facetSparqlBindingBGP = String.format("<{uri}> <%s> ?o", Ontology.facetSparqlBinding);
	private static String predicateSparqlGraphBGP = String.format("<{uri}> <%s> ?o", Ontology.facetSparqlGraph);
	private static String facetTypeBGP = String.format("<{uri}> <%s> ?o", Ontology.facetType);
	private static String connectedSparqlSelectVarsBGP = String.format("<{uri}> <%s> ?o", Ontology.facetSparqlSelVar);
	private static String facetSparqlAggregateVarslBGP = String.format("<{uri}> <%s> ?o", Ontology.facetSparqlAggVar);
	private static String codependentFacetBGP = String.format("<{uri}> <%s> ?o", Ontology.facetCodependentFacet);

	/**
	 * Complex Queries
	 */

	private static String interfaceInfoQuery = prefixes +
			"SELECT ?output ?input ?limit {from}WHERE { " +
			" <{uri}> <" + Ontology.hasOutput + "> ?output . " +
			" OPTIONAL { <{uri}> <" + Ontology.hasDefaultLimit + "> ?limit . }" +
			" OPTIONAL { <{uri}> <" + Ontology.forInput + "> ?input . } }";
	
	private static String widgetsFromInputs = prefixes +
			"SELECT DISTINCT ?widget {from}WHERE { " +
			"?widget a {class} . " +
			"{paradigm}" +
			"OPTIONAL { ?widget <" + Ontology.hasOutput + "> ?output . } " +
			"OPTIONAL { ?widget <" + Ontology.forInput + "> ?input . } " +
			"FILTER (!bound(?output){output}) " +
			"FILTER (!bound(?input){input}) }";
	
	/**
	 * FacetOntology queries
	 */
	
	public static String connectedFacetTestQuery(String uri, QueryableSource source) {
		return buildQuery(String.format("ASK { %s }", connectedFacetTestBGP), uri, source);
	}

	public static String connectedFacetLabelQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(connectedFacetLabelBGP, uri, source);
	}
	
	public static String connectedFacetClassQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(connectedFacetClassBGP, uri, source);
	}
	
	public static String nextPredicateQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(nextPredicateBGP, uri, source);
	}
	
	public static String connectedFacetFiltersQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(connectedFacetFiltersBGP, uri, source);
	}
	
	public static String facetItemLabelQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(facetItemLabelBGP, uri, source);
	}
	
	public static String facetCollectionTestQuery(String uri, QueryableSource source) {
		return buildQuery(String.format("ASK { %s }", facetCollectionTestBGP), uri, source);
	}
	
	public static String facetSparqlAggregateVarsQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(facetSparqlAggregateVarslBGP, uri, source);
	}
	
	public static String predicateSparqlGraphQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(predicateSparqlGraphBGP, uri, source);
	}
	
	public static String firstOrderFacetQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(firstOrderFacetBGP, uri, source);
	}
	
	public static String connectedFacetsQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(connectedFacetsBGP, uri, source);
	}
	
	public static String facetContextQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(connectedFacetContextBGP, uri, source);
	}
	
	public static String facetSparqlSelectVarsQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(connectedSparqlSelectVarsBGP, uri, source);
	}
	
	public static String filterTestQuery(String uri, QueryableSource source) {
		return buildQuery(String.format("ASK { %s }", filterTestBGP), uri, source);
	}
	
	public static String filterValueQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(filterValueBGP, uri, source);
	}
	
	public static String firstOrderFacetTestQuery(String uri, QueryableSource source) {
		return buildQuery(String.format("ASK { %s }", firstOrderFacetTestBGP), uri, source);
	}

	public static String predicateTestQuery(String uri, QueryableSource source) {
		return buildQuery(String.format("ASK { %s }", predicateTestBGP), uri, source);
	}
	
	public static String predicateValueQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(predicateValueBGP, uri, source);
	}
	
	public static String predicateReverseQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(predicateReverseBGP, uri, source);
	}
	
	public static String predicateTransitiveQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(predicateTransitiveBGP, uri, source);
	}
	
	public static String predicateOptionalQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(predicateOptionalBGP, uri, source);
	}

	public static String facetSparqlBindingQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(facetSparqlBindingBGP, uri, source);
	}
	
	public static String facetTypeQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(facetTypeBGP, uri, source);
	}
	
	public static String connectedFacetCodependentQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(codependentFacetBGP, uri, source);
	}
	
	/**
	 * Generic queries
	 */
	
	public static String labelQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(labelBGP, uri, source);
	}

	public static String commentQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(commentBGP, uri, source);
	}
	
	public static String typeQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(typeBGP, uri, source);
	}
	
	public static String seeAlsoQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(seeAlsoBGP, uri, source);
	}
	
	/**
	 * Widget queries
	 */
	
	public static String widgetScriptsQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(widgetScriptsBGP, uri, source);
	}

	public static String widgetStylesheetsQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(widgetStylesheetsBGP, uri, source);
	}

	public static String widgetOutputSupportQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(widgetSupportBGP, uri, source);
	}
	
	public static String widgetPrototypeQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(widgetPrototypeBGP, uri, source);
	}
	
	public static String widgetInputSupportQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(widgetInputSupportBGP, uri, source);
	}
	
	public static String widgetParadigmSupportQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(widgetParadigmSupportBGP, uri, source);
	}
	
	/**
	 * Widget selection queries
	 */
	
	public static String widgetsFromInputs(String klass, String input, String output, String paradigm, QueryableSource source) {
		String inf = (SparqlSource.class.isAssignableFrom(source.getClass())) ? ((SparqlSource)source).getInference() : null;
		String query = widgetsFromInputs;
		
		if (klass != null) {
			query = query.replaceAll("\\{class\\}", "<" + klass + ">");
		} else {
			query = query.replaceAll("\\{class\\}", "s2s:Widget");
		}
		
		if (output != null) {
			query = query.replaceAll("\\{output\\}", " || ?output = <" + output + ">");
		} else {
			query = query.replaceAll("\\{output\\}", "");
		}
		
		if (input != null) {
			query = query.replaceAll("\\{input\\}", " || ?input = <" + input + ">");
		} else {
			query = query.replaceAll("\\{input\\}", "");
		}
		
		if (paradigm != null) {
			query = query.replaceAll("\\{paradigm\\}", "?widget <" + Ontology.supportsParadigm + "> <" + paradigm + "> . ");
		} else {
			query = query.replaceAll("\\{paradigm\\}", "");
		}
		
		String fromClause = "";
		String infClause = "";
		if (source.getGraph() != null)
			fromClause = from.replaceAll("\\{graph\\}", source.getGraph());
		if (inf != null)
			infClause = inference.replaceAll("\\{inference\\}", inf);
		return infClause.concat(query.replaceAll("\\{from\\}", fromClause));
	}

	/**
	 * Search service queries
	 */
	
	public static String searchServiceConfigQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(searchServiceConfigBGP, uri, source);
	}
	
	public static String searchServiceParadigmSupportQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(searchServiceParadigmSupportBGP, uri, source);
	}
	
	/**
	 * Input queries
	 */
	
	public static String inputInfoQuery(String uri, QueryableSource source) {
		return buildSimpleQuery(inputDelimiterBGP, uri, source);
	}
	
	/**
	 * Interface queries
	 */
	
	public static String interfaceInfoQuery(String uri, QueryableSource source) {
		return buildQuery(interfaceInfoQuery, uri, source);
	}
	
	/**
	 * Query builders
	 */
	
	public static String buildSimpleQuery(String bgp, String uri, QueryableSource source) {
		return buildQuery(simpleQuery.replaceAll("\\{bgp\\}", bgp), uri, source);
	}
	
	public static String buildQuery(String query, String uri, QueryableSource source) {
		String inf = (SparqlSource.class.isAssignableFrom(source.getClass())) ? ((SparqlSource)source).getInference() : null;
		String fromClause = "";
		String inferenceClause = "";
		if (source.getGraph() != null)
			fromClause = from.replaceAll("\\{graph\\}", source.getGraph());
		if (inf != null)
			inferenceClause = inference.replaceAll("\\{inference\\}", inf);
		return inferenceClause.concat((query.replaceAll("\\{uri\\}", uri)).replaceAll("\\{from\\}", fromClause));
	}
	
	/**
	 * Query execution
	 */
	
	public static boolean sparqlAsk(String q, Model m) {
		Query query = QueryFactory.create(q) ;
		m.enterCriticalSection(Lock.READ);
		QueryExecution qExec = QueryExecutionFactory.create(query, m);
		boolean rs =  qExec.execAsk();
		m.leaveCriticalSection();
		return rs;	
	}
	
	public static boolean sparqlAsk(String q, String ep) {
		try {
			URL url = new URL(ep + "?query=" + URLEncoder.encode(q,"UTF-8") + "&format=xml");
			DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = df.newDocumentBuilder();
			Document doc = db.parse(url.openConnection().getInputStream());
			NodeList nodes = doc.getElementsByTagName("boolean");
			if (nodes.getLength() > 0) {
				Node n = nodes.item(0);
				return Boolean.parseBoolean(n.getTextContent());
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
        return false;
	}
	
	public static ResultSet sparqlSelect(String q, Model m) {
		Query query = QueryFactory.create(q) ;
		m.enterCriticalSection(Lock.READ);
		QueryExecution qExec = QueryExecutionFactory.create(query, m);
		ResultSet rs =  qExec.execSelect();
		m.leaveCriticalSection();
		return rs;
	}
	
	public static ResultSet sparqlSelect(String q, String ep) {
		try {
			URL url = new URL(ep + "?query=" + URLEncoder.encode(q,"UTF-8") + "&format=xml");
			return ResultSetFactory.fromXML(url.openConnection().getInputStream());
		} catch (IOException e) {
            throw new RuntimeException(e);
		}
	}
}
