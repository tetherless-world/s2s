package edu.rpi.tw.sesf.facetontology.impl;

import java.util.Collection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.facetontology.FacetType;
import edu.rpi.tw.sesf.facetontology.Filter;
import edu.rpi.tw.sesf.facetontology.FirstOrderFacet;
import edu.rpi.tw.sesf.facetontology.Predicate;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Configuration;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class FirstOrderFacetImpl implements FirstOrderFacet {

	private static final long serialVersionUID = -6713589878054312958L;

	private String _uri;
	private String _lang;
	private DataSource _source;
	private boolean _fromCache;
	private String _label;
	private String _comment;
	private Predicate _itemLabel;
	private String _class;
	private String _var;
	private Vector<Filter> _filters;
	private Vector<String> _selectVars;
	private Vector<String> _aggregateVars;
	private Vector<Predicate> _context;
	private FacetType _type;
	private int _limit;
	
	public FirstOrderFacetImpl(String uri, DataSource source) throws UnregisteredInstanceException, RippleException {
		_source = source;
		_uri = uri;
		_fromCache = false;
		_filters = new Vector<Filter>();
		_limit = 10;
		_selectVars = new Vector<String>();
		_context = new Vector<Predicate>();
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			query();
		} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
			crawl();
		}
	}
	
	public String getLabel() {
		return _label;
	}

	public String getFacetClass() {
		return _class;
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
			throw new IncompatibleDataSourceException("Only QueryableSource useful for this class.");
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

	@Override
	public Predicate getItemLabelPredicate() {
		return _itemLabel;
	}
	
	private void query() throws UnregisteredInstanceException, RippleException {
		QueryableSource source = (QueryableSource)_source;
		
		//Test if instance belongs to correct class
		if (!source.sparqlAsk(Queries.firstOrderFacetTestQuery(_uri, source))) {
			throw new UnregisteredInstanceException("FirstOrderFacet data for " + _uri + " does not exist at source: " + _source.toString() );
		}
		
		ResultSet rs = source.sparqlSelect(Queries.connectedFacetLabelQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_label = Utils.parseLiteral(qs.get("o").toString());
		}
		
		rs = source.sparqlSelect(Queries.connectedFacetClassQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_class = qs.get("o").toString();
		}
		
		rs = source.sparqlSelect(Queries.connectedFacetFiltersQuery(_uri, source));
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_filters.add(new FilterImpl(qs.get("o").toString(), _source));
		}
		
		rs = source.sparqlSelect(Queries.facetItemLabelQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_itemLabel = new PredicateImpl(qs.get("o").toString(), source);
		}
		
		rs = source.sparqlSelect(Queries.facetContextQuery(_uri, source));
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_context.add(new PredicateImpl(qs.get("o").toString(), source));
		}
		
		rs = source.sparqlSelect(Queries.facetSparqlSelectVarsQuery(_uri, source));
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_selectVars.add(Utils.parseLiteral(qs.get("o").toString()));
		}
		
		rs = source.sparqlSelect(Queries.facetTypeQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_type = Configuration.facetMap.get(qs.get("o").toString());
		}
		
		rs = source.sparqlSelect(Queries.facetSparqlBindingQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_var = Utils.parseLiteral(qs.get("o").toString());
		}
		
		//TODO: check if instance is well-formed
		//TODO: get lang property
		//TODO: get comment property
		//TODO: get default limit property
	}

	private void crawl() throws RippleException, UnregisteredInstanceException {
		RippleSource source = (RippleSource)_source;
		
	    Pattern literalPattern = Pattern.compile("\"(.*?)\"(\\^\\^<(.*?)>)?");
		
		Collector<RippleList,RippleException> c = new Collector<RippleList,RippleException>();
	    QueryPipe p = new QueryPipe(source.getQueryEngine(), c);
	    
	    String uriRef = "<" + _uri + ">";
		
	    p.put(uriRef + " <" + Ontology.facetLabel + ">.");
	    for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _label = m.group(1);
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetClass + ">.");
	    for (RippleList l : c) {
	    	_class = l.getFirst().toString();
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.facetFilter + ">.");
	    for (RippleList l : c) {
	    	_filters.add(new FilterImpl(l.getFirst().toString(), source));
	    }
		c.clear();
		
    	p.put(uriRef + " <" + Ontology.facetSparqlSelVar + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _selectVars.add(m.group(1));
    	}
		c.clear();
		
    	p.put(uriRef + " <" + Ontology.facetSparqlAggVar + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _aggregateVars.add(m.group(1));
    	}
    	c.clear();
		
    	p.put(uriRef + " <" + Ontology.facetContextPredicate + ">.");
    	for (RippleList l : c) {
    		_context.add(new PredicateImpl(l.getFirst().toString(), source));
    	}
		c.clear();
		
		p.put(uriRef + " <" + Ontology.facetItemLabel + ">.");
		for (RippleList l : c) {
			_itemLabel = new PredicateImpl(l.getFirst().toString(), source);
		}
		c.clear();
		
    	p.put(uriRef + " <" + Ontology.facetSparqlBinding + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _var = m.group(1);
    	}
    	c.clear();
		
		p.put(uriRef + " <" + Ontology.facetType + ">.");
		for (RippleList l : c) {
			_type = Configuration.facetMap.get(l.getFirst().toString());
		}
		c.clear();
		
		//TODO: check if instance is well-formed
		//TODO: get lang property
		//TODO: get comment property
		//TODO: get default limit property
	}
	
	@Override
	public Collection<Filter> getFilters() {
		return _filters;
	}

	@Override
	public FacetType getFacetType() {
		return _type;
	}

	@Override
	public String getURI() {
		return _uri;
	}

	@Override
	public String getFacetLanguage() {
		return _lang;
	}

	@Override
	public String getComment() {
		return _comment;
	}

	@Override
	public int getDefaultLimit() {
		return _limit;
	}

	@Override
	public Collection<Predicate> getContextPredicates() {
		return _context;
	}

	@Override
	public Collection<String> getSparqlSelectVariables() {
		return _selectVars;
	}

	@Override
	public Collection<String> getSparqlAggregateVariables() {
		return _aggregateVars;
	}

	@Override
	public String getSparqlBinding() {
		return _var;
	}

}
