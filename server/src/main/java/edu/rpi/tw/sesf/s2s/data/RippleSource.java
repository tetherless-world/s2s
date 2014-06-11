package edu.rpi.tw.sesf.s2s.data;

import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import net.fortytwo.ripple.query.QueryEngine;

public class RippleSource implements DataSource {

	private static final long serialVersionUID = -1943592159885023059L;
	
	private String _id;
	private QueryEngine _engine;
	private DataSource _source;
	private boolean _isFromCache;
	
	public RippleSource(String id, QueryEngine engine) {
		_id = id;
		_engine = engine;
	}
	
	public QueryEngine getQueryEngine() {
		return _engine;
	}
	
	public void setQueryEngine(QueryEngine engine) {
		_engine = engine;
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

	@Override
	public String getId() {
		return _id;
	}
}
