package edu.rpi.tw.sesf.s2s.impl.lod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import edu.rpi.tw.sesf.s2s.Input;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;

public class InputFromRipple extends Input {
	
	private static final long serialVersionUID = -8101726978668555922L;
	
	private RippleSource _source;
	private String _uri;
	private String _label;
	private String _comment;
	private String _delimiter;
	
	public InputFromRipple(String uri, RippleSource source) throws RippleException, InvalidLinkedDataIdentifierException {
		_uri = uri;
		_source = source;
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
	public String getDelimiter() {
		return _delimiter;
	}

	private void _crawl() throws RippleException, InvalidLinkedDataIdentifierException {

	    Collector<RippleList,RippleException> c = new Collector<>();
	    QueryPipe p = new QueryPipe(_source.getQueryEngine(), c);
	    
	    Pattern literalPattern = Pattern.compile("\"(.*?)\"(\\^\\^<(.*?)>)?");
	    
	    String uriRef = "<" + _uri + ">";
	    
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
    	
	    p.put(uriRef + " <" + Ontology.hasDelimiter + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _delimiter = m.group(1);
	    }
    	c.clear();
    	
    	if (_label == null)
    		throw new InvalidLinkedDataIdentifierException("Missing information needed for valid Input from " + _uri);
	}
	
	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException {
		if (!RippleSource.class.isAssignableFrom(source.getClass())) {
			throw new IncompatibleDataSourceException("LinkedDataInput requires RippleSource.");
		}
		_source = (RippleSource) source;
	}
	
}
