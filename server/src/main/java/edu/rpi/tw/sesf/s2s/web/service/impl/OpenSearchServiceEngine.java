package edu.rpi.tw.sesf.s2s.web.service.impl;

import org.apache.abdera.Abdera;
import org.apache.abdera.ext.opensearch.OpenSearchConstants;
import org.apache.abdera.ext.opensearch.model.Url;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.parser.Parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import org.openrdf.sail.SailException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.s2s.SearchService;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.exception.MalformedInstanceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.impl.sparql.SearchServiceFromSparql;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;
import edu.rpi.tw.sesf.s2s.web.service.WebServiceEngine;

public class OpenSearchServiceEngine implements WebServiceEngine {
	
	//private static final QName relAttr = new QName(OpenSearchConstants.OPENSEARCH_NS, "rel", OpenSearchConstants.OS_PREFIX);
	private final static String _osddBGP = "<{uri}> <" + Ontology.hasOpenSearchDescriptionDocument + "> ?o . ";
	
	private SearchService _service;
	private String _osdd;
	private Map<String, QueryTemplate> _interfaces;
	private Set<String> _inputs;
	
	public OpenSearchServiceEngine(SearchService service) throws IncompatibleDataSourceException, UnregisteredInstanceException, SailException, RippleException, InvalidLinkedDataIdentifierException, MalformedInstanceException {
		_service = service;
		_interfaces = new HashMap<String, QueryTemplate>();
		_inputs = new HashSet<String>();
		if (QueryableSource.class.isAssignableFrom(service.getDataSource().getClass())) {
			_query();
		} else if (RippleSource.class.isAssignableFrom(service.getDataSource().getClass())) {
			_crawl();
		}
		_parse();
	}
	
	public Collection<String> getInputs() {
		return _inputs;
	}

	public Collection<String> getInterfaces() {
		return _interfaces.keySet();
	}

	public void runQuery(String query, Map<String, String> parameters,
			HttpServletResponse response) throws IOException {
		if (!_interfaces.containsKey(query)) {
			response.sendError(422, "There is no such query ("+query+") for this OpenSearch service ("+_service.getURI()+").");
		} else {
			Utils.proxyResponse(_interfaces.get(query).createRequest(parameters), response);
		}
	}
	
	private void _query() throws UnregisteredInstanceException {
		SearchServiceFromSparql s = (SearchServiceFromSparql)_service;
		String q = Queries.buildSimpleQuery(_osddBGP, s.getURI(), ((QueryableSource)s.getDataSource()));
		ResultSet rs = ((QueryableSource)s.getDataSource()).sparqlSelect(q);
		
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o")) {
				_osdd = Utils.parseLiteral(qs.get("o").toString());
			} else {
				throw new UnregisteredInstanceException("OpenSearchService at URI (" + s.getURI() + ") is missing a pointer to its OpenSearch description document.");
			}
		} else {
			throw new UnregisteredInstanceException("OpenSearchService at URI (" + s.getURI() + ") is missing a pointer to its OpenSearch description document.");
		}
	}
	
	private void _crawl() throws SailException, RippleException, InvalidLinkedDataIdentifierException {

		Collector<RippleList,RippleException> c = new Collector<RippleList,RippleException>();
	    QueryPipe p = new QueryPipe(((RippleSource)_service.getDataSource()).getQueryEngine(), c);
	    
	    Pattern literalPattern = Pattern.compile("\"(.*?)\"(\\^\\^<(.*?)>)?");
	    
	    String uriRef = "<" + _service.getURI() + ">";
	    
	    p.put(uriRef + " <" + Ontology.hasOpenSearchDescriptionDocument + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _osdd = m.group(1);
	    }
    	c.clear();
    	
       	if (_osdd == null)
    		throw new InvalidLinkedDataIdentifierException("Missing link to OpenSearch description document needed for valid OpenSearchService from " + _service.getURI());
	}

	private void _parse() throws MalformedInstanceException {
		try {
			URL url = new URL(_osdd);
			Parser p = Abdera.getNewParser();
			Document<Element> doc = p.parse(url.openStream());
			Element root = doc.getRoot();
			for (Element el : root.getElements()) {
				Url u = null;
				if (el.getQName().equals(OpenSearchConstants.URL)) {
					u = new Url(el);
					//TODO: request bug fix for "os:rel" attribute
					String rel = u.getAttributeValue("rel");
					QueryTemplate qt = new QueryTemplate(u);
					_interfaces.put(_expandCurie(rel,u), qt);
					_inputs.addAll(qt.getParameters());
				}
			}
		} catch (MalformedURLException e) {
			throw new MalformedInstanceException("OpenSearch description document location (" + _osdd + ") is malformed.");
		} catch (IOException e) {
			throw new MalformedInstanceException("OpenSearch description document at " + _osdd + " could not be read.");
		}
	}
	
	private class QueryTemplate {
		private Url _u;
		private String _template;
		private Map<String,TemplateParameter> _params;
		
		public QueryTemplate(Url u) {
			_u = u;
			_params = new HashMap<String,TemplateParameter>();
			_getTemplate();
			_parse();
		}
		
		private void _getTemplate() {
			_template = _u.getTemplate();
		}
		
		private void _parse() {
			String[] arr = _template.split("\\{");
			boolean opt = false;
			for(int i = 1; i < arr.length; i++) {
				String[] tmp = arr[i].split("\\}");
				String curie = tmp[0];
				String last = curie.substring(curie.length() - 1);
				if (last.equals("?")) {
					curie = curie.substring(0, curie.length() - 1);
					opt = true;
				}
				String uri = OpenSearchServiceEngine._expandCurie(curie, _u);
				_params.put(uri, new TemplateParameter(curie,opt));
				opt = false;
			}
		}
		
		public Set<String> getParameters() {
			return _params.keySet();
		}
		
		public String createRequest(Map<String,String> parameters) {
			//TODO: throw error if non-optional parameter is missing
			/*for (String key : _params.keySet()) {
				if (!_params.get(key).opt && !parameters.keySet().contains(key)) {

				}
			}*/
			String request = _template;
			if (parameters != null) {
				for (String key : parameters.keySet()) {
					TemplateParameter param = _params.get(key);
					String opt = "";
					if (param != null) {
						if (param.opt) {
							opt = "\\?";
						}
						String regex = "\\{" + param.curie + opt + "\\}";
						try {
							request = request.replaceAll(regex, URLEncoder.encode(parameters.get(key), "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							//TODO: throw warning
						}
					}
				}
			}
			request = request.replaceAll("\\{.*?\\}", "");
			return request;
		}
		
		private class TemplateParameter {
			public String curie;
			public boolean opt;
			
			public TemplateParameter(String c, boolean o) {
				curie = c;
				opt = o;
			}
		}
		
	};
	
	private static String _expandCurie(String curie, Element e) {
		String[] arr = curie.split(":");
		if (arr.length > 1) {
			String ns = e.getNamespaces().get(arr[0]);
			if (ns != null) {
				return ns + arr[1];
			} else return curie;
		} else return e.getNamespaces().get("") + curie;
	}

}
