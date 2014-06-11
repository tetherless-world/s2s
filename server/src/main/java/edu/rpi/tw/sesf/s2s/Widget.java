package edu.rpi.tw.sesf.s2s;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONObject;

public abstract class Widget implements InstanceData {

	private static final long serialVersionUID = 3741799170708403099L;

	private boolean _fromCache = false;
	
	public abstract String getURI();
	public abstract String getLabel();
	public abstract String getComment();
	public abstract Vector<String> getRequiredScripts();
	public abstract Vector<String> getRequiredStylesheets();
	public abstract String getPrototype();
	public abstract Vector<String> getSupportedOutputs();
	public abstract Vector<String> getSupportedInputs();
	public abstract Vector<String> getSupportedParadigms();
	public abstract Vector<String> getTypes();
	
	public void setFromCache() {
		_fromCache = true;
	}
	
	public boolean isFromCache() {
		return _fromCache;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JSONObject getJSON()
	{
		Map map = new HashMap();
		map.put("uri", getURI());
		map.put("prototype", getPrototype());
		if (getLabel() != null) map.put("label", getLabel());
		if (getComment() != null) map.put("comment", getComment());
		if (getSupportedOutputs() != null) map.put("outputs", getSupportedOutputs().toArray());
		if (getSupportedInputs() != null) map.put("inputs", getSupportedInputs().toArray());
		if (getSupportedParadigms() != null) map.put("paradigms", getSupportedParadigms().toArray());
		if (getTypes() != null) map.put("types", getTypes().toArray());
		if (getRequiredScripts() != null) map.put("scripts", getRequiredScripts().toArray());
		if (getRequiredStylesheets() != null) map.put("css", getRequiredStylesheets().toArray());
		if (this.getDataSource().getId() != null) map.put("datasource", this.getDataSource().getId());
		map.put("cached", isFromCache());
		JSONObject json = new JSONObject(map);
		return json;
	}	
}
