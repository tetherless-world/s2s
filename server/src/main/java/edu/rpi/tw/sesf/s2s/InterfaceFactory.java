package edu.rpi.tw.sesf.s2s;

import java.util.Collection;
import java.util.Iterator;

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
import edu.rpi.tw.sesf.s2s.impl.lod.InterfaceFromRipple;
import edu.rpi.tw.sesf.s2s.impl.sparql.InterfaceFromSparql;

public class InterfaceFactory {

	private Log log = LogFactory.getLog(InterfaceFactory.class);
	
	
	private Collection<DataSource> _sources;
	private boolean _caching;
	
	private JCS _cache;
	
	public InterfaceFactory(Collection<DataSource> sources, boolean caching) {
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
	
	public Interface createInterface(String uri) {
		return createInterface(uri, true);
	}
	
	public Interface createInterface(String uri, boolean cacheOverride) {
		Interface query = null;
		
		//try to load from cache
		if (_caching && cacheOverride) {
			query = loadFromCache(uri);
			if (query != null) query.setFromCache();
		}
		
		Iterator<DataSource> iter = _sources.iterator();
		while (iter.hasNext() && query == null) {
			DataSource source = iter.next();
			if (QueryableSource.class.isAssignableFrom(source.getClass())) {
				try {
					query = new InterfaceFromSparql(uri, (QueryableSource)source);
				} catch (UnregisteredInstanceException e) {
					String message = "QueryInterface URI not found in source: " + source.toString();
					log.trace(message, e);
				}
			} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
				//try to load from linked data
				try {
					query = new InterfaceFromRipple(uri, (RippleSource)source);
				} catch(InvalidLinkedDataIdentifierException e) {
					log.trace("QueryInterface URI (" + uri + ") is not dereferenceable.", e);
				} catch (RippleException e) {
					log.trace("Error using LinkedDataSail.", e);
				}
			} else {
				log.warn("Unrecognized DataSource encountered.");
			}
		}
		
		//log if service did not load
		if (query == null) {
			log.warn("Could not get Interface information for URI: " + uri);
		}
		
		//save to cache if enabled
		if (_caching && query != null) {
			saveToCache(uri,query);
		}
		
		return query;
	}
	
	private Interface loadFromCache(String uri) {
		Object o = _cache.get("QueryInterface:" +  uri);
		if (o != null) {
			if (Interface.class.isAssignableFrom(o.getClass())) {
				return (Interface)o;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private void saveToCache(String uri, Interface obj) {
		if (_cache.get("QueryInterface:" + uri) == null) {
			try {
				_cache.putSafe("QueryInterface:" + uri, obj);
			} catch (CacheException e) {
				log.error("Unable to cache QueryInterface (" + uri + ").", e);
			}
		}
	}
}
