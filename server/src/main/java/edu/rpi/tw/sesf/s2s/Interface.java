package edu.rpi.tw.sesf.s2s;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONObject;

public abstract class Interface implements InstanceData {
	
	private static final long serialVersionUID = 4511027545999806172L;

	private boolean _fromCache = false;

	public abstract String getURI();
	public abstract Vector<String> getTypes();
	public abstract String getOutput();
	public abstract String getLabel();
	public abstract String getComment();
	public abstract String getInput();
	//TODO: find use cases for these
	public abstract int getLimit();

	public void setFromCache() {
		_fromCache   = true;
	}
	
	public boolean isFromCache() {
		return _fromCache;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JSONObject getJSON()
	{
		Map map = new HashMap();
		map.put("uri", getURI());
		map.put("types", getTypes().toArray());
		map.put("output", getOutput());
		if (getLabel() != null) map.put("label", getLabel());
		if (getComment() != null) map.put("comment", getComment());
		if (getLimit() != -1) map.put("limit", getLimit());
		if (getInput() != null) map.put("input", getInput());
		if (this.getDataSource().getId() != null) map.put("datasource", this.getDataSource().getId());
		map.put("cached", isFromCache());
		JSONObject json = new JSONObject(map);
		return json;
	}
}
