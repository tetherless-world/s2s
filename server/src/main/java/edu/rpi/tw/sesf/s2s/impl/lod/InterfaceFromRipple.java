package edu.rpi.tw.sesf.s2s.impl.lod;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import edu.rpi.tw.sesf.s2s.Interface;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;

public class InterfaceFromRipple extends Interface {

	private static final long serialVersionUID = -2688812864549808159L;
	
	private String _uri;
	private RippleSource _source;
	private Vector<String> _types;
	private String _output = null;
	private String _label = null;
	private String _comment = null;
	private String _input = null;
	private int _limit;
	
	public InterfaceFromRipple(String uri, RippleSource source) throws RippleException, InvalidLinkedDataIdentifierException {
		_uri = uri;
		_source = source;
		_types = new Vector<>();
		_limit = -1;
		_crawl();
	}

	@Override
	public String getURI() {
		return _uri;
	}

	@Override
	public Vector<String> getTypes() {
		return _types;
	}

	@Override
	public String getOutput() {
		return _output;
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
	public String getInput() {
		return _input;
	}

	@Override
	public int getLimit() {
		return _limit;
	}
	
	private void _crawl() throws RippleException, InvalidLinkedDataIdentifierException {

	    Collector<RippleList> c = new Collector<>();
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
	    
	    p.put(uriRef + " <" + Ontology.hasOutput + ">.");
    	for (RippleList l : c) {
    		_output = l.getFirst().toString();
	    }
    	c.clear();
    	
	    p.put(uriRef + " <" + Ontology.forInput + ">.");
    	for (RippleList l : c) {
    		_input = l.getFirst().toString();
	    }
    	c.clear();
    	
    	if (_types.size() < 1 || (_types.contains(Ontology.InputValuesInterface) && _input == null) || _output == null)
    		throw new InvalidLinkedDataIdentifierException("Missing information needed for valid Interface from " + _uri);
	}

	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException {
		if (!RippleSource.class.isAssignableFrom(source.getClass())) {
			throw new IncompatibleDataSourceException("LinkedDataInterface requires RippleSource.");
		}
		_source = (RippleSource) source;
	}
}
