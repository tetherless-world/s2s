package edu.rpi.tw.sesf.facetontology.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.facetontology.Filter;
import edu.rpi.tw.sesf.facetontology.Predicate;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class FilterImpl implements Filter {

	private static final long serialVersionUID = 4106123577536426285L;

	private String _uri;
	private DataSource _source;
	private boolean _fromCache;
	private Predicate _pred;
	private String _val;
	private String _graph;
	private boolean _optional;
	
	public FilterImpl(String uri, DataSource source) throws UnregisteredInstanceException, RippleException {
		_uri = uri;
		_source = source;
		_fromCache = false;
		_optional = false;
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			query();
		} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
			crawl();
		}
	}
	
	public Predicate getFirstPredicate() {
		return _pred;
	}

	public String getFilterValue() {
		return _val;
	}

	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException,
			UnregisteredInstanceException, RippleException {
		if (!QueryableSource.class.isAssignableFrom(source.getClass()))
			throw new IncompatibleDataSourceException("This class only supports queryable sources.");
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

	private void crawl() throws RippleException, UnregisteredInstanceException {
		RippleSource source = (RippleSource)_source;
		
	    Pattern literalPattern = Pattern.compile("\"(.*?)\"(\\^\\^<(.*?)>)?");
		
		Collector<RippleList,RippleException> c = new Collector<RippleList,RippleException>();
	    QueryPipe p = new QueryPipe(source.getQueryEngine(), c);
	    
	    String uriRef = "<" + _uri + ">";
	    
	    p.put(uriRef + " <" + Ontology.facetFilterValue + ">.");
	    for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _val = m.group(1);
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetNextPredicate + ">.");
	    for (RippleList l : c) {
	    	_pred = new PredicateImpl(l.getFirst().toString(), source);
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetSparqlGraph + ">.");
	    for (RippleList l : c) {
    		_graph = l.getFirst().toString();
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetOptional + ">.");
	    for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _optional = Boolean.parseBoolean(m.group(1)) || m.group(1).equals("1");
	    }
	    c.clear();
	    
		//TODO: check if instance is well-formed
	}
	
	private void query() throws UnregisteredInstanceException, RippleException {
		QueryableSource source = (QueryableSource)_source;
		//Test if instance belongs to correct class
		if (!source.sparqlAsk(Queries.filterTestQuery(_uri, source))) {
			throw new UnregisteredInstanceException("Filter data for " + _uri + " does not exist at source: " + _source.toString() );
		}
		
		ResultSet rs = source.sparqlSelect(Queries.filterValueQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_val = Utils.parseLiteral(qs.get("o").toString());
		}
		
		rs = source.sparqlSelect(Queries.nextPredicateQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_pred = new PredicateImpl(qs.get("o").toString(), source);
		}
		
		rs = source.sparqlSelect(Queries.predicateSparqlGraphQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_graph = Utils.parseLiteral(qs.get("o").toString());
		}
		
		rs = source.sparqlSelect(Queries.predicateOptionalQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_optional = Boolean.parseBoolean(Utils.parseLiteral(qs.get("o").toString())) || Utils.parseLiteral(qs.get("o").toString()).equals("1");
		}
		
		
		//TODO: check if instance is well-formed
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
