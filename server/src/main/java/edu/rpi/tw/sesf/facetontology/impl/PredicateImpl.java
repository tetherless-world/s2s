package edu.rpi.tw.sesf.facetontology.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.facetontology.Predicate;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class PredicateImpl extends Predicate {

	private static final long serialVersionUID = -4848862344011464292L;

	private DataSource _source;
	private String _uri;
	private String _val;
	private String _var;
	private boolean _rev;
	private boolean _transitive;
	private boolean _optional;
	private Predicate _next;
	private boolean _fromCache;
	private String _graph;
	
	public PredicateImpl(String uri, DataSource source) throws UnregisteredInstanceException, RippleException {
		_uri = uri;
		_source = source;
		_fromCache = false;
		_rev = false;
		_transitive = false;
		_optional = false;
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			query();
		} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
			crawl();
		}
	}
	
	public Predicate getNextPredicate() {
		return _next;
	}

	public String getPredicateValue() {
		return _val;
	}

	public boolean isReverse() {
		return _rev;
	}
	
	public boolean isTransitive() {
		return _transitive;
	}

	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException,
			UnregisteredInstanceException, RippleException {
		if (!QueryableSource.class.isAssignableFrom(source.getClass())) {
			throw new IncompatibleDataSourceException("Only queryable sources are compatible.");
		}
		_source = source;
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			query();
		} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
			crawl();
		}
	}

	@Override
	public boolean isFromCache() {
		return _fromCache;
	}

	@Override
	public void setFromCache() {
		_fromCache = true;
	}
	
	private void query() throws UnregisteredInstanceException, RippleException {
		QueryableSource source = (QueryableSource)_source;
		
		//Test if instance belongs to correct class
		if (!source.sparqlAsk(Queries.predicateTestQuery(_uri, source))) {
			throw new UnregisteredInstanceException("Predicate data for " + _uri + " does not exist at source: " + _source.toString() );
		}
		
		ResultSet rs = source.sparqlSelect(Queries.predicateValueQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_val = Utils.parseLiteral(qs.get("o").toString());
		}
		
		rs = source.sparqlSelect(Queries.nextPredicateQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_next = new PredicateImpl(qs.get("o").toString(), source);
		}
		
		rs = source.sparqlSelect(Queries.predicateReverseQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_rev = Boolean.parseBoolean(Utils.parseLiteral(qs.get("o").toString())) || Utils.parseLiteral(qs.get("o").toString()).equals("1");
		}
		
		rs = source.sparqlSelect(Queries.predicateTransitiveQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_transitive = Boolean.parseBoolean(Utils.parseLiteral(qs.get("o").toString())) || Utils.parseLiteral(qs.get("o").toString()).equals("1");
		}
		
		rs = source.sparqlSelect(Queries.predicateOptionalQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_optional = Boolean.parseBoolean(Utils.parseLiteral(qs.get("o").toString())) || Utils.parseLiteral(qs.get("o").toString()).equals("1");
		}
		
		rs = source.sparqlSelect(Queries.facetSparqlBindingQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_var = Utils.parseLiteral(qs.get("o").toString());
		}
		
		rs = source.sparqlSelect(Queries.predicateSparqlGraphQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_graph = Utils.parseLiteral(qs.get("o").toString());
		}
		
		//TODO: check if instance is well-formed
	}
	
	private void crawl() throws RippleException, UnregisteredInstanceException {
		RippleSource source = (RippleSource)_source;
		
	    Pattern literalPattern = Pattern.compile("\"(.*?)\"(\\^\\^<(.*?)>)?");
		
		Collector<RippleList,RippleException> c = new Collector<RippleList,RippleException>();
	    QueryPipe p = new QueryPipe(source.getQueryEngine(), c);
	    
	    String uriRef = "<" + _uri + ">";
	    
	    p.put(uriRef + " <" + Ontology.facetPredicate + ">.");
	    for (RippleList l : c) {
    		_val = l.getFirst().toString();
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetSparqlBinding + ">.");
	    for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _var = m.group(1);
	    }
	    c.clear();

	    p.put(uriRef + " <" + Ontology.facetSparqlGraph + ">.");
	    for (RippleList l : c) {
    		_graph = l.getFirst().toString();
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetNextPredicate + ">.");
	    for (RippleList l : c) {
	    	_next = new PredicateImpl(l.getFirst().toString(), source);
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetReverse + ">.");
	    for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _rev = Boolean.parseBoolean(m.group(1)) || m.group(1).equals("1");
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetTransitive + ">.");
	    for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _transitive = Boolean.parseBoolean(m.group(1)) || m.group(1).equals("1");
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetOptional + ">.");
	    for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _optional = Boolean.parseBoolean(m.group(1)) || m.group(1).equals("1");
	    }
	    c.clear();
	}

	@Override
	public String getSparqlBinding() {
		return _var;
	}

	@Override
	public String getSparqlGraph() {
		return _graph;
	}

	@Override
	public boolean isOptional() {
		return _optional;
	}
	

}
