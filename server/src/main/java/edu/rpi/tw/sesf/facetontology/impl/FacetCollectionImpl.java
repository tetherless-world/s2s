package edu.rpi.tw.sesf.facetontology.impl;

import java.util.Collection;
import java.util.Vector;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.facetontology.ConnectedFacet;
import edu.rpi.tw.sesf.facetontology.FacetCollection;
import edu.rpi.tw.sesf.facetontology.FirstOrderFacet;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;

public class FacetCollectionImpl extends FacetCollection {

	private static final long serialVersionUID = 7010105524547863017L;
	
	private String _uri;
	private DataSource _source;
	private boolean _fromCache;
	private FirstOrderFacet _goal;
	private Vector<ConnectedFacet> _connectedFacets;
	
	public FacetCollectionImpl(String uri, DataSource source) throws UnregisteredInstanceException, RippleException {
		_uri = uri;
		_source = source;
		_fromCache = false;
		_connectedFacets = new Vector<>();
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			query();
		} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
			crawl();
		}
	}
	
	public FirstOrderFacet getFirstOrderFacet() {
		return _goal;
	}

	public Collection<ConnectedFacet> getConnectedFacets() {
		return _connectedFacets;
	}

	public DataSource getDataSource() {
		return _source;
	}

	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException,
			UnregisteredInstanceException, RippleException {
		if (!QueryableSource.class.isAssignableFrom(source.getClass())) {
			throw new IncompatibleDataSourceException("Only queryable sources can be used for this class.");
		}
		_source = source;
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			query();
		} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
			crawl();
		}
	}

	public boolean isFromCache() {
		return _fromCache;
	}

	public void setFromCache() {
		_fromCache = true;
	}

	private void query() throws UnregisteredInstanceException, RippleException {
		QueryableSource source = (QueryableSource)_source;
		//Test if instance belongs to correct class
		if (!source.sparqlAsk(Queries.facetCollectionTestQuery(_uri, source))) {
			throw new UnregisteredInstanceException("FacetCollection data for " + _uri + " does not exist at source: " + _source.toString() );
		}
		
		ResultSet rs = source.sparqlSelect(Queries.firstOrderFacetQuery(_uri, source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_goal = new FirstOrderFacetImpl(qs.get("o").toString(), source);
		}
		
		rs = source.sparqlSelect(Queries.connectedFacetsQuery(_uri, source));
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_connectedFacets.add(new ConnectedFacetImpl(qs.get("o").toString(), source));
		}
		
		//TODO: check if instance is well-formed
	}
	
	private void crawl() throws RippleException, UnregisteredInstanceException {
		RippleSource source = (RippleSource)_source;
		
		Collector<RippleList> c = new Collector<>();
	    QueryPipe p = new QueryPipe(source.getQueryEngine(), c);
	    
	    String uriRef = "<" + _uri + ">";
		
	    p.put(uriRef + " <" + Ontology.facetFacetUri + ">. dup. <" + Ontology.type + ">.");
	    for (RippleList l : c) {
	    	String type = l.getFirst().toString();
	    	String curr = l.getRest().getFirst().toString();
	    	if (type.equals(Ontology.facetFirstOrderFacet)) {
	    		_goal = new FirstOrderFacetImpl(curr,source);
	    	} else if (type.equals(Ontology.facetConnectedFacet)) {
	    		_connectedFacets.add(new ConnectedFacetImpl(curr, source));
	    	}
	    }
	    c.clear();
	}
}
