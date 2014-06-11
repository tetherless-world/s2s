package edu.rpi.tw.sesf.facetontology;

import java.io.Serializable;
import java.util.Collection;
import edu.rpi.tw.sesf.s2s.InstanceData;

public interface Facet extends Serializable, InstanceData {

	public String getFacetClass();

    public String getFacetLanguage();

    public String getLabel();

    public String getComment();

    public String getURI();

    public Predicate getItemLabelPredicate();

    public Collection<Predicate> getContextPredicates();

    public String getSparqlBinding();

    public Collection<String> getSparqlSelectVariables();

    public Collection<String> getSparqlAggregateVariables();

    public Collection<Filter> getFilters();

    public FacetType getFacetType();
}
