package edu.rpi.tw.sesf.s2s.impl.sparql;

import java.util.Vector;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import edu.rpi.tw.sesf.s2s.Widget;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.QueryableSource;
import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class WidgetFromSparql extends Widget {

	private static final long serialVersionUID = 8123520831418467542L;
	
	private String _uri;
	private String _label;
	private String _comment;
	private Vector<String> _scripts;
	private Vector<String> _css;
	private Vector<String> _outputs;
	private Vector<String> _inputs;
	private Vector<String> _types;
	private QueryableSource _source;
	private Vector<String> _paradigms;
	private String _prototype;
	
	public WidgetFromSparql(String uri, QueryableSource source) throws UnregisteredInstanceException {
		_uri = uri;
		_source = source;
		_scripts = new Vector<>();
		_css = new Vector<>();
		_outputs = new Vector<>();
		_inputs = new Vector<>();
		_paradigms = new Vector<>();
		_types = new Vector<>();
		_query();
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
	
	private void _query() throws UnregisteredInstanceException {
				
		//comment query
		ResultSet rs = _source.sparqlSelect(Queries.labelQuery(_uri, _source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_label = Utils.parseLiteral(qs.get("o").toString());
		}
		
		//label query
		rs = _source.sparqlSelect(Queries.commentQuery(_uri, _source));
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_comment = Utils.parseLiteral(qs.get("o").toString());
		}
		
		//types query
		rs = _source.sparqlSelect(Queries.typeQuery(_uri, _source));
		if (!rs.hasNext())
			throw new UnregisteredInstanceException("S2S Widget (" + _uri + ") does not exist at source: " + _source.toString() );
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_types.add(qs.get("o").toString());
		}
		
		//scripts query
		rs = _source.sparqlSelect(Queries.widgetScriptsQuery(_uri, _source));
		if (!rs.hasNext() && _types.contains(Ontology.JavaScriptWidget))
			throw new UnregisteredInstanceException("S2S Widget (" + _uri + ") does not exist at source: " + _source.toString() );
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_scripts.add(Utils.parseLiteral(qs.get("o").toString()));
		}
		
		//prototype query
		rs = _source.sparqlSelect(Queries.widgetPrototypeQuery(_uri, _source));
		if (!rs.hasNext() && _types.contains(Ontology.JavaScriptWidget))
			throw new UnregisteredInstanceException("S2S Widget (" + _uri + ") does not exist at source: " + _source.toString() );
		else {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_prototype = Utils.parseLiteral(qs.get("o").toString());
		}
		
		//stylesheets query
		rs = _source.sparqlSelect(Queries.widgetStylesheetsQuery(_uri, _source));
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_css.add(Utils.parseLiteral(qs.get("o").toString()));
		}
	
		//output support query
		rs = _source.sparqlSelect(Queries.widgetOutputSupportQuery(_uri, _source));	
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_outputs.add(qs.get("o").toString());
		}
		
		//parameter support query
		rs = _source.sparqlSelect(Queries.widgetInputSupportQuery(_uri, _source));	
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_inputs.add(qs.get("o").toString());
		}
		
		//paradigm support
		rs = _source.sparqlSelect(Queries.widgetParadigmSupportQuery(_uri, _source));
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o"))
				_paradigms.add(qs.get("o").toString());
		}
	}

	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException, UnregisteredInstanceException {
		if (!QueryableSource.class.isAssignableFrom(source.getClass())) {
			throw new IncompatibleDataSourceException("SparqlWidget requires QueryableSource.");
		}
		_source = (QueryableSource) source;
		_query();
	}
}
