package edu.rpi.tw.sesf.facetontology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.rpi.tw.sesf.s2s.InstanceData;
import edu.rpi.tw.sesf.s2s.data.SparqlType;
import edu.rpi.tw.sesf.s2s.utils.Ontology;

public abstract class FacetCollection implements Serializable, InstanceData {
	
	private Log log = LogFactory.getLog(FacetCollection.class);

	private static String goalVar = "goal";
	private static String selectVar = "id";
	private static String labelVar = "label";
	
	private static final long serialVersionUID = 7976977063673106165L;

	public abstract FirstOrderFacet getFirstOrderFacet();
	public abstract Collection<ConnectedFacet> getConnectedFacets();
	
	public ConnectedFacet getConnectedFacet(String uri) {
		for (ConnectedFacet c : getConnectedFacets()) {
			if (c.getURI().equals(uri)) return c;
		}
		return null;
	}
	
	public String buildConnectedFacetQuery(String facet, Map<String, Collection<String>> inputs) {
		return buildConnectedFacetQuery(facet, inputs, SparqlType.Default);
	}
	
	public String buildConnectedFacetQuery(String facet, Map<String, Collection<String>> inputs, SparqlType type) {
		ConnectedFacet f = getConnectedFacet(facet);
		StringBuilder sb = new StringBuilder();
		String query = null;
		if (f != null) {
			sb.append(buildQueryHeader(f));
			int vcount = 1;
			String vbase = "v";
			//write constraints
			for (String key : inputs.keySet()) {
				if (getConnectedFacet(key) != null) {
					Collection<String> vals = inputs.get(key);
					Collection<String> constraints = buildQueryConstraint(getConnectedFacet(key), vals, String.format("%s%d", vbase, vcount++), goalVar, type);
					sb.append(StringUtils.join(constraints, " UNION "));
				}
			}
			sb.append(' ');
			//write type constraint
			if (f.getFacetClass() != null) sb.append(String.format("?id <%s> <%s> . ", Ontology.type, f.getFacetClass()));
			//write selection paths
			sb.append(writePredicatePathsUnion(f.getPredicatePaths(), String.format("%s%d", vbase, vcount++), "?" + selectVar, goalVar, type));
			//boolean writeSameTermFilter = f.getSparqlBinding() != null && sb.toString().split(String.format("[{. ]\\\\?%s[}. ]",f.getSparqlBinding())).length > 1;
			//TODO: use the more flexible boolean above (has weird behavior where even though the expression evaluates to true, it doesn't work)
			boolean writeSameTermFilter = f.getSparqlBinding() != null && sb.toString().contains(" ?" + f.getSparqlBinding() + " ");
			if (writeSameTermFilter) sb.append(String.format(" FILTER sameTerm(?id, ?%s)", f.getSparqlBinding()));
			for (Filter filter : f.getFilters()) {
				sb.append(' '); 
				sb.append(writeFilterPath(filter, String.format("%s%d", vbase, vcount++), "?" + selectVar, type));
			}
			for (Predicate p : f.getContextPredicates()) {
				sb.append(' ');
				sb.append(writePredicatePath(p.getList(), String.format("%s%d", vbase, vcount++), "?" + selectVar, "x" /* this is a junk variable... */, type));
			}
			sb.append(' ');
			sb.append(writePredicatePath(f.getItemLabelPredicate().getList(), String.format("%s%d", vbase, vcount++), "?" + labelVar, selectVar, type));
			
			sb.append(buildQueryFooter(f,0,0));
			query = sb.toString();
			log.info("Facet Query for " + f.getLabel());
			log.info("=================================");
			log.info(query);
		}
		return query;
	}
	
	public String buildFirstOrderFacetQuery(Map<String, Collection<String>> inputs) {
		return buildFirstOrderFacetQuery(inputs, SparqlType.Default);
	}
	
	public String buildFirstOrderFacetCountQuery(Map<String, Collection<String>> inputs, SparqlType type) {
		StringBuilder sb = new StringBuilder();
		FirstOrderFacet f = getFirstOrderFacet();
		String query;
		//write header
		sb.append(buildCountQueryHeader());
		int vcount = 1;
		String vbase = "v";
		//write contraint path
		for (String key : inputs.keySet()) {
			if (getConnectedFacet(key) != null) {
				Collection<String> vals = inputs.get(key);
				Collection<String> constraints = buildQueryConstraint(getConnectedFacet(key), vals, String.format("%s%d", vbase, vcount++), goalVar, type);
				sb.append(StringUtils.join(constraints, " UNION "));
			}
		}
		sb.append(' ');
		//write select path
		sb.append(buildQuerySelect(f));
		if (f.getItemLabelPredicate() != null) {
			sb.append(writePredicatePath(getFirstOrderFacet().getItemLabelPredicate().getList(), String.format("%s%d", vbase, vcount++), "?" + labelVar, goalVar, type));
		}
		boolean writeSameTermFilter = f.getSparqlBinding() != null && sb.toString().contains(" ?" + f.getSparqlBinding() + " ");
		if (writeSameTermFilter) sb.append(String.format(" FILTER sameTerm(?id, ?%s)", f.getSparqlBinding()));
		for (Filter filter : f.getFilters()) {
			sb.append(' '); 
			sb.append(writeFilterPath(filter, String.format("%s%d", vbase, vcount++), "?" + goalVar, type));
		}
		for (Predicate p : f.getContextPredicates()) {
			sb.append(' ');
			sb.append(writePredicatePath(p.getList(), String.format("%s%d", vbase, vcount++), "?" + goalVar, "x" /* this is a junk variable... */, type));
		}
		sb.append(' ');
		sb.append(buildCountQueryFooter());
		query = sb.toString();
		log.info("Results Count Query for " + f.getLabel());
		log.info("=================================");
		log.info(query);
		return query;
	}
	
	public String buildFirstOrderFacetQuery(Map<String, Collection<String>> inputs, SparqlType type) {
		StringBuilder sb = new StringBuilder();
		FirstOrderFacet f = getFirstOrderFacet();
		String query;
		//write header
		sb.append(buildQueryHeader(f));
		int vcount = 1;
		String vbase = "v";
		//write contraint path
		for (String key : inputs.keySet()) {
			if (getConnectedFacet(key) != null) {
				Collection<String> vals = inputs.get(key);
				Collection<String> constraints = buildQueryConstraint(getConnectedFacet(key), vals, String.format("%s%d", vbase, vcount++), goalVar, type);
				sb.append(StringUtils.join(constraints, " UNION "));
			}
		}
		sb.append(' ');
		//write select path
		sb.append(buildQuerySelect(f));
		if (f.getItemLabelPredicate() != null) {
			sb.append(writePredicatePath(getFirstOrderFacet().getItemLabelPredicate().getList(), String.format("%s%d", vbase, vcount++), "?" + labelVar, goalVar, type));
		}
		boolean writeSameTermFilter = f.getSparqlBinding() != null && sb.toString().contains(" ?" + f.getSparqlBinding() + " ");
		if (writeSameTermFilter) sb.append(String.format(" FILTER sameTerm(?id, ?%s)", f.getSparqlBinding()));
		for (Filter filter : f.getFilters()) {
			sb.append(' '); 
			sb.append(writeFilterPath(filter, String.format("%s%d", vbase, vcount++), "?" + goalVar, type));
		}
		for (Predicate p : f.getContextPredicates()) {
			sb.append(' ');
			sb.append(writePredicatePath(p.getList(), String.format("%s%d", vbase, vcount++), "?" + goalVar, "x" /* this is a junk variable... */, type));
		}
		sb.append(' ');
		int limit = inputs.containsKey(Ontology.opensearchCount) ?
				Integer.parseInt(inputs.get(Ontology.opensearchCount).toArray()[0].toString()) :
				getFirstOrderFacet().getDefaultLimit();
		int offset = inputs.containsKey(Ontology.opensearchStartIndex) ?
				Integer.parseInt(inputs.get(Ontology.opensearchStartIndex).toArray()[0].toString()) : 0;
		sb.append(buildQueryFooter(getFirstOrderFacet(),limit,offset));
		query = sb.toString();
		log.info("Results Query for " + f.getLabel());
		log.info("=================================");
		log.info(query);
		return query;
	}
	
	private Object buildQuerySelect(FirstOrderFacet f) {
		return (f.getFacetClass() != null) ? String.format("?%s <%s> <%s> . ", goalVar, Ontology.type, f.getFacetClass()) : "";
	}
	
	private Collection<String> buildQueryConstraint(ConnectedFacet facet, Collection<String> values, String vbase, String vgoal, SparqlType type) {
		Vector<String> ret = new Vector<>();
		for (String value : values) {
			if (facet.getFacetType().equals(FacetType.Object) && facet.getSparqlBinding() != null) {
				ret.add(String.format("{ %s FILTER (?%s = <%s>) } ", writePredicatePathsUnion(facet.getPredicatePaths(), vbase, "?" + facet.getSparqlBinding(), vgoal, type), facet.getSparqlBinding(), value));  
			} else {
				ret.add("{ " + writePredicatePathsUnion(facet.getPredicatePaths(), vbase, writeSparqlFacetValue(facet, value), vgoal, type)+ " }");
			} 
		}
		return ret;
	}
	
	private String buildQueryHeader(Facet facet) {
		String header = "SELECT DISTINCT ";
		String label = (facet.getItemLabelPredicate() != null) ? "?" + labelVar + " " : ""; 
		if (ConnectedFacet.class.isAssignableFrom(facet.getClass())) {
			header += "?id " + label + "(count(DISTINCT ?goal) as ?count) ";
		} else {
			header += "?goal " + label;
		}
		return header + StringUtils.join(facet.getSparqlSelectVariables(), " ") + " WHERE { ";
	}
	
	private String buildCountQueryHeader() {
		return "SELECT (count(DISTINCT ?goal) as ?count) WHERE {";
	}
	
	private String buildQueryFooter(Facet facet, int limit, int offset) {
		if (ConnectedFacet.class.isAssignableFrom(facet.getClass())) {
			String agg = (facet.getSparqlAggregateVariables().size() > 0) ?
					" ?" + StringUtils.join(facet.getSparqlAggregateVariables(), " ?") : "";
			return "} GROUP BY ?id ?label" + agg;
		} else {
			return String.format("} LIMIT %d OFFSET %d", limit, offset);
		}
	}
	
	private String buildCountQueryFooter() {
		return "}";
	}
	
	private String writeSparqlFacetValue(ConnectedFacet facet, String val) {
		if (facet.getFacetType().equals(FacetType.Object)) {
			return String.format("<%s>", val);
		} else if (facet.getFacetType().equals(FacetType.Literal)) {
			String result = String.format("\"%s\"", val);
			if (facet.getFacetClass() != null) result = String.format("%s^^<%s>", result, facet.getFacetClass());
			if (facet.getFacetLanguage() != null) result = String.format("%s@%s", result, facet.getFacetLanguage());
			return result;
		} else {
			return null;
		}
	}
	
	private String writeFilterPath(Filter f, String vbase, String vstart, SparqlType type) {
		StringBuilder sb = new StringBuilder();
		if (f.getSparqlGraph() != null) sb.append(String.format("GRAPH <%s> { ", f.getSparqlGraph()));
		if (f.isOptional()) sb.append("OPTIONAL { ");
		String vgoal = String.format("%s%s", vbase, "fval");		
		List<Predicate> preds = new ArrayList<>(f.getFirstPredicate().getList());
		sb.append(writePredicatePath(preds, vbase, vstart, vgoal, type));
		sb.append(String.format(" FILTER (%s)", f.getFilterValue().replaceAll("\\{var\\}", "?" + vgoal)));
		if (f.getSparqlGraph() != null) sb.append("} ");
		if (f.isOptional()) sb.append("} ");
		return sb.toString();
	}
	
	private String writePredicatePathsUnion(Collection<Predicate> preds, String vbase, String vstart, String vgoal, SparqlType type) {
		Vector<String> paths = new Vector<>();
		StringBuilder union = new StringBuilder();
		union.append("{ ");
		for (Predicate p : preds) {
			paths.add(writePredicatePath(p.getList(), vbase, vstart, vgoal, type));
		}
		union.append(StringUtils.join(paths, " } UNION { "));
		union.append("} ");
		return union.toString();
	}
		
	private String writePredicatePath(Collection<Predicate> preds, String vbase, String vstart, String vgoal, SparqlType type) {
		int vcount = 1;
		String v, prev = null;
		StringBuilder bgp = new StringBuilder();
		int i = 0;
		for (Predicate p : preds) {
			if (p.getSparqlBinding() != null) {
				v = p.getSparqlBinding();
			} else if (i != preds.size() - 1) {
				v = String.format("%s%d", vbase, vcount++);
			} else {
				v = vgoal;
			}
			if (p.isOptional()) bgp.append("OPTIONAL { ");
			if (p.getSparqlGraph() != null) bgp.append(String.format("GRAPH <%s> { ", p.getSparqlGraph()));
			String modifier = "";
			if (p.isTransitive() && type.equals(SparqlType.VirtuosoSparql)) modifier = "OPTION(transitive) "; 
			if (!p.isReverse() && prev != null) {
				bgp.append(String.format("?%s <%s> ?%s %s. ", v, p.getPredicateValue(), prev, modifier));
			} else if (!p.isReverse() && prev == null) {
				bgp.append(String.format("?%s <%s> %s %s. ", v, p.getPredicateValue(), vstart, modifier));
			} else if (prev != null) {
				bgp.append(String.format("?%s <%s> ?%s %s. ", prev, p.getPredicateValue(), v, modifier));
			} else {
				bgp.append(String.format("%s <%s> ?%s %s. ", vstart, p.getPredicateValue(), v, modifier));
			}
			if (p.getSparqlGraph() != null) bgp.append("} ");
			if (p.isOptional()) bgp.append("} ");
			prev = v; i++;
		}
		return bgp.toString();
	}
}
