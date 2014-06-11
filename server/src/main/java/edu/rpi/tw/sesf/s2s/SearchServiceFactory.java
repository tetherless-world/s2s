package edu.rpi.tw.sesf.s2s;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import net.fortytwo.ripple.RippleException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.impl.lod.SearchServiceFromRipple;
import edu.rpi.tw.sesf.s2s.impl.sparql.SearchServiceFromSparql;
import edu.rpi.tw.sesf.s2s.utils.Configuration;
import edu.rpi.tw.sesf.s2s.web.service.WebServiceEngine;

public class SearchServiceFactory {
	
	private Log log = LogFactory.getLog(SearchServiceFactory.class);
	
	private Collection<DataSource> _sources;
	private boolean _caching;
	
	private JCS _cache;
	
	public SearchServiceFactory(Collection<DataSource> sources, boolean caching) {
		_sources = sources;
		_caching = caching;
		if (_caching) {
			try {
				_cache = JCS.getInstance("s2sCache");
			} catch (CacheException e) {
				log.error("Unable to setup JCS cache; caching not enabled.", e);
				_caching = false;
			}
		}
	}

	public SearchService createSearchService(String uri) {
		return createSearchService(uri, true);
	}
	
	public SearchService createSearchService(String uri, boolean cacheOverride) {
		SearchService service = null;
		
		//try to load from cache
		if (_caching && cacheOverride) {
			service = loadFromCache(uri);
			if (service != null) service.setFromCache();
		}
		
		Iterator<DataSource> iter = _sources.iterator();
		while (iter.hasNext() && service == null) {
			DataSource source = iter.next();
			try {
				service = createSearchService(uri, source, cacheOverride);
			} catch (InvocationTargetException e) {
				log.warn("Error instantiating a WebServiceEngine for DataSource: " + source.toString());
			}
		}
		
		//log if service did not load
		if (service == null) {
			log.warn("Could not get SearchService information for URI: " + uri);
		}
		
		//save to cache if enabled
		if (_caching && service != null) {
			saveToCache(uri,service);
		}
		
		return service;
	}
	
	private SearchService createSearchService(String uri, DataSource source, boolean cacheOverride) throws InvocationTargetException {
		SearchService service = null;
		
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			try {
				service = new SearchServiceFromSparql(uri, (QueryableSource)source);
			} catch (UnregisteredInstanceException e) {
				String message = "SearchService URI not found in source: " + source.toString();
				log.trace(message, e);
			}
		} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
			//try to load from linked data
			try {
				service = new SearchServiceFromRipple(uri, (RippleSource)source);
			} catch(InvalidLinkedDataIdentifierException e) {
				log.trace("SearchService URI (" + uri + ") is not dereferenceable.", e);
			} catch (RippleException e) {
				log.trace("Error using LinkedDataSail.", e);
			}
		} else {
			log.warn("Unrecognized DataSource encountered.");
		}
		
		if (service != null) {
			WebServiceEngine engine = getWebServiceEngine(service);
			if (engine == null) {
				service = null;
				log.error("Could not instantiate Web service engine for SearchService (" + uri + ")");
			} else {
				service.setWebServiceEngine(engine);
			}
		}
		
		return service;
	}

	private WebServiceEngine getWebServiceEngine(SearchService service) throws InvocationTargetException {
		Vector<String> types = service.getTypes();
		WebServiceEngine engine = null;
		for (String serviceClassURI : types) {
			if (Configuration.classMap.containsKey(serviceClassURI)) {
				Class<? extends WebServiceEngine> engineClass = Configuration.classMap.get(serviceClassURI);
				try {
					engine = engineClass.getConstructor(SearchService.class).newInstance(service);
				} catch (SecurityException e) {
					log.error("Security exception: unable to get constructor for class (" + serviceClassURI + ").", e);
				} catch (NoSuchMethodException e) {
					log.error("Constructor does not exist: unable to get constructor for class (" + serviceClassURI + ").", e);
				} catch (IllegalArgumentException e) {
					log.error("Illegal arguments: can't create WebServiceEngine for class (" + serviceClassURI + ").", e);
				} catch (InstantiationException e) {
					log.error("Instantiation exception: can't create WebServiceEngine for class (" + serviceClassURI + ").", e);
				} catch (IllegalAccessException e) {
					log.error("Illegal access exception: can't create WebServiceEngine for class (" + serviceClassURI + ").", e);
				}
			}
		}
		
		return engine;
	}
	
	private SearchService loadFromCache(String uri) {
		Object o = _cache.get("SearchService:" +  uri);
		if (o != null) {
			if (SearchService.class.isAssignableFrom(o.getClass())) {
				return (SearchService)o;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private void saveToCache(String uri, SearchService obj) {
		if (_cache.get("SearchService:" + uri) == null) {
			try {
				_cache.putSafe("SearchService:" + uri, obj);
			} catch (CacheException e) {
				log.error("Unable to cache SearchService (" + uri + ").", e);
			}
		}
	}
}