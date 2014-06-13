package edu.rpi.tw.sesf.s2s.impl.lod;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import edu.rpi.tw.sesf.s2s.SearchService;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;

public class SearchServiceFromRipple extends SearchService {

	private static final long serialVersionUID = -2877158854469361889L;
	
	private String _uri;
	private RippleSource _source;
	private String _label;
	private String _comment;
	private Vector<String> _links;
	private Vector<String> _types;
	private String _config;
	
	public SearchServiceFromRipple(String uri, RippleSource source) throws RippleException, InvalidLinkedDataIdentifierException {
		_uri = uri;
		_source = source;
		_links = new Vector<>();
		_types = new Vector<>();
		_crawl();
	}
	
	@Override
	public String getURI() {
		return _uri;
	}

	@Override
	public String getLabel() {
		return _label;
	}

	@Override
	public String getComment() {
		return _comment;
	}

	@Override
	public Vector<String> getRelatedLinks() {
		return _links;
	}

	@Override
	public Vector<String> getTypes() {
		return _types;
	}

	@Override
	public String getDefaultConfigurationURL() {
		return _config;
	}
	
	private void _crawl() throws RippleException, InvalidLinkedDataIdentifierException {

	    Collector<RippleList,RippleException> c = new Collector<>();
	    QueryPipe p = new QueryPipe(_source.getQueryEngine(), c);
	    
	    Pattern literalPattern = Pattern.compile("\"(.*?)\"(\\^\\^<(.*?)>)?");
	    
	    String uriRef = "<" + _uri + ">";
	    
	    p.put(uriRef + " <" + Ontology.type + ">.");
    	for (RippleList l : c) {
    		if (!_types.contains(l.getFirst().toString()))
    			_types.add(l.getFirst().toString());
	    }
    	c.clear();
	    
	    p.put(uriRef + " <" + Ontology.label + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _label = m.group(1);
	    }
    	c.clear();
    	
	    p.put(uriRef + " <" + Ontology.comment + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _comment = m.group(1);
	    }
    	c.clear();
	    
	    p.put(uriRef + " <" + Ontology.seeAlso + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches() && !_links.contains(m.group(1))) _links.add(m.group(1));
	    }
    	c.clear();
    	
	    p.put(uriRef + " <" + Ontology.hasDefaultConfiguration + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _config = m.group(1);
	    }
    	c.clear();
    	
    	if (_types.size() < 1)
    		throw new InvalidLinkedDataIdentifierException("Missing information needed for valid SearchService from " + _uri);
	}
	
	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException {
		if (!RippleSource.class.isAssignableFrom(source.getClass())) {
			throw new IncompatibleDataSourceException("LinkedDataSearchService requires RippleSource.");
		}
		_source = (RippleSource) source;
	}
	
	

}
