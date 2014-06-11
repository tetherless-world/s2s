package edu.rpi.tw.sesf.facetontology;

import java.util.Collection;
import java.util.Iterator;

import net.fortytwo.ripple.RippleException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import edu.rpi.tw.sesf.facetontology.impl.FacetCollectionImpl;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;

public class FacetCollectionFactory {
	
	private Log log = LogFactory.getLog(FacetCollectionFactory.class);
	
	private Collection<DataSource> _sources;
	private boolean _caching;
	
	private JCS _cache;
	
	public FacetCollectionFactory(Collection<DataSource> sources, boolean caching) {
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

	public FacetCollection createFacetCollection(String uri) {
		return createFacetCollection(uri, true);
	}
	
	public FacetCollection createFacetCollection(String uri, boolean cacheOverride) {
		FacetCollection facets = null;
		
		//try to load from cache
		if (_caching && cacheOverride) {
			facets = loadFromCache(uri);
			if (facets != null) facets.setFromCache();
		}
		
		Iterator<DataSource> iter = _sources.iterator();
		while (iter.hasNext() && facets == null) {
			DataSource source = iter.next();
			facets = createFacetCollection(uri, source, cacheOverride);
		}
		
		//log if service did not load
		if (facets == null) {
			log.warn("Could not get SearchService information for URI: " + uri);
		}
		
		//save to cache if enabled
		if (_caching && facets != null) {
			saveToCache(uri,facets);
		}
		
		return facets;
	}
		
	private FacetCollection createFacetCollection(String uri, DataSource source, boolean cacheOverride) {
		FacetCollection facets = null;
		
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			try {
				facets = new FacetCollectionImpl(uri, (QueryableSource) source);
			} catch (UnregisteredInstanceException e) {
				String message = "SearchService URI not found in source: " + source.toString();
				log.trace(message, e);
			} catch (RippleException e) {
				String message = "Error setting up linked data crawler for URI: " + source.toString();
				log.trace(message, e);
			}
		} else {
			log.warn("Unrecognized DataSource encountered.");
		}
		
		return facets;
	}
	
	private FacetCollection loadFromCache(String uri) {
		Object o = _cache.get("SearchService:" +  uri);
		if (o != null) {
			if (FacetCollection.class.isAssignableFrom(o.getClass())) {
				return (FacetCollection)o;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private void saveToCache(String uri, FacetCollection obj) {
		if (_cache.get("SearchService:" + uri) == null) {
			try {
				_cache.putSafe("SearchService:" + uri, obj);
			} catch (CacheException e) {
				log.error("Unable to cache SearchService (" + uri + ").", e);
			}
		}
	}
}
