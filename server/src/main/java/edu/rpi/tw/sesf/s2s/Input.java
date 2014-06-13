package edu.rpi.tw.sesf.s2s;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public abstract class Input implements InstanceData {

	private static final long serialVersionUID = 2674659180160519523L;
	
	private boolean _fromCache = false;

	public abstract String getURI();
	public abstract String getLabel();
	public abstract String getComment();
	public abstract String getDelimiter();

	public void setFromCache() {
		_fromCache  = true;
	}
	
	public boolean isFromCache() {
		return _fromCache;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JSONObject getJSON()
	{
		Map map = new HashMap();
		map.put("uri", getURI());
		map.put("label", getLabel());
		if (getComment() != null) map.put("comment", getComment());
		if (getDelimiter() != null) map.put("delimiter", getDelimiter());
		map.put("cached", isFromCache());
		if (this.getDataSource().getId() != null) map.put("datasource", this.getDataSource().getId());
        return new JSONObject(map);
	}
}
