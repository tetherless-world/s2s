package edu.rpi.tw.sesf.s2s.utils;

import java.util.HashMap;
import java.util.Map;

import edu.rpi.tw.sesf.facetontology.FacetType;
import edu.rpi.tw.sesf.s2s.data.DataSource;
import edu.rpi.tw.sesf.s2s.data.JenaPelletSource;
import edu.rpi.tw.sesf.s2s.data.RippleSource;
import edu.rpi.tw.sesf.s2s.data.SparqlSource;
import edu.rpi.tw.sesf.s2s.data.SparqlType;
import edu.rpi.tw.sesf.s2s.web.service.WebServiceEngine;
import edu.rpi.tw.sesf.s2s.web.service.impl.FacetOntologyServiceEngine;
import edu.rpi.tw.sesf.s2s.web.service.impl.OpenSearchServiceEngine;

public class Configuration 
{	
	public static String facetInputDelimiter = " OR ";
	public static Map<String,Class<? extends DataSource>> dataSourceMap = getDataSourceClassConfiguration();
	public static Map<String,FacetType> facetMap = getFacetMapConfiguration();
	public static Map<String,Class<? extends WebServiceEngine>> classMap = getServiceClassConfiguration();
	public static Map<String,SparqlType> sparqlTypes = getSparqlTypeConfiguration();
	
	private static Map<String,Class<? extends WebServiceEngine>> getServiceClassConfiguration() {
		//TODO: read configuration from text file
		Map<String,Class<? extends WebServiceEngine>> map = new HashMap<String,Class<? extends WebServiceEngine>>();
		map.put(Ontology.OpenSearchService, OpenSearchServiceEngine.class);
		map.put(Ontology.FacetOntologyService, FacetOntologyServiceEngine.class);
		return map;
	}
	
	private static Map<String,Class<? extends DataSource>> getDataSourceClassConfiguration() {
		Map<String,Class<? extends DataSource>> map = new HashMap<String,Class<? extends DataSource>>();
		map.put(Ontology.SparqlEndpoint, SparqlSource.class);
		map.put(Ontology.RdfFile, JenaPelletSource.class);
		map.put(Ontology.LinkedData, RippleSource.class);
		return map;
	}
	
	private static Map<String,FacetType> getFacetMapConfiguration() {
		//TODO: read configuration from text file
		Map<String,FacetType> map = new HashMap<String,FacetType>();
		map.put(Ontology.facetLiteralFacetType, FacetType.Literal);
		map.put(Ontology.facetObjectFacetType, FacetType.Object);
		return map;
	}
	
	private static Map<String,SparqlType> getSparqlTypeConfiguration() {
		Map<String,SparqlType> map = new HashMap<String,SparqlType>();
		map.put(Ontology.VirtuosoSparql, SparqlType.VirtuosoSparql);
		return map;
	}
}
