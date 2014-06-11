package edu.rpi.tw.sesf.s2s.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rpi.tw.sesf.s2s.SearchService;
import edu.rpi.tw.sesf.s2s.SearchServiceFactory;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class ProxyServlet extends HttpServlet {
	
private static final long serialVersionUID = 4192965662302643319L;
	
	private Log log = LogFactory.getLog(MetadataServlet.class);
	
	//factory fields
	private SearchServiceFactory _searchServiceFactory;
	
	public ProxyServlet() {}
	
	@Override
	public void init(ServletConfig config) throws ServletException{
		super.init(config);
		
		//set up servlet based on configuration
		Configuration s2sConfig = null;
		try {
			s2sConfig = new PropertiesConfiguration(config.getInitParameter("s2s-properties"));
		} catch (ConfigurationException e1) {
			log.warn("Could not open S2S properties file");
		}
		Collection<DataSource> sources = Utils.getDataSources(s2sConfig,config.getInitParameter("s2s-sources-property"));
		//boolean caching = (s2sConfig != null) ? s2sConfig.getBoolean(config.getInitParameter("s2s-caching-property"),false) : false;
		boolean caching = true;
		
		//initialize factories
		_searchServiceFactory = new SearchServiceFactory(sources, caching);

		//initialize log4j
		String log4jLocation = config.getInitParameter("log4j-properties-location");
		ServletContext sc = config.getServletContext();
		if (log4jLocation == null) {
			System.err.println("*** No log4j-properties-location init param, so initializing log4j with BasicConfigurator");
			BasicConfigurator.configure();
		} else {
			String webAppPath = sc.getRealPath("/");
			String log4jProp = webAppPath + log4jLocation;
			File f = new File(log4jProp);
			if (f.exists()) {
				PropertyConfigurator.configure(log4jProp);
			} else {
				System.err.println("*** " + log4jProp + " file not found, so initializing log4j with BasicConfigurator");
				BasicConfigurator.configure();
			}
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		try {
			BufferedReader reader = request.getReader();
			String line = null;
			String content = "";
			while ((line = reader.readLine()) != null) {
				content += line + "\n";
			}
			
			JSONObject json;
			try {
				json = new JSONObject(content);
			} catch (JSONException e) {
				log.error("Could not parse the JSON request.", e);
				response.sendError(400, "Could not parse the JSON request.");
				return;
			}
			
			String service = null;
			String _interface = null;
			
			try {
				service = json.getString("service");
			} catch (JSONException e) {
				log.error("Could not retrieve string from \"service\" field in JSON request.", e);
				response.sendError(400, "Could not retrieve string from \"service\" field in JSON request.");
				return;
			}
			
			try {
				_interface = json.getString("interface");
			} catch (JSONException e) {
				log.error("Could not retrieve string from \"interface\" field in JSON request.", e);
				response.sendError(400, "Could not retrieve string from \"interface\" field in JSON request.");
				return;
			}
			
			Map<String, String> inputs = null;
			try {
				inputs = _parseInputs(json.getJSONObject("inputs"));
			} catch (JSONException e) {
				log.error("Could not retrieve string from \"inputs\" field in JSON request.", e);
			}
			
			SearchService s = _searchServiceFactory.createSearchService(service);
			if (s == null) {
				log.error("Could not instantiate a SearchService for the URI: " + service);
				response.sendError(422, "Could not instantiate a SearchService for the URI: " + service);
			} else {
				s.getWebServiceEngine().runQuery(_interface, inputs, response);
			}
			
		} catch (IOException e) {
			log.error("Malformed JSON received by server.", e);
			response.sendError(400, "Malformed JSON received by server.");
		}
	}

	@SuppressWarnings("rawtypes")
	private Map<String, String> _parseInputs(JSONObject json) {
		Map<String, String> ret = new HashMap<String, String>();
		Iterator i = json.keys();
		while (i.hasNext()) {
			String s = i.next().toString();
			try {
				ret.put(s, json.getString(s));
			} catch (JSONException e) {
				log.error("Could not retrieve the parameter value for parameter: " + s, e);
			}
		}
		return ret;
	}
}
