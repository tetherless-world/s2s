package edu.rpi.tw.sesf.s2s.data;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.s2s.Input;
import edu.rpi.tw.sesf.s2s.utils.Configuration;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;

public class DataSourceFactory {
	
	private Log log = LogFactory.getLog(DataSourceFactory.class);
		
	private static String sourceTypeBGP = "<{uri}> <" + Ontology.type + "> ?o";
	
	private Collection<DataSource> _sources;
	private boolean _caching;

	private JCS _cache;
	
	public DataSourceFactory(Collection<DataSource> sources, boolean caching) {
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
	
	public DataSource createDataSource(String uri) {
		return createDataSource(uri, true);
	}
	
	public DataSource createDataSource(String uri, boolean cacheOverride) {
		DataSource sourceInst = null;
		
		//try to load from cache
		if (_caching && cacheOverride) {
			sourceInst = loadFromCache(uri);
			if (sourceInst != null) sourceInst.setFromCache();
		}
		
		Iterator<DataSource> iter = _sources.iterator();
		while (iter.hasNext() && sourceInst == null) {
			DataSource source = iter.next();
			try {
				Class<? extends DataSource> sourceClass = getDataSourceType(uri, source);
				if (sourceClass != null) {
					sourceInst = sourceClass.getConstructor(String.class, DataSource.class).newInstance(uri, source);
				}
			} catch(InstantiationException | IllegalArgumentException | SecurityException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | RippleException e) {

				e.printStackTrace();
			}
        }
		
		//log if service did not load
		if (sourceInst == null) {
			log.warn("Could not get Parameter information for URI: " + uri);
		}
		
		//save to cache if enabled
		if (_caching && sourceInst != null) {
			saveToCache(uri,sourceInst);
		}
		
		return sourceInst;
	}
	
	public Class<? extends DataSource> getDataSourceType(String uri, DataSource source) throws RippleException {
		if (RippleSource.class.isAssignableFrom(source.getClass())) {
			RippleSource rsource = (RippleSource)source;
		    Collector<RippleList> c = new Collector<>();
		    QueryPipe p = new QueryPipe(rsource.getQueryEngine(), c);
		    
		    String uriRef = "<" + uri + ">";
		    
		    p.put(uriRef + " <" + Ontology.type + ">.");
	    	for (RippleList l : c) {
	    		String type = l.getFirst().toString();
	    		if (Configuration.dataSourceMap.containsKey(type)) {
	    			return Configuration.dataSourceMap.get(type);
	    		}
		    }
	    	c.clear();
		} else if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			QueryableSource qsource = (QueryableSource)source;
			ResultSet rs = qsource.sparqlSelect(Queries.buildSimpleQuery(sourceTypeBGP, uri, qsource));
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				if (qs.contains("o") && Configuration.dataSourceMap.containsKey(qs.get("o").toString())) {
					return Configuration.dataSourceMap.get(qs.get("o").toString());
				}
			}
		}
		return null;
	}
	
	private DataSource loadFromCache(String uri) {
		Object o = _cache.get("DataSource:" +  uri);
		if (o != null) {
			if (Input.class.isAssignableFrom(o.getClass())) {
				return (DataSource)o;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private void saveToCache(String uri, DataSource obj) {
		if (_cache.get("DataSource:" + uri) == null) {
			try {
				_cache.putSafe("DataSource:" + uri, obj);
			} catch (CacheException e) {
				log.error("Unable to cache DataSource (" + uri + ").", e);
			}
		}
	}
	
}