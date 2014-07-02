package edu.rpi.tw.sesf.s2s.data;

//import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;

import junit.framework.TestCase;

public class SparqlSourceTest extends TestCase {

	private static final String TEST_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#SparqlDataSource";
	private static final String TEST_ENDPOINT = "http://escience.rpi.edu:8890/sparql";
	private static final String TEST_GRAPH = "http://escience.rpi.edu/ontology/sesf/s2s-test/4/0/";
	
	private SparqlSource _source;

	/**
	 * @param name
	 * @throws InvalidLinkedDataIdentifierException 
	 * @throws UnregisteredInstanceException 
	 * @throws IncompatibleDataSourceException 
	 */
	public SparqlSourceTest(String name) throws IncompatibleDataSourceException, UnregisteredInstanceException, InvalidLinkedDataIdentifierException {
		super(name);
		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		m.read(TEST_URI);
		DataSource s = new JenaPelletSource("test",m);
		_source = new SparqlSource(TEST_URI, s);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testCreateSourcesFromJena() {
		assertTrue(_source.getLocation().equals(TEST_ENDPOINT));
		assertTrue(_source.getGraph().equals(TEST_GRAPH));
	}
}
