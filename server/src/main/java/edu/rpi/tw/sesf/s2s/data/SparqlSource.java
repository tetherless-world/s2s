package edu.rpi.tw.sesf.s2s.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Configuration;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class SparqlSource implements QueryableSource {

	private static final long serialVersionUID = -8920931593989920938L;
	
	private static String _epBGP = "<{uri}> <" + Ontology.location + "> ?o . ";
	private static String _graphBGP = "<{uri}> <" + Ontology.defaultGraph + "> ?o ."; 
	
	private String _id;
	private String _uri;
	private String _endpoint;
	private String _graph;
	private String _inference;
	private DataSource _source;
	private boolean _isFromCache;
	private SparqlType _type;
	
	public SparqlSource(String uri, DataSource source) throws IncompatibleDataSourceException, UnregisteredInstanceException, InvalidLinkedDataIdentifierException {
		_uri = uri;
		_source = source;
		_graph = null;
		_inference = null;
		_type = SparqlType.Default;
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			_query();
		} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
			try {
				_crawl();
			} catch (RippleException e) {
				throw new InvalidLinkedDataIdentifierException("Could not find data for URI (" + uri + ")");
			}
		} else {
			throw new IncompatibleDataSourceException("Only SPARQL sources supported for now");
		}
	}
	
	public SparqlSource(String id, String graph, String endpoint) {
		this(id, graph, endpoint, null);
	}
	
	public SparqlSource(String id, String endpoint, String graph, String inference) {
		_id = id;
		_graph = graph;
		_endpoint = endpoint;
		_inference = inference;
	}

	public String getInference() {
		return _inference;
	}
	
	public void setInference(String inference) {
		_inference = inference;
	}
	
	public void setEndpoint(String endpoint) {
		_endpoint = endpoint;
	}
	
	public String getLocation() {
		return getEndpoint();
	}
	
	public String getEndpoint() {
		return _endpoint;
	}
	
	public void setGraph(String graph) {
		_graph = graph;
	}
	
	public String getGraph() {
		return _graph;
	}

	@Override
	public ResultSet sparqlSelect(String query) {
		return Queries.sparqlSelect(query, _endpoint);
	}
	
	public boolean sparqlAsk(String query) {
		return Queries.sparqlAsk(query, _endpoint);
	}

	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException,
			UnregisteredInstanceException {
		_source = source;
	}

	@Override
	public boolean isFromCache() {
		return _isFromCache;
	}

	@Override
	public void setFromCache() {
		_isFromCache = true;
	}
	
	private void _crawl() throws UnregisteredInstanceException, RippleException {
		RippleSource rsource = (RippleSource)_source;
		
	    Pattern literalPattern = Pattern.compile("\"(.*?)\"(\\^\\^<(.*?)>)?");
		
	    Collector<RippleList,RippleException> c = new Collector<RippleList,RippleException>();
	    QueryPipe p = new QueryPipe(rsource.getQueryEngine(), c);
	    
	    String uriRef = "<" + _uri + ">";
	    
	    p.put(uriRef + " <" + Ontology.location + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _endpoint = m.group(1);
	    }
    	c.clear();
    	
    	p.put(uriRef + " <" + Ontology.defaultGraph + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _graph = m.group(1);
    	}
    	c.clear();
    
    	p.put(uriRef + " <" + Ontology.sparqlType + ">.");
    	for (RippleList l : c) {
    		if (Configuration.sparqlTypes.containsKey(l.getFirst().toString()))
    			_type = Configuration.sparqlTypes.get(l.getFirst().toString());
    	}
    	c.clear();
	}
	
	private void _query() throws UnregisteredInstanceException {
		String q = Queries.buildSimpleQuery(_epBGP, _uri, ((QueryableSource)_source));
		ResultSet rs = ((QueryableSource)_source).sparqlSelect(q);
		
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o")) {
				_endpoint = Utils.parseLiteral(qs.get("o").toString());
			} else {
				throw new UnregisteredInstanceException("JenaPelletSource with URI (" + _uri + ") is missing a pointer to a SPARQL endpoint.");
			}
		} else {
			throw new UnregisteredInstanceException("JenaPelletSource with URI (" + _uri + ") is missing a pointer to a SPARQL endpoint.");
		}
		
		q = Queries.buildSimpleQuery(_graphBGP, _uri, ((QueryableSource)_source));
		rs = ((QueryableSource)_source).sparqlSelect(q);
		
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o")) {
				_graph = Utils.parseLiteral(qs.get("o").toString());
			}
		}
	}

	@Override
	public SparqlType getSparqlType() {
		return _type;
	}

	public String getId() {
		return (_id != null) ? _id : _uri; 
	}
}
