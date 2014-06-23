package edu.rpi.tw.sesf.s2s.impl.lod;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

import edu.rpi.tw.sesf.s2s.Widget;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;

public class WidgetFromRipple extends Widget {

	private static final long serialVersionUID = -1245852077894872654L;
	
	private String _uri;
	private RippleSource _source;
	private String _label;
	private String _comment;
	private Vector<String> _scripts;
	private Vector<String> _css;
	private String _prototype;
	private Vector<String> _outputs;
	private Vector<String> _inputs;
	private Vector<String> _paradigms;
	private Vector<String> _types;

	public WidgetFromRipple(String uri, RippleSource source) throws RippleException, InvalidLinkedDataIdentifierException {
		_uri = uri;
		_source = source;
		_scripts = new Vector<>();
		_css = new Vector<>();
		_outputs = new Vector<>();
		_inputs = new Vector<>();
		_paradigms = new Vector<>();
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
	public Vector<String> getRequiredScripts() {
		return _scripts;
	}

	@Override
	public Vector<String> getRequiredStylesheets() {
		return _css;
	}

	@Override
	public String getPrototype() {
		return _prototype;
	}

	@Override
	public Vector<String> getSupportedOutputs() {
		return _outputs;
	}

	@Override
	public Vector<String> getSupportedInputs() {
		return _inputs;
	}
	
	@Override
	public Vector<String> getSupportedParadigms() {
		return _paradigms;
	}

	@Override
	public Vector<String> getTypes() {
		return _types;
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
	    
	    p.put(uriRef + " <" + Ontology.requiresJavaScript + ">.");
    	for (RippleList l : c) {
	    	Matcher m = literalPattern.matcher(l.getFirst().toString());
	    	if (m.matches() && !_scripts.contains(m.group(1))) _scripts.add(m.group(1));
	    }
    	c.clear();
    	
	    p.put(uriRef + " <" + Ontology.requiresStylesheet + ">.");
    	for (RippleList l : c) {
	    	Matcher m = literalPattern.matcher(l.getFirst().toString());
	    	if (m.matches() && !_css.contains(m.group(1))) _css.add(m.group(1));
	    }
    	c.clear();
    	
	    p.put(uriRef + " <" + Ontology.supportsInput + ">.");
    	for (RippleList l : c) {
    		if (!_inputs.contains(l.getFirst().toString()))
    			_inputs.add(l.getFirst().toString());
	    }
    	c.clear();
    	
	    p.put(uriRef + " <" + Ontology.supportsOutput + ">.");
    	for (RippleList l : c) {
    		if (!_outputs.contains(l.getFirst().toString())) {
    			
    			_outputs.add(l.getFirst().toString());
    		}
	    }
    	c.clear();
    	
	    p.put(uriRef + " <" + Ontology.supportsParadigm + ">.");
    	for (RippleList l : c) {
    		if (!_paradigms.contains(l.getFirst().toString()))
    			_paradigms.add(l.getFirst().toString());
	    }
    	c.clear();
    	
	    p.put(uriRef + " <" + Ontology.hasJavaScriptPrototype + ">.");
    	for (RippleList l : c) {
	    	Matcher m = literalPattern.matcher(l.getFirst().toString());
	    	if (m.matches()) _prototype = m.group(1);
	    }
    	c.clear();
    	
    	if (_types.size() < 1 || (_types.contains(Ontology.JavaScriptWidget) && (_prototype == null || _scripts.size() < 1)))
    		throw new InvalidLinkedDataIdentifierException("Missing information needed for valid Widget from " + _uri);
	}
	
	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException {
		if (!RippleSource.class.isAssignableFrom(source.getClass())) {
			throw new IncompatibleDataSourceException("LinkedDataWidget requires RippleSource.");
		}
		_source = (RippleSource) source;
	}
}
