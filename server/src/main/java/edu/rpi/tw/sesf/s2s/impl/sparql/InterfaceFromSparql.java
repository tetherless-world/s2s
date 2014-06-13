package edu.rpi.tw.sesf.s2s.impl.sparql;

import java.util.Vector;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.s2s.Interface;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class InterfaceFromSparql extends Interface {

	private static final long serialVersionUID = 8775449850052322975L;

	private String _uri;
	private QueryableSource _source;
	private Vector<String> _types;
	private String _output;
	private String _label;
	private String _comment;
	private String _input;
	private int _limit;
	
	public InterfaceFromSparql(String uri, QueryableSource source) throws UnregisteredInstanceException {
		_uri = uri;
		_source = source;
		_types = new Vector<>();
		_query();
	}
	
	@Override
	public String getURI() {
		return _uri;
	}

	@Override
	public Vector<String> getTypes() {
		return _types;
	}

	@Override
	public String getOutput() {
		return _output;
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
	public String getInput() {
		return _input;
	}

	@Override
	public int getLimit() {
		return _limit;
	}
	
	private void _query() throws UnregisteredInstanceException {
		ResultSet rs;
		rs =  _source.sparqlSelect(Queries.labelQuery(_uri, _source));
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
		
		//info query
		rs = _source.sparqlSelect(Queries.interfaceInfoQuery(_uri, _source));
		if (!rs.hasNext())
			throw new UnregisteredInstanceException("QueryInterface (" + _uri + ") not found in source: " + _source.toString() );
		else {
			QuerySolution qs = rs.next();
			if (qs.contains("output"))
				_output = qs.get("output").toString();
			if (qs.contains("input"))
				_input = qs.get("input").toString();
			if (qs.contains("limit"))
				_limit = Integer.parseInt(Utils.parseLiteral(qs.get("limit").toString()));
		}

		//types query
		rs = _source.sparqlSelect(Queries.typeQuery(_uri, _source));
		if (!rs.hasNext())
			throw new UnregisteredInstanceException("QueryInterface (" + _uri + ") not found in source: " + _source.toString() );
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_types.add(qs.get("o").toString());
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
			throw new IncompatibleDataSourceException("SparqlInterface requires QueryableSource.");
		}
		_source = (QueryableSource) source;
		_query();
	}
}
