package edu.rpi.tw.sesf.s2s;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONObject;

import edu.rpi.tw.sesf.s2s.web.service.WebServiceEngine;

public abstract class SearchService implements InstanceData {
		
	private static final long serialVersionUID = 7809467934624552868L;
	
	private WebServiceEngine _engine;
	private boolean _fromCache = false;
	
	public abstract String getURI();
	public abstract String getLabel();
	public abstract String getComment();
	public abstract Vector<String> getRelatedLinks();
	public abstract Vector<String> getTypes();
	public abstract String getDefaultConfigurationURL();
	
	public void setFromCache() {
		_fromCache  = true;
	}
	
	public boolean isFromCache() {
		return _fromCache;
	}
	
	public WebServiceEngine getWebServiceEngine() {
		return _engine;
	}
	
	public void setWebServiceEngine(WebServiceEngine engine) {
		_engine = engine;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JSONObject getJSON()
	{
		Map map = new HashMap();
		map.put("uri", getURI());
		if (getLabel() != null) map.put("label", getLabel());
		if (getComment() != null) map.put("comment", getComment());
		if (getDefaultConfigurationURL() != null) map.put("config", getDefaultConfigurationURL());
		if (getTypes() != null) map.put("types", getTypes().toArray());
		if (getRelatedLinks() != null) map.put("links", getRelatedLinks().toArray());
		if (this.getDataSource().getId() != null) map.put("datasource", this.getDataSource().getId());
		map.put("cached", isFromCache());
		JSONObject json = new JSONObject(map);
		return json;
	}
}
