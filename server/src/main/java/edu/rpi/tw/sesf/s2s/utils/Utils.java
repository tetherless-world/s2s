package edu.rpi.tw.sesf.s2s.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.data.SparqlSource;

public class Utils {

	private static Collection<String> rdfLangs = Arrays.asList("RDF/XML","N3","N-TRIPLE","TTL","TURTLE");
	private static Log log = LogFactory.getLog(Utils.class);
	
	public static String parseLiteral(String s) {
		return parseLiteral(s,false);
	}
	
	public static String parseLiteral(String s, boolean ripple) {
		String ret = s;
		if (ret.split("\\^\\^").length > 1) {
			ret = ret.split("\\^\\^")[0];
		}
		if (ripple) {
			ret = ret.substring(1, ret.length() - 2);
		}
		return ret;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isCacheRequest(HttpServletRequest request) {
    	Enumeration e = request.getHeaders("Cache-Control");
    	while (e.hasMoreElements()) {
    		if ( e.nextElement().equals("no-cache")) return false;
    	}
    	e = request.getHeaders("Pragma");
    	while (e.hasMoreElements()) {
    		if ( e.nextElement().equals("no-cache")) return false;
    	}
    	return true;
	}
	
	public static String buildQueryURL(String query, String endpoint) {
		try {
			return ((endpoint.concat("?query=")).concat(URLEncoder.encode(query, "UTF-8"))).concat("&output=xml");
		} catch (UnsupportedEncodingException e) {
			log.error("Could not encode query as UTF-8");
			return null;
		}
	}
	
    public static void proxyResponse(String loc, HttpServletResponse response) throws IOException
    {
		URL url = new URL(loc);
		URLConnection conn = url.openConnection();
		boolean access = false;
		int nBytes = -1;
		for (String key : conn.getHeaderFields().keySet()) {
			if (key != null) {
				for (String val : conn.getHeaderFields().get(key))
				{
					if (!key.equals("Transfer-Encoding"))
						response.addHeader(key, val);
					if (key.equals("Content-Length")) 
						nBytes = Integer.parseInt(val);
				}
				if (key.equals("Access-Control-Allow-Origin")) access = true;
			}
		}
		
		if (!access) {
			response.addHeader("Access-Control-Allow-Origin", "*");
		}
		
		if (nBytes < 0) {
			nBytes = 1024;
		}
		
		try (PrintWriter writer = response.getWriter()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                char[] bytes = new char[nBytes];
                int readBytes;
                while ((readBytes = br.read(bytes)) >= 0) {
                    writer.write(bytes, 0, readBytes);
                }
            }
        }
    }
    
	public static Collection<DataSource> getDataSources(org.apache.commons.configuration.Configuration config, String property) {
    	Vector<DataSource> sources = new Vector<>();
    	if (config == null) {
    		sources.add(new RippleSource("default",RippleQueryEngineSingleton.getInstance()));
    	} else {
        	String[] sourceProperties = config.getStringArray(property);
	    	for (String sourceProp : sourceProperties) {
	    		String[] sourceInfo = config.getStringArray(sourceProp);

                switch (sourceInfo[0]) {
                    case "lod":
                        sources.add(new RippleSource(sourceProp, RippleQueryEngineSingleton.getInstance()));
                        break;
                    case "sparql":
                        sources.add(new SparqlSource(sourceInfo[1], (sourceInfo.length > 2) ? sourceInfo[2] : null, (sourceInfo.length > 3) ? sourceInfo[3] : null));
                        break;
                    case "jena":
                        Model m = ModelFactory.createDefaultModel();
                        for (int i = 1; i < sourceInfo.length; i += 2) {
                            if (rdfLangs.contains(sourceInfo[i]) && i < (sourceInfo.length - 1)) {
                                if (sourceInfo[i + 1].startsWith("file://")) {
                                    try {
                                        m.read(new FileInputStream(sourceInfo[i + 1].substring(6)), null, sourceInfo[i]);
                                    } catch (FileNotFoundException e) {
                                        log.warn("Could not load data from file: " + sourceInfo[i + 1].substring(6));
                                    }
                                } else if (sourceInfo[i + 1].startsWith("http://")) {
                                    m.read(sourceInfo[i + 1], sourceInfo[i]);
                                }
                            } else {
                                log.warn("Invalid Jena lang: " + sourceInfo[i]);
                            }
                        }
                        sources.add(new JenaPelletSource(sourceProp, m));
                        break;
                }
	    	}
    	}
    	return sources;
    }
    
    public static Collection<DataSource> getDataSources(InputStream config) {
    	Vector<DataSource> sources = new Vector<>();
    	
    	if (config == null) {
			sources.add(new RippleSource("default",RippleQueryEngineSingleton.getNewInstance()));
    	} else {
    		try (BufferedReader br = new BufferedReader(new InputStreamReader(config))) {
		    	String line;
		    	while ((line = br.readLine()) != null) {
		    		String[] parts = line.split("\t");

                    switch (parts[0]) {
                        case "sparql":
                            sources.add(new SparqlSource("sparql", parts[1], parts[2], (parts.length > 3) ? parts[3] : null));
                            break;
                        case "lod":
                            sources.add(new RippleSource("lod", RippleQueryEngineSingleton.getInstance()));
                            break;
                        case "jena":
                            Model m = ModelFactory.createDefaultModel();
                            for (int i = 1; i < parts.length; i += 2) {
                                if (rdfLangs.contains(parts[i]) && i < (parts.length - 1)) {
                                    if (parts[i + 1].startsWith("file://")) {
                                        try {
                                            m.read(new FileInputStream(parts[i + 1].substring(6)), null, parts[i]);
                                        } catch (FileNotFoundException e) {
                                            log.warn("Could not load data from file: " + parts[i]);
                                        }
                                    } else if (parts[i + 1].startsWith("http://")) {
                                        m.read(parts[i + 1], parts[i]);
                                    }
                                }
                            }
                            sources.add(new JenaPelletSource("jena", m));
                            break;
                    }
		    	}
    		} catch (IOException e) {
    			sources.clear();
    			sources.add(new RippleSource("default",RippleQueryEngineSingleton.getNewInstance()));
			}
    	}
    	return sources;
    }
    
    public static String getSimpleSparqlJson(ResultSet rs) {
    	JSONArray arr = new JSONArray();
    	while (rs.hasNext()) {
    		QuerySolution qs = rs.next();
    		Map<String,String> r = new HashMap<>();
    		for (String var : rs.getResultVars()) {
    			if (qs.contains(var) && qs.get(var).isLiteral()) {
    				r.put(var,qs.get(var).asLiteral().getValue().toString());
    			} else if (qs.contains(var) && qs.get(var).isResource()) {
    				r.put(var, qs.get(var).toString());
    			}
    		}
    		arr.put(r);
    	}
    	return arr.toString();
    }
    
	public static String getFirstOrderResultsJson(ResultSet rs) {
    	JSONArray arr = new JSONArray();
    	arr.put(rs.getResultVars());
    	while (rs.hasNext()) {
    		QuerySolution qs = rs.next();
    		Map<String,String> r = new HashMap<>();
    		for (String var : rs.getResultVars()) {
    			if (qs.contains(var) && qs.get(var).isLiteral()) {
    				r.put(var,qs.get(var).asLiteral().getValue().toString());
    			} else if (qs.contains(var) && qs.get(var).isResource()) {
    				r.put(var, qs.get(var).toString());
    			}
    		}
    		arr.put(r);
    	}
    	return arr.toString();
	}
	
	public static String getPagedFirstOrderResultsJson(ResultSet rs, ResultSet count, Map<String,Collection<String>> inputs) {
    	JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
    	arr.put(rs.getResultVars());
    	while (rs.hasNext()) {
    		QuerySolution qs = rs.next();
    		Map<String,String> r = new HashMap<>();
    		for (String var : rs.getResultVars()) {
    			if (qs.contains(var) && qs.get(var).isLiteral()) {
    				r.put(var,qs.get(var).asLiteral().getValue().toString());
    			} else if (qs.contains(var) && qs.get(var).isResource()) {
    				r.put(var, qs.get(var).toString());
    			}
    		}
    		arr.put(r);
    	}
    	try {
	    	obj.put("results", arr);
	    	obj.put("total", count.next().get("count").asLiteral().getValue().toString());
	    	obj.put("limit", inputs.get(Ontology.opensearchCount));
	    	obj.put("offset", inputs.get(Ontology.opensearchStartIndex));
    	} catch (JSONException e) {
    		//TODO: error handling
            throw new RuntimeException(e);
    	}
    	return obj.toString();
	}
}
