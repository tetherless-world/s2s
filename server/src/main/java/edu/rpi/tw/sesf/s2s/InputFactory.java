package edu.rpi.tw.sesf.s2s;

import java.util.Collection;
import java.util.Iterator;

import net.fortytwo.ripple.RippleException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import edu.rpi.tw.sesf.facetontology.FacetCollection;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.impl.facetontology.InputFromConnectedFacet;
import edu.rpi.tw.sesf.s2s.impl.lod.InputFromRipple;
import edu.rpi.tw.sesf.s2s.impl.sparql.InputFromSparql;

public class InputFactory {

	private Log log = LogFactory.getLog(InputFactory.class);
	
	private Collection<DataSource> _sources;
	private boolean _caching;
	
	private JCS _cache;
	
	public InputFactory( Collection<DataSource> sources, boolean caching) {
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
	
	public Input createInput(String uri) {
		return createInput(uri, true);
	}
	
	public Input createInputFromFacetOntology(String uri, FacetCollection collection) {
		return new InputFromConnectedFacet(collection.getConnectedFacet(uri));
	}
	
	public Input createInput(String uri, boolean cacheOverride) {
		Input param = null;
		
		//try to load from cache
		if (_caching && cacheOverride) {
			param = loadFromCache(uri);
			if (param != null) param.setFromCache();
		}
		
		Iterator<DataSource> iter = _sources.iterator();
		while (iter.hasNext() && param == null) {
			DataSource source = iter.next();
			if (QueryableSource.class.isAssignableFrom(source.getClass())) {
				try {
					param = new InputFromSparql(uri, (QueryableSource)source);
				} catch (UnregisteredInstanceException e) {
					String message = "Parameter URI not found in source: " + source.toString();
					log.trace(message, e);
				}
			} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
				//try to load from linked data
				try {
					param = new InputFromRipple(uri, (RippleSource)source);
				} catch(InvalidLinkedDataIdentifierException e) {
					log.trace("Parameter URI (" + uri + ") is not dereferenceable.", e);
				} catch (RippleException e) {
					log.trace("Error using LinkedDataSail.", e);
				}
			} else {
				log.error("Unrecognized DataSource encountered.");
			}
		}
		
		//log if service did not load
		if (param == null) {
			log.warn("Could not get Parameter information for URI: " + uri);
		}
		
		//save to cache if enabled
		if (_caching && param != null) {
			saveToCache(uri,param);
		}
		
		return param;
	}
	
	private Input loadFromCache(String uri) {
		Object o = _cache.get("Parameter:" +  uri);
		if (o != null) {
			if (Input.class.isAssignableFrom(o.getClass())) {
				return (Input)o;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private void saveToCache(String uri, Input obj) {
		if (_cache.get("Parameter:" + uri) == null) {
			try {
				_cache.putSafe("Parameter:" + uri, obj);
			} catch (CacheException e) {
				log.error("Unable to cache Parameter (" + uri + ").", e);
			}
		}
	}
}
