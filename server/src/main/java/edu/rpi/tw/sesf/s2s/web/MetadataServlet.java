package edu.rpi.tw.sesf.s2s.web;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

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

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.facetontology.ConnectedFacet;
import edu.rpi.tw.sesf.facetontology.FacetCollection;
import edu.rpi.tw.sesf.s2s.Input;
import edu.rpi.tw.sesf.s2s.InputFactory;
import edu.rpi.tw.sesf.s2s.Interface;
import edu.rpi.tw.sesf.s2s.InterfaceFactory;
import edu.rpi.tw.sesf.s2s.SearchService;
import edu.rpi.tw.sesf.s2s.SearchServiceFactory;
import edu.rpi.tw.sesf.s2s.Widget;
import edu.rpi.tw.sesf.s2s.WidgetFactory;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.UrlParameterException;
import edu.rpi.tw.sesf.s2s.impl.facetontology.InputFromConnectedFacet;
import edu.rpi.tw.sesf.s2s.impl.facetontology.InterfaceFromConnectedFacet;
import edu.rpi.tw.sesf.s2s.impl.facetontology.InterfaceFromFirstOrderFacet;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import edu.rpi.tw.sesf.s2s.utils.MetadataServletRequestType;
import edu.rpi.tw.sesf.s2s.utils.Utils;
import edu.rpi.tw.sesf.s2s.web.service.impl.FacetOntologyServiceEngine;

public class MetadataServlet extends HttpServlet {

	private static final long serialVersionUID = 4192965662302643319L;
	
	private Log log = LogFactory.getLog(MetadataServlet.class);

	//factory fields
	private SearchServiceFactory _searchServiceFactory;
	private WidgetFactory _widgetFactory;
	private InterfaceFactory _interfaceFactory;
	private InputFactory _inputFactory;

	private Collection<DataSource> _sources;
	
	public MetadataServlet() {}

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
		_sources = Utils.getDataSources(s2sConfig,config.getInitParameter("s2s-sources-property"));
		//boolean caching = (s2sConfig != null) ? s2sConfig.getBoolean(config.getInitParameter("s2s-caching-property"),false) : false;
		boolean caching = true;
		
		//initialize factories
		_searchServiceFactory = new SearchServiceFactory(_sources, caching);
		_widgetFactory = new WidgetFactory(_sources, caching);
		_interfaceFactory = new InterfaceFactory(_sources, caching);
		_inputFactory = new InputFactory(_sources, caching);
		
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
	
	public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {
    	    	
    	try {
        	MetadataServletRequestType rt = getRequestType(request);
        	switch(rt) {
        		case services:
        			response.addHeader("Access-Control-Allow-Origin", "*");
        			getServices(request,response);
        			break;
        		case widgets:
        			response.addHeader("Access-Control-Allow-Origin", "*");
        			getWidgets(request,response);
        			break;
        		case inputs:
        			response.addHeader("Access-Control-Allow-Origin", "*");
        			getInputs(request,response);
        			break;
        		case interfaces:
        			response.addHeader("Access-Control-Allow-Origin", "*");
        			getInterfaces(request,response);
        			break;
        		case defaults:
        			getDefaults(request,response);
        			break;
        		default:
        			break;
        	}
    	} catch (UrlParameterException e) {
    		log.error(e.getMessage(), e);
    		response.sendError(e.getErrorCode(), e.getMessage());
    	}
    }
   
    private MetadataServletRequestType getRequestType(HttpServletRequest request)
    	throws UrlParameterException {
    	String[] type = request.getParameterValues("type");
    	if (type == null || type.length != 1)
    	{
    		throw new UrlParameterException("Exactly one \"type\" parameter should be provided");
    	}
    	try {
    		return Enum.valueOf(MetadataServletRequestType.class, type[0]);
    	} catch (IllegalArgumentException e) {
    		String message = "The \"type\" parameter must contain one of: ";
    		for (int i = 0; i < MetadataServletRequestType.values().length; ++i)
    		{
    			message += MetadataServletRequestType.values()[i].toString();
    			if (i < MetadataServletRequestType.values().length - 1) message += ", ";
    		}
    		throw new UrlParameterException(422, message);
    	}
    }
    
    private void getServices(HttpServletRequest request, HttpServletResponse response) 
    	throws UrlParameterException {
    	String[] instances = request.getParameterValues("instance");
		
    	//TODO: use the cacheOverride
		boolean cacheOverride = !Utils.isNoCacheRequest(request);
		Collection<DataSource> useSources = _sources;
    	if (!cacheOverride) {
    		useSources = new Vector<DataSource>();
    		for (DataSource source : _sources) {
    			if (RippleSource.class.isAssignableFrom(source.getClass())) {
    				useSources.add(new RippleSource("lod",RippleQueryEngineSingleton.getNewInstance()));
    			} else {
    				useSources.add(source);
    			}
    		}
    	}
		
    	if (instances == null) {
    		//TODO: implement a feature to get all services?
    		throw new UrlParameterException("At least one \"instance\" parameter should be provided");
    	} 
		    	
		//TODO: set up content negotiation for servlet
		//if (request.getHeader("Accept").contains("application/json")) {}
		JSONObject returnObj = new JSONObject();
		for (String uri : instances) {
			SearchService s = _searchServiceFactory.createSearchService(uri, cacheOverride);
			if (s == null) {
				log.error("Could not instantiate SearchService for URI: " + uri);
			} else if (FacetOntologyServiceEngine.class.isAssignableFrom(s.getWebServiceEngine().getClass())) {
				FacetCollection fc = ((FacetOntologyServiceEngine)s.getWebServiceEngine()).getFacetCollection();
				
				JSONObject serviceObj = new JSONObject();
				JSONObject serviceInfo = s.getJSON();
				JSONObject inputInfo = new JSONObject();
				JSONObject interfaceInfo = new JSONObject();
				
				for (ConnectedFacet f : fc.getConnectedFacets()) {
					Input p = new InputFromConnectedFacet(f);
					try {
						inputInfo.put(p.getURI(), p.getJSON());
					} catch (JSONException e) {
						log.error("Could not add Parameter (" + p.getURI() + ") to JSON response.");
					}
				}
				for (ConnectedFacet f : fc.getConnectedFacets()) {
					Interface q = new InterfaceFromConnectedFacet(f);
					try {
						interfaceInfo.put(q.getURI(), q.getJSON());
					} catch (JSONException e) {
						log.error("Could not add QueryInterface (" + q.getURI() + ") to JSON response.");
					}
				}
				Interface q = new InterfaceFromFirstOrderFacet(fc.getFirstOrderFacet());
				try {
					interfaceInfo.put(q.getURI(), q.getJSON());
					serviceObj.put("service", serviceInfo);
					serviceObj.put("inputs", inputInfo);
					serviceObj.put("interfaces", interfaceInfo);
				} catch (JSONException e) {
					log.error("Could not add all necessary information for SearchService (" + uri + ") to JSON response.");
				}

				try {
					returnObj.put(uri, serviceObj);
				} catch (JSONException e) {
					log.error("Could not add SearchService (" + uri + ") to JSON response.");
				}
				
			} else {
				JSONObject serviceObj = new JSONObject();
				JSONObject serviceInfo = s.getJSON();
				JSONObject inputInfo = new JSONObject();
				JSONObject interfaceInfo = new JSONObject();
				for (String inputUri : s.getWebServiceEngine().getInputs()) {
					Input p = _inputFactory.createInput(inputUri, cacheOverride);
					if (p == null) {
						log.error("Could not instantiate Input for URI: " + inputUri);
					} else {
						try {
							inputInfo.put(inputUri, p.getJSON());
						} catch (JSONException e) {
							log.error("Could not add Parameter (" + inputUri + ") to JSON response.");
						}
					}
				}
				for (String interfaceUri : s.getWebServiceEngine().getInterfaces()) {
					Interface q = _interfaceFactory.createInterface(interfaceUri, cacheOverride);
					if (q == null) {
						log.error("Could not instantiate QueryInterface for URI: " + interfaceUri);
					} else {
						try {
							interfaceInfo.put(interfaceUri, q.getJSON());
						} catch (JSONException e) {
							log.error("Could not add QueryInterface (" + interfaceUri + ") to JSON response.");
						}
					}
				}
				try {
					serviceObj.put("service", serviceInfo);
					serviceObj.put("inputs", inputInfo);
					serviceObj.put("interfaces", interfaceInfo);
				} catch (JSONException e) {
					log.error("Could not add all necessary information for SearchService (" + uri + ") to JSON response.");
				}

				try {
					returnObj.put(uri, serviceObj);
				} catch (JSONException e) {
					log.error("Could not add SearchService (" + uri + ") to JSON response.");
				}
			}
		}
		response.setContentType("application/json");
		try {
			returnObj.write(response.getWriter());
			response.getWriter().flush();
		} catch (JSONException e) {
			log.error("JSON Exception writing output to ServletResponse writer.",e);
		} catch (IOException e) {
			log.error("IO Exception writing output to ServletResponse writer.",e);
		}
    }
    
    private void getWidgets(HttpServletRequest request, HttpServletResponse response) 
    	throws UrlParameterException {
    	
    	boolean cacheOverride = !Utils.isNoCacheRequest(request);
    	
    	String[] instances = request.getParameterValues("instance");
    	String klass = request.getParameter("class");
    	String parameter = request.getParameter("input");
    	String output = request.getParameter("output");
    	
    	if ((instances == null || instances.length == 0) && (parameter == null && output == null)) {
    		throw new UrlParameterException("At least one \"instance\" parameter should be included, otherwise exactly one parameter or exactly one output format should be included.");
    	}
    	    	
    	if (instances == null || instances.length == 0) {
    		instances = _getWidgetsFromInputs(klass, parameter, output);
    	}
    	
    	JSONObject returnObj = new JSONObject();
    	for (String uri : instances) {
    		Widget w = _widgetFactory.createWidget(uri, cacheOverride);
    		if (w != null) {
    			try {
    				returnObj.put(uri, w.getJSON());
    			} catch (JSONException e) {
    				log.error("Could not add Widget (" + uri + ") to JSON response.");
    			}
    		} else {
				log.error("Could not instantiate Widget (" + uri + ").");
    		}
    	}
    	
		response.setContentType("application/json");
		try {
			returnObj.write(response.getWriter());
			response.getWriter().flush();
		} catch (JSONException e) {
			log.error("JSON Exception writing output to ServletResponse writer.",e);
		} catch (IOException e) {
			log.error("IO Exception writing output to ServletResponse writer.",e);
		}
    }

	private void getInputs(HttpServletRequest request, HttpServletResponse response)
    	throws UrlParameterException {
    	
    	boolean cacheOverride = !Utils.isNoCacheRequest(request);
		
    	String[] instances = request.getParameterValues("instance");
    	String serviceUri = request.getParameter("service");
    	
    	if ((instances == null || instances.length == 0) && serviceUri == null) {
    		throw new UrlParameterException("At least one \"instance\" parameter or exactly one \"service\" parameter should be included.");
    	}
    	
    	if ((instances == null || instances.length == 0) && serviceUri != null) {
    		SearchService service = _searchServiceFactory.createSearchService(serviceUri, cacheOverride);
    		if (service == null) {
    			throw new UrlParameterException(422, "The \"service\" URI (" + serviceUri + ") could not be instantiated.");
    		}
    		Collection<String> inputs = service.getWebServiceEngine().getInputs();
    		instances = new String[inputs.size()];
    		inputs.toArray(instances);
    	}
    	
    	JSONObject returnObj = new JSONObject();
    	for (String uri : instances) {
    		Input p = _inputFactory.createInput(uri, cacheOverride);
    		try {
				returnObj.put(uri, p.getJSON());
			} catch (JSONException e) {
				log.error("Could not add Parameter (" + uri + ") to JSON response.");
			}
    	}
    	
		response.setContentType("application/json");
		try {
			returnObj.write(response.getWriter());
			response.getWriter().flush();
		} catch (JSONException e) {
			log.error("JSON Exception writing output to ServletResponse writer.",e);
		} catch (IOException e) {
			log.error("IO Exception writing output to ServletResponse writer.",e);
		}
    }
    
    private void getInterfaces(HttpServletRequest request, HttpServletResponse response) 
    	throws UrlParameterException {
    	
		boolean cacheOverride = !Utils.isNoCacheRequest(request);
    	
    	String[] instances = request.getParameterValues("instance");
    	String serviceUri = request.getParameter("service");
    	
    	if ((instances == null || instances.length == 0) && serviceUri == null) {
    		throw new UrlParameterException("At least one \"instance\" parameter or exactly one \"service\" parameter should be included.");
    	}
    	
    	if ((instances == null || instances.length == 0) && serviceUri != null) {
    		SearchService service = _searchServiceFactory.createSearchService(serviceUri, cacheOverride);
    		if (service == null) {
    			throw new UrlParameterException(422, "The \"service\" URI (" + serviceUri + ") could not be instantiated.");
    		}
    		Collection<String> interfaces = service.getWebServiceEngine().getInterfaces();
    		instances = new String[interfaces.size()];
    		interfaces.toArray(instances);
    	}
    	
    	JSONObject returnObj = new JSONObject();
    	for (String uri : instances) {
    		Interface q = _interfaceFactory.createInterface(uri, cacheOverride);
    		try {
				returnObj.put(uri, q.getJSON());
			} catch (JSONException e) {
				log.error("Could not add QueryInterface (" + uri + ") to JSON response.");
			}
    	}
    	
		response.setContentType("application/json");
		try {
			returnObj.write(response.getWriter());
			response.getWriter().flush();
		} catch (JSONException e) {
			log.error("JSON Exception writing output to ServletResponse writer.",e);
		} catch (IOException e) {
			log.error("IO Exception writing output to ServletResponse writer.",e);
		}
    }
    
    private void getDefaults(HttpServletRequest request, HttpServletResponse response) 
    	throws UrlParameterException {
    	
		boolean cacheOverride = !Utils.isNoCacheRequest(request);
    	
    	String uri = request.getParameter("service");
    	if (uri == null) {
    		throw new UrlParameterException("Exactly one \"service\" parameter should be included.");
    	}
    	
    	SearchService service = _searchServiceFactory.createSearchService(uri, cacheOverride);
    	if (service == null) {
    		throw new UrlParameterException(422, "The \"service\" URI (" + uri + ") could not be instantiated.");
    	} else {
    		try {
    			Utils.proxyResponse(service.getDefaultConfigurationURL(), response);
    		} catch (IOException e) {
    			log.error("Could not send HTTP response from: " + service.getDefaultConfigurationURL(), e);
    		}
    	}
    }
    
    private String[] _getWidgetsFromInputs(String klass, String parameter, String output) {
		Set<String> widgets = new HashSet<String>();
    	for (DataSource source : _sources) {
    		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
    			ResultSet rs = ((QueryableSource)source).sparqlSelect(Queries.widgetsFromInputs(klass, parameter, output, null, ((QueryableSource)source)));
	    		while (rs.hasNext()) {
	    			QuerySolution qs = rs.next();
	    			if (qs.contains("widget"))
	    				widgets.add(qs.get("widget").toString());
	    		}
    		}
    	}
		String[] result = new String[widgets.size()];
		widgets.toArray(result);
		return result;
    }
}