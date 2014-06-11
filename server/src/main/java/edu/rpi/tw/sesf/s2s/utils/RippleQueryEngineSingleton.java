package edu.rpi.tw.sesf.s2s.utils;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.Model;
import net.fortytwo.ripple.model.impl.sesame.SesameModel;
import net.fortytwo.ripple.query.QueryEngine;

public class RippleQueryEngineSingleton {
	
	private static Log log = LogFactory.getLog(RippleQueryEngineSingleton.class);
	
    private static final QueryEngine instance = init();
    
    // Private constructor prevents instantiation from other classes
    private RippleQueryEngineSingleton() { }

    public static QueryEngine getInstance() {
    	return instance;
    }
    
    public static QueryEngine getNewInstance() {
    	Sail baseSail = new MemoryStore();
	    try {
			baseSail.initialize();
		    LinkedDataSail sail = new LinkedDataSail(baseSail);
		    sail.initialize();
		    Model model = new SesameModel(sail);
		    QueryEngine qe = new QueryEngine(model);
		    return qe;
		} catch (SailException e) {
			log.error(e.getMessage(), e);
			return null;
		} catch (RippleException e) {
			log.error(e.getMessage(), e);
			return null;
		}
    }
    
    private static QueryEngine init() {
    	Properties props = new Properties();
    	props.setProperty("net.fortytwo.ripple.io.httpConnectionTimeout", "1000");
    	try {
			Ripple.initialize(props);
		} catch (RippleException e) {
			return null;
		}
    	return getNewInstance();
    }
}
