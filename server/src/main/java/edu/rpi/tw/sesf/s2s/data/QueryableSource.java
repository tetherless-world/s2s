package edu.rpi.tw.sesf.s2s.data;

import com.hp.hpl.jena.query.ResultSet;

public interface QueryableSource extends DataSource {
	public ResultSet sparqlSelect(String query);
	public boolean sparqlAsk(String query);
	public void setGraph(String graph);
	public String getGraph();
	public String getLocation();
	public SparqlType getSparqlType();
}
