/**
 * 
 */
package edu.rpi.tw.sesf.s2s.data;

import java.util.Vector;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.rpi.tw.sesf.s2s.utils.RippleQueryEngineSingleton;
import junit.framework.TestCase;

/**
 * @author rozele
 *
 */
public class DataSourceFactoryTest extends TestCase {
	
	private static final String TEST_FILE_DATASOURCE_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#FileDataSource";
	private static final String TEST_SPARQL_DATASOURCE_URI = "http://escience.rpi.edu/s2s/test/4.0/test.owl#SparqlDataSource";
	private static final String TEST_FILE_DATASOURCE_LOCATION = "http://escience.rpi.edu/s2s/test/4.0/test.owl";
	private static final String TEST_SPARQL_DATASOURCE_LOCATION = "http://escience.rpi.edu:8890/sparql";
	
	private DataSourceFactory _rippleFactory;
	private DataSourceFactory _jenaFactory;
	private DataSourceFactory _cachedFactory;

	/**
	 * @param name
	 */
	public DataSourceFactoryTest(String name) {
		super(name);
		Vector<DataSource> source1 = new Vector<>();
		Vector<DataSource> source2 = new Vector<>();
		OntModel m = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		m.read(TEST_FILE_DATASOURCE_URI);
		source1.add(new JenaPelletSource("test",m));
		source2.add(new RippleSource("test",RippleQueryEngineSingleton.getInstance()));
		_rippleFactory = new DataSourceFactory(source2, false);
		_jenaFactory = new DataSourceFactory(source1, false);
		_cachedFactory = new DataSourceFactory(source1, true);
	}

    public void testCreateSourcesFromJena() {
		DataSource source = _jenaFactory.createDataSource(TEST_FILE_DATASOURCE_URI);
		assertTrue(QueryableSource.class.isAssignableFrom(source.getClass()));
		assertTrue(((QueryableSource)source).getLocation().equals(TEST_FILE_DATASOURCE_LOCATION));
		source = _jenaFactory.createDataSource(TEST_SPARQL_DATASOURCE_URI);
		assertTrue(QueryableSource.class.isAssignableFrom(source.getClass()));
		assertTrue(((QueryableSource)source).getLocation().equals(TEST_SPARQL_DATASOURCE_LOCATION));				
	}
	
	public void testCreateCollectionFromRippleSource() {
		DataSource source = _rippleFactory.createDataSource(TEST_FILE_DATASOURCE_URI);
		assertTrue(QueryableSource.class.isAssignableFrom(source.getClass()));
		assertTrue(((QueryableSource)source).getLocation().equals(TEST_FILE_DATASOURCE_LOCATION));
		source = _rippleFactory.createDataSource(TEST_SPARQL_DATASOURCE_URI);
		assertTrue(QueryableSource.class.isAssignableFrom(source.getClass()));
		assertTrue(((QueryableSource)source).getLocation().equals(TEST_SPARQL_DATASOURCE_LOCATION));			
	}

	public void testCreateCollectionFromCache() {
		DataSource source = _cachedFactory.createDataSource(TEST_FILE_DATASOURCE_URI);
		assertTrue(QueryableSource.class.isAssignableFrom(source.getClass()));
		assertTrue(((QueryableSource)source).getLocation().equals(TEST_FILE_DATASOURCE_LOCATION));
	}
}
