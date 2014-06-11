package edu.rpi.tw.sesf.s2s.data;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.s2s.exception.IncompatibleDataSourceException;
import edu.rpi.tw.sesf.s2s.exception.InvalidLinkedDataIdentifierException;
import edu.rpi.tw.sesf.s2s.exception.UnregisteredInstanceException;

import junit.framework.TestCase;

public class JenaPelletSourceTest extends TestCase {
	private static final String TEST_FILE_DATASOURCE_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#FileDataSource";
	private static final String TEST_FILE_DATASOURCE_LOCATION = "http://escience.rpi.edu/s2s/test/4.0/test.owl";
	private static final String TEST_FILE_DATASOURCE_FORMAT = "RDF/XML";
	
	private JenaPelletSource _source;

	/**
	 * @param name
	 * @throws InvalidLinkedDataIdentifierException 
	 * @throws UnregisteredInstanceException 
	 * @throws IncompatibleDataSourceException 
	 */
	public JenaPelletSourceTest(String name) throws IncompatibleDataSourceException, UnregisteredInstanceException, InvalidLinkedDataIdentifierException {
		super(name);
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_FILE_DATASOURCE_URI);
		DataSource s = new JenaPelletSource("test",m);
		_source = new JenaPelletSource(TEST_FILE_DATASOURCE_URI, s);
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
		assertTrue(_source.getLocation().equals(TEST_FILE_DATASOURCE_LOCATION));
		assertTrue(_source.getFormat().equals(TEST_FILE_DATASOURCE_FORMAT));
	}
}
