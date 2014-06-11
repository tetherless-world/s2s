package edu.rpi.tw.sesf.facetontology;

import java.io.Serializable;

import edu.rpi.tw.sesf.s2s.InstanceData;

public interface Filter extends Serializable, InstanceData {
	public Predicate getFirstPredicate();
	public String getSparqlGraph();
	public boolean isOptional();
	public String getFilterValue();
}
