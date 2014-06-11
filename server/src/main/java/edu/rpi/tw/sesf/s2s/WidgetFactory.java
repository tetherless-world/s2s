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
import edu.rpi.tw.sesf.s2s.impl.lod.WidgetFromRipple;
import edu.rpi.tw.sesf.s2s.impl.sparql.WidgetFromSparql;

public class WidgetFactory {

	private Log log = LogFactory.getLog(WidgetFactory.class);
	
	private Collection<DataSource> _sources;
	private boolean _caching;
	
	private JCS _cache;
	
	public WidgetFactory(Collection<DataSource> sources, boolean caching) {
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

	public Widget createWidget(String uri) {
		return createWidget(uri, true);
	}
	
	public Widget createWidget(String uri, boolean cacheOverride) {
		Widget widget = null;
		
		//try to load from cache
		if (_caching && cacheOverride) {
			widget = loadFromCache(uri);
			if (widget != null) widget.setFromCache();
		}
		
		Iterator<DataSource> iter = _sources.iterator();
		while (iter.hasNext() && widget == null) {
			DataSource source = iter.next();
			if (QueryableSource.class.isAssignableFrom(source.getClass())) {
				try {
					widget = new WidgetFromSparql(uri, (QueryableSource)source);
				} catch (UnregisteredInstanceException e) {
					String message = "Widget URI not found in source: " + source.toString();
					log.trace(message, e);
				}
			} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
				//try to load from linked data
				try {
					widget = new WidgetFromRipple(uri, (RippleSource)source);
				} catch(InvalidLinkedDataIdentifierException e) {
					log.trace("Widget URI (" + uri + ") is not dereferenceable.", e);
				} catch (RippleException e) {
					log.trace("Error using LinkedDataSail.", e);
				}
			} else {
				log.warn("Unrecognized DataSource encountered.");
			}
		}
		
		//log if service did not load
		if (widget == null) {
			log.warn("Could not get Widget information for URI: " + uri);
		}
		
		//save to cache if enabled
		if (_caching && widget != null) {
			saveToCache(uri,widget);
		}
		
		return widget;
	}
	
	private Widget loadFromCache(String uri) {
		Object o = _cache.get("Widget:" +  uri);
		if (o != null) {
			if (Widget.class.isAssignableFrom(o.getClass())) {
				return (Widget)o;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private void saveToCache(String uri, Widget obj) {
		if (_cache.get("Widget:" + uri) == null) {
			try {
				_cache.putSafe("Widget:" + uri, obj);
			} catch (CacheException e) {
				log.error("Unable to cache Widget (" + uri + ").", e);
			}
		}
	}

}
