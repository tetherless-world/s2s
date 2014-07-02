package edu.rpi.tw.sesf.s2s.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.ontology.OntModelSpec;
import net.fortytwo.flow.Collector;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.query.QueryPipe;

////import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;
import edu.rpi.tw.sesf.s2s.utils.Ontology;
import edu.rpi.tw.sesf.s2s.utils.Queries;
import edu.rpi.tw.sesf.s2s.utils.Utils;

public class JenaPelletSource implements QueryableSource {

	private static final long serialVersionUID = -6057869728372531586L;
	
	private static String _fileBGP = "<{uri}> <" + Ontology.location + "> ?o";
	private static String _formatBGP = "<{uri}> <" + Ontology.format + "> ?o";
	
	private String _id;
	private String _file;
	private String _format;
	private String _uri;
	private String _graph;
	private OntModel _model;
	private DataSource _source;
	private boolean _isFromCache;
	
	public JenaPelletSource(String uri, DataSource source) throws IncompatibleDataSourceException, UnregisteredInstanceException, InvalidLinkedDataIdentifierException {
		_uri = uri;
		_source = source;
		if (QueryableSource.class.isAssignableFrom(source.getClass())) {
			_query();
		} else if (RippleSource.class.isAssignableFrom(source.getClass())) {
			try {
				_crawl();
			} catch (RippleException e) {
				throw new InvalidLinkedDataIdentifierException("Could not find information for URI (" + uri + ")");
			}
		} else {
			throw new IncompatibleDataSourceException("Only SPARQL sources supported for now");
		}
		_model = ModelFactory.createOntologyModel();
		_model.read(_file, _format);
	}
	
	public String getFormat() {
		return _format;
	}
	
	public String getLocation() {
		return _file;
	}
	
	public JenaPelletSource(String id, OntModel model, String graph) {
		_id = id;
		_graph = graph;
		_model = model;
	}
	
	public JenaPelletSource(String id, OntModel model) {
		_id = id;
		_model = model;
	}
	
	public JenaPelletSource(String id, String url, String lang) {
		_id = id;
        _model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		_model.read(url, lang);
	}
	
	public JenaPelletSource(String id, Model model) {
		_id = id;
        _model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, model);
	}
	
	public JenaPelletSource(String id, Model model, String graph) {
		_id = id;
        _model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, model);
		_graph = graph;
	}
	
	public String getGraph() {
		return _graph;
	}
	
	public OntModel getModel() {
		return _model;
	}
	
	public void setModel(OntModel model) {
		_model = model;
	}
	
	public void setModel(Model model) {
        _model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, model);
		//_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, model);
	}

	@Override
	public ResultSet sparqlSelect(String query) {
		return Queries.sparqlSelect(query, _model);
	}

	@Override
	public void setGraph(String graph) {
		_graph = graph;
	}

	@Override
	public boolean sparqlAsk(String query) {
		return Queries.sparqlAsk(query, _model);
	}

	@Override
	public DataSource getDataSource() {
		return _source;
	}

	@Override
	public void setDataSource(DataSource source)
			throws IncompatibleDataSourceException,
			UnregisteredInstanceException {
		_source = source;
	}

	@Override
	public boolean isFromCache() {
		return _isFromCache;
	}

	@Override
	public void setFromCache() {
		_isFromCache = true;
	}
	
	private void _crawl() throws RippleException {
		RippleSource rsource = (RippleSource)_source;
		
	    Pattern literalPattern = Pattern.compile("\"(.*?)\"(\\^\\^<(.*?)>)?");
		
	    Collector<RippleList> c = new Collector<>();
	    QueryPipe p = new QueryPipe(rsource.getQueryEngine(), c);
	    
	    String uriRef = "<" + _uri + ">";
	    
	    p.put(uriRef + " <" + Ontology.location + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _file = m.group(1);
	    }
    	c.clear();
    	
    	p.put(uriRef + " <" + Ontology.format + ">.");
    	for (RippleList l : c) {
    		Matcher m = literalPattern.matcher(l.getFirst().toString());
    		if (m.matches()) _format = m.group(1);
    	}
    	c.clear();
	}
	
	private void _query() throws UnregisteredInstanceException {
		String q = Queries.buildSimpleQuery(_fileBGP, _uri, ((QueryableSource)_source));
		ResultSet rs = ((QueryableSource)_source).sparqlSelect(q);
		
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o")) {
				_file = Utils.parseLiteral(qs.get("o").toString());
			} else {
				throw new UnregisteredInstanceException("JenaPelletSource with URI (" + _uri + ") is missing a pointer to an RDF file.");
			}
		} else {
			throw new UnregisteredInstanceException("JenaPelletSource with URI (" + _uri + ") is missing a pointer to an RDF file.");
		}
		
		q = Queries.buildSimpleQuery(_formatBGP, _uri, ((QueryableSource)_source));
		rs = ((QueryableSource)_source).sparqlSelect(q);
		
		if (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (qs.contains("o")) {
				_format = Utils.parseLiteral(qs.get("o").toString());
			} else {
				throw new UnregisteredInstanceException("JenaPelletSource with URI (" + _uri + ") is missing a pointer to an RDF file.");
			}
		} else {
			throw new UnregisteredInstanceException("JenaPelletSource with URI (" + _uri + ") is missing a pointer to an RDF file.");
		}
	}

	@Override
	public SparqlType getSparqlType() {
		return SparqlType.Default;
	}

	@Override
	public String getId() {
		return (_id != null) ? _id : _uri; 
	}
}
