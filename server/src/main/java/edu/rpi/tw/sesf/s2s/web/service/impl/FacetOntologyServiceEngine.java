package edu.rpi.tw.sesf.s2s.web.service.impl;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.facetontology.ConnectedFacet;
import edu.rpi.tw.sesf.facetontology.FacetCollection;
import edu.rpi.tw.sesf.facetontology.impl.FacetCollectionImpl;
import edu.rpi.tw.sesf.s2s.SearchService;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.DataSourceFactory;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Configuration;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;
import edu.rpi.tw.sesf.s2s.web.service.WebServiceEngine;

public class FacetOntologyServiceEngine implements WebServiceEngine {

	private final static String _fcBGP = "<{uri}> <" + Ontology.hasFacetCollection + "> ?o . ";
	private final static String _dsBGP = "<{uri}> <" + Ontology.hasDataSource + "> ?o . ";
	private SearchService _service;
	private FacetCollection _fc;
	private DataSource _ds;
	private String _fcuri;
	private String _dsuri;
	
	public FacetOntologyServiceEngine(SearchService service) throws IncompatibleDataSourceException, UnregisteredInstanceException, RippleException {
		_service = service;
		if (QueryableSource.class.isAssignableFrom(service.getDataSource().getClass())) {
			_query();
			_fc = new FacetCollectionImpl(_fcuri, service.getDataSource());
			DataSourceFactory dsf = new DataSourceFactory(Arrays.asList(service.getDataSource()), true);
			_ds = dsf.createDataSource(_dsuri);
			if (!QueryableSource.class.isAssignableFrom(_ds.getClass())) {
				throw new IncompatibleDataSourceException("Only SPARQL sources supported for now for facet queries.");
			}
		} else if (RippleSource.class.isAssignableFrom(service.getDataSource().getClass())) {
			_crawl();
			_fc = new FacetCollectionImpl(_fcuri, service.getDataSource());
			DataSourceFactory dsf = new DataSourceFactory(Arrays.asList(service.getDataSource()), true);
			_ds = dsf.createDataSource(_dsuri);
			if (!QueryableSource.class.isAssignableFrom(_ds.getClass())) {
				throw new IncompatibleDataSourceException("Only SPARQL sources supported for now for facet queries.");
			}
		} else {
			throw new IncompatibleDataSourceException("Data source not currently supported");
		}
	}
	
	public FacetCollection getFacetCollection() {
		return _fc;
	}
	
	public Collection<String> getInputs() {
		Vector<String> ret = new Vector<>();
		for (ConnectedFacet f : _fc.getConnectedFacets()) {
			ret.add(f.getURI());
		}
		return ret;
	}

	public Collection<String> getInterfaces() {
		Collection<String> ret = getInputs();
		ret.add(_fc.getFirstOrderFacet().getURI());
		return ret;
	}

	public void runQuery(String query, Map<String, String> parameters,
			HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		Map<String,Collection<String>> fcInputs = new HashMap<>();
		if (parameters != null) {
			for (String key : parameters.keySet()) {
				Vector<String> vals = new Vector<>();
				if (!parameters.get(key).equals("")) {
					String[] valArr = parameters.get(key).split(Configuration.facetInputDelimiter);
                    Collections.addAll(vals, valArr);
					fcInputs.put(key, vals);
				}
			}
		}
		response.addHeader("Access-Control-Allow-Origin", "*");
		if (_fc.getFirstOrderFacet().getURI().equals(query)) {
			String q = _fc.buildFirstOrderFacetQuery(fcInputs, ((QueryableSource)_ds).getSparqlType());
			ResultSet rs = ((QueryableSource)_ds).sparqlSelect(q);
			q = _fc.buildFirstOrderFacetCountQuery(fcInputs, ((QueryableSource)_ds).getSparqlType());
			ResultSet rs2 = ((QueryableSource)_ds).sparqlSelect(q);
			if (fcInputs.containsKey(Ontology.opensearchCount)) {
				response.getWriter().write(Utils.getPagedFirstOrderResultsJson(rs,rs2,fcInputs));
			} else {
				response.getWriter().write(Utils.getFirstOrderResultsJson(rs));
			}
		} else if (_fc.getConnectedFacet(query) != null) {
			String q = _fc.buildConnectedFacetQuery(query, fcInputs, ((QueryableSource)_ds).getSparqlType());
			ResultSet rs = ((QueryableSource)_ds).sparqlSelect(q);
			response.getWriter().write(Utils.getSimpleSparqlJson(rs));
		} else {
			response.sendError(400, "Query URI does not exist for this service.");
		}
	}

	private void _crawl() throws RippleException {
		RippleSource source = (RippleSource)_service.getDataSource();
				
		Collector<RippleList> c = new Collector<>();
	    QueryPipe p = new QueryPipe(source.getQueryEngine(), c);
	    
	    String uriRef = "<" + _service.getURI() + ">";
	    
	    p.put(uriRef + " <" + Ontology.hasFacetCollection + ">.");
	    for (RippleList l : c) {
	    	_fcuri = l.getFirst().toString();
	    }
	    c.clear();
	    
	    p.put(uriRef + " <" + Ontology.hasDataSource + ">.");
	    for (RippleList l : c) {
	    	_dsuri = l.getFirst().toString();
	    }
	    c.clear();
	}
	
	private void _query() throws UnregisteredInstanceException {
		String q = Queries.buildSimpleQuery(_fcBGP, _service.getURI(), ((QueryableSource)_service.getDataSource()));
		ResultSet rs = ((QueryableSource)_service.getDataSource()).sparqlSelect(q);
		
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o")) {
				_fcuri = Utils.parseLiteral(qs.get("o").toString());
			} else {
				throw new UnregisteredInstanceException("FacetOntologyService at URI (" + _service.getURI() + ") is missing a pointer to its facet collection.");
			}
		} else {
			throw new UnregisteredInstanceException("FacetOntologyService at URI (" + _service.getURI() + ") is missing a pointer to its facet collection.");
		}
		
		q = Queries.buildSimpleQuery(_dsBGP, _service.getURI(), ((QueryableSource)_service.getDataSource()));
		rs = ((QueryableSource)_service.getDataSource()).sparqlSelect(q);
		
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o")) {
				_dsuri = Utils.parseLiteral(qs.get("o").toString());
			} else {
				throw new UnregisteredInstanceException("FacetOntologyService at URI (" + _service.getURI() + ") is missing a pointer to its data source.");
			}
		} else {
			throw new UnregisteredInstanceException("FacetOntologyService at URI (" + _service.getURI() + ") is missing a pointer to its data source.");
		}
	}
}
