package edu.rpi.tw.sesf.s2s.impl.sparql;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.s2s.Input;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class InputFromSparql extends Input {

	private static final long serialVersionUID = 8342371664245539874L;

	private QueryableSource _source;
	private String _uri;
	private String _label;
	private String _comment;
	private String _delimiter;
	
	public InputFromSparql(String uri, QueryableSource source) throws UnregisteredInstanceException
	{
		_uri = uri;
		_source = source;
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
	public String getDelimiter() {
		return _delimiter;
	}
	
	private void _query() throws UnregisteredInstanceException {
		ResultSet rs = _source.sparqlSelect(Queries.labelQuery(_uri, _source));
		if (!rs.hasNext())
			throw new UnregisteredInstanceException("S2S Input data for " + _uri + " does not exist at source: " + _source.toString() );
		else {
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
		
		rs = _source.sparqlSelect(Queries.inputInfoQuery(_uri, _source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("delimiter"))
				_delimiter = Utils.parseLiteral(qs.get("delimiter").toString());
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
			throw new IncompatibleDataSourceException("SparqlInput requires QueryableSource.");
		}
		_source = (QueryableSource)_source;
		_query();
	}

}
