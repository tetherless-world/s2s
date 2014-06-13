package edu.rpi.tw.sesf.s2s.impl.sparql;

import java.util.Vector;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.s2s.SearchService;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class SearchServiceFromSparql extends SearchService {

	private static final long serialVersionUID = -2080301206379057809L;
	
	private String _uri;
	private QueryableSource _source;
	private String _label;
	private String _comment;
	private Vector<String> _links;
	private Vector<String> _types;
	private String _config;
	
	public SearchServiceFromSparql(String uri, QueryableSource source) throws UnregisteredInstanceException {
		_uri = uri;
		_source = source;
		_links = new Vector<>();
		_types = new Vector<>();
		_query();
	}
	
	@Override
	public String getURI() {
		return _uri;
	}

	@Override
	public String getLabel() {
		return _label;
	}

	@Override
	public String getComment() {
		return _comment;
	}

	@Override
	public Vector<String> getRelatedLinks() {
		return _links;
	}

	@Override
	public Vector<String> getTypes() {
		return _types;
	}

	@Override
	public String getDefaultConfigurationURL() {
		return _config;
	}

	private void _query() throws UnregisteredInstanceException {
		ResultSet rs = _source.sparqlSelect(Queries.labelQuery(_uri, _source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_label = Utils.parseLiteral(qs.get("o").toString());
		}
		
		rs = _source.sparqlSelect(Queries.commentQuery(_uri, _source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_comment = Utils.parseLiteral(qs.get("o").toString());
		}
		
		//config query
		rs = _source.sparqlSelect(Queries.searchServiceConfigQuery(_uri, _source));
		if (!rs.hasNext())
			throw new UnregisteredInstanceException("S2S SearchService (" + _uri + ") does not exist at source: " + _source.toString() );
		else {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_config = Utils.parseLiteral(qs.get("o").toString());
		}
		
		//types query
		rs = _source.sparqlSelect(Queries.typeQuery(_uri, _source));
		if (!rs.hasNext())
			throw new UnregisteredInstanceException("S2S SearchService (" + _uri + ") does not exist at source: " + _source.toString() );
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_types.add(qs.get("o").toString());
		}
		
		//links query
		rs = _source.sparqlSelect(Queries.seeAlsoQuery(_uri, _source));		
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_links.add(Utils.parseLiteral(qs.get("o").toString()));
		}
	}
	
	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException, UnregisteredInstanceException {
		if (!QueryableSource.class.isAssignableFrom(source.getClass())) {
			throw new IncompatibleDataSourceException("SparqlSearchService requires QueryableSource.");
		}
		_source = (QueryableSource) source;
		_query();
	}
}
