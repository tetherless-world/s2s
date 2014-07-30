<?php

include_once "utils.php";
include_once "config.php";

class RDS_S2SConfig extends S2SConfig
{
	public $VIVO_URL_PREFIX = "http://data.tw.rpi.edu/rds-vivo/individual";
	
	private $namespaces = array(
		'foaf'	=> "http://xmlns.com/foaf/0.1/",
		'rdfs'	=> "http://www.w3.org/2000/01/rdf-schema#",
		'time'	=> "http://www.w3.org/2006/time#",
		'vivo'	=> "http://vivoweb.org/ontology/core#",
		'vitro'	=> "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#",
		'bibo'	=> "http://purl.org/ontology/bibo/",
		'xsd'	=> "http://www.w3.org/2001/XMLSchema#",
		'skos'	=> "http://www.w3.org/2004/02/skos/core#",
		'owl'	=> "http://www.w3.org/2002/07/owl#",
		'dct'	=> "http://purl.org/dc/terms/",
		'dc'	=> "http://purl.org/dc/elements/1.1/",
		'obo'	=> "http://purl.obolibrary.org/obo/",
		'dcat'	=> "http://www.w3.org/ns/dcat#"
	);


	public function getEndpoint() {
		return "http://data.tw.rpi.edu/rds-vivo/admin/sparqlquery";
	}

	public function getNamespaces() {
		return $this->namespaces;
	}
	
	public function sparqlSelect($query) {
	
		$options = array(
			CURLOPT_CONNECTTIMEOUT => 10,
			CURLOPT_TIMEOUT => 60
		);
				
		$encoded_query = 'query=' . urlencode($query) . '&resultFormat=RS_XML';
		return execSelect($this->getEndpoint(), $encoded_query, $options);
	}
	
	private function getAuthorsByDataset($dataset) {
				
		$query = $this->getPrefixes();
		$query .= "SELECT DISTINCT ?uri ?name WHERE { ";
		$query .= "?authorship vivo:relates <$dataset> . ";
		$query .= "?authorship a vivo:Authorship . ";
		$query .= "?authorship vivo:relates ?author . ";
		$query .= "?author a foaf:Agent . ";
		$query .= "?author rdfs:label ?l . ";
		$query .= "BIND(str(?author) AS ?uri ) . ";
		$query .= "BIND(str(?l) AS ?name) } ";
				
		return $this->sparqlSelect($query);
	}
	
	private function getContributorsByDataset($dataset) {
	
		$query = $this->getPrefixes();
		$query .= "SELECT DISTINCT ?uri ?name WHERE { ";
		$query .= "<$dataset> dct:contributor ?contributor . ";
		$query .= "?contributor a foaf:Agent . ";
		$query .= "?contributor rdfs:label ?label . ";
		$query .= "BIND(str(?contributor) AS ?uri) . ";
		$query .= "BIND(str(?label) AS ?name) } ";
				
		return $this->sparqlSelect($query);
	}
	
	private function getCatalogsByDataset($dataset) {
		
		$query = $this->getPrefixes();
		$query .= "SELECT DISTINCT ?uri ?name WHERE { ";
		$query .= "?catalog dcat:dataset <$dataset> . ";
		$query .= "?catalog a dcat:Catalog . ";
		$query .= "?catalog rdfs:label ?title . ";
		$query .= "BIND(str(?catalog) AS ?uri) . ";
		$query .= "BIND(str(?title) AS ?name) }";
		
		return $this->sparqlSelect($query);
	}
	
	private function getKeywordsByDataset($dataset) {
	
		$query = $this->getPrefixes();
		$query .= "SELECT DISTINCT ?keyword WHERE { ";
		$query .= "<$dataset> vivo:freetextKeyword ?k . ";
		$query .= "BIND(str(?k) AS ?keyword) } ";
		
		return $this->sparqlSelect($query);
	}
	
	// override
	public function getSearchResultCount(array $constraints) {
		
		$query = $this->getSelectQuery("count", $constraints);
		$results = $this->sparqlSelect($query);
		$result = $results[0];
		return $result['count'];
	}
	
	// override	
	public function getSearchResultOutput(array $result) {
							
		$dataset = $result['dataset'];
		$html = "<div class='result-list-item'>";
	
		// title
		$dataset_vivo_url = $this->VIVO_URL_PREFIX . "?uri=" . urlencode($dataset);
		$html .= "<span class='title'><a target='_blank_' href=\"" . $dataset_vivo_url . "\">" . $result['label'] . "</a></span>";	
	
		// description
		if(isset($result['description'])) {
			$description = $result['description'];
			$summary_end = strpos($description, '.') + 1;
			$description_summary = substr($description, 0, $summary_end);
			$html .= "<br /><span>Description: " . $description_summary . "</span>";
		}

		// handle
		if(isset($result['id'])) {
			$html .= "<br /><span>Handle: " . $result['id'] . "</span>";
		}
	
		// year issued
		if(isset($result['issued'])) {
			$html .= "<br /><span>Date Issued: " . $result['issued'] . "</span>";
		}
				
		// authors
		$authors = $this->getAuthorsByDataset($dataset);
									
		if (count($authors) > 0) {
			$html .= "<br /><span>Authors: ";
			$authors_markup = array();
			foreach ($authors as $i => $author) {
				$author_vivo_url = $this->VIVO_URL_PREFIX . "?uri=" . urlencode($author['uri']);
				array_push($authors_markup, "<a target='_blank_' href=\"" . $author_vivo_url . "\">" . $author['name'] . "</a>");
			}
			$html .= implode('; ', $authors_markup);
			$html .= "</span>";
		}
		
		// contributors
		$contributors = $this->getContributorsByDataset($dataset);
					
		if (count($contributors) > 0) {
			$html .= "<br /><span>Contributors: ";
			$contributors_markup = array();
			foreach ($contributors as $i => $contributor) {
				$contributor_vivo_url = $this->VIVO_URL_PREFIX . "?uri=" . urlencode($contributor['uri']);
				array_push($contributors_markup, "<a target='_blank_' href=\"" . $contributor_vivo_url . "\">" . $contributor['name'] . "</a>");
			}
			if (substr($html, -1) == ">") $html .= "; ";
			$html .= implode('; ', $contributors_markup);
			$html .= "</span>";
		}
		
		// dataset catalogs
		$catalogs = $this->getCatalogsByDataset($dataset);
					
		if(count($catalogs) > 0) {
			$html .= "<br /><span>Catalogs: ";
			$catalogs_markup = array();
			foreach ($catalogs as $i => $catalog) {
				$catalog_vivo_url = $this->VIVO_URL_PREFIX . "?uri=" . urlencode($catalog['uri']);
				array_push($catalogs_markup, "<a target='_blank_' href=\"" . $catalog_vivo_url . "\">" . $catalog['name'] . "</a>");
			}
			$html .= implode('; ', $catalogs_markup);
			$html .= "</span>";
		}
		
		// keywords
		$keywords = $this->getKeywordsByDataset($dataset);
					
		if(count($keywords) > 0) {
			$html .= "<br /><span>Keywords: ";
			$keywords_markup = array();
			foreach ($keywords as $i => $keyword) {
				array_push($keywords_markup, $keyword['keyword']);
			}
			$html .= implode('; ', $keywords_markup);
			$html .= "</span>";
		}
		
		// landing pages
		if(isset($result['landingPage'])) {
			$html .= "<br /><span>Landing Page: <a target='_blank_' href=\"" . $result['landingPage'] . "\">" . $result['landingPage'] . "</a></span>";
		}
	
		$html .= "</div>";				
		return $html;
	}
	
	public function getQueryHeader($type) {
	
		$header = "";
		switch($type) {
			case "datasets":
				$header .= "?dataset ?id ?label ?description ?issued ?landingPage";
				break;
			case "count":
				$header .= "(count(DISTINCT ?dataset) AS ?count)";
				break;
			default:
				$header .= "?id ?label (COUNT(DISTINCT ?dataset) AS ?count)";
				break;
		}
		return $header;
	}
	
	public function getQueryFooter($type, $limit=null, $offset=0, $sort=null) {
	
		$footer = "";
		switch($type) {
			case "datasets":
				$footer .= " LIMIT $limit OFFSET $offset";
				break;
			case "count":
				break;
			default:
				$footer .= " GROUP BY ?label ?id";
				break;
		}
		return $footer;
	}
	
	public function getQueryBody($type) {
		
		$body = "";
		switch($type) {
			case "authors":
				$body .= "?dataset a dcat:Dataset . ";
				$body .= "?authorship vivo:relates ?dataset . ";
				$body .= "?authorship a vivo:Authorship . ";
				$body .= "?authorship vivo:relates ?author . ";
				$body .= "?author a foaf:Agent . ";
				$body .= "?author rdfs:label ?l . ";
				$body .= "BIND(str(?author) AS ?id ) . ";
				$body .= "BIND(str(?l) AS ?label) . ";
				break;
				
			case "contributors":
				$body .= "?dataset a dcat:Dataset . ";
				$body .= "?dataset dct:contributor ?agent . ";
				$body .= "?agent a foaf:Agent . ";
				$body .= "?agent rdfs:label ?l . ";
				$body .= "BIND(str(?agent) AS ?id) . ";
				$body .= "BIND(str(?l) AS ?label) . ";
				break;
				
			case "catalogs":
				$body .= "?dataset a dcat:Dataset . ";
				$body .= "?catalog dcat:dataset ?dataset . ";
				$body .= "?catalog a dcat:Catalog . ";
				$body .= "?catalog rdfs:label ?title . ";
				$body .= "BIND(str(?title) AS ?label) . ";
				$body .= "BIND(str(?catalog) AS ?id) . ";
				break;
				
			case "keywords":
				$body .= "?dataset a dcat:Dataset . ";
				$body .= "?dataset vivo:freetextKeyword ?id . ";
				$body .= "BIND(str(?id) AS ?label) . ";
				break;
				
			case "count":
				$body .= "?dataset a dcat:Dataset . ";
				break;
				
			case "datasets":
				$body .= "?dataset a dcat:Dataset . ";
				$body .= "?dataset dc:title ?l . ";
				$body .= "OPTIONAL { ?dataset dc:identifier ?id . } ";
				$body .= "OPTIONAL { ?dataset dct:issued ?issued_date . } ";
				$body .= "OPTIONAL { ?dataset dcat:landingPage ?lp . } ";
				$body .= "OPTIONAL { ?dataset dc:description ?description . } ";
				$body .= "BIND(str(?l) AS ?label) . ";
				$body .= "BIND(str(?issued_date) AS ?issued) . ";
				$body .= "BIND(str(?lp) AS ?landingPage) . ";
				break;
		}
				
		return $body;
	}
	
	public function getQueryConstraint($constraint_type, $constraint_value) {
		
		$body = "";
		switch($constraint_type) {
			case "authors":
				$body .= "{ ?authorship vivo:relates ?dataset . ?authorship vivo:relates <$constraint_value> . ?authorship a vivo:Authorship }";
				break;
			case "catalogs":
				$body .= "{ <$constraint_value> dcat:dataset ?dataset }";
				break;
			case "contributors":
				$body .= "{ ?dataset dct:contributor <$constraint_value> }";
				break;
			case "keywords":
				$body .= "{ ?dataset vivo:freetextKeyword \"$constraint_value\"^^xsd:string } UNION { ?dataset vivo:freetextKeyword \"$constraint_value\" }";
				break;
			default:
				break;
		}
		return $body;
	}
	
	private function addContextLinks(&$results, $type) {
		
		if ($type == 'authors' || $type == 'contributors' || $type == 'catalogs') {
			foreach ( $results as $i => $result ) {
				$results[$i]['context'] = $this->VIVO_URL_PREFIX . "?uri=" . urlencode($result['id']); 
			}
		}
	}
	
	public function getOutput(array $results, $type, array $constraints, $limit=0, $offset=0) {
				
		if($type == "datasets") {
			$count = $this->getSearchResultCount($constraints);						
			return $this->getSearchResultsOutput($results, $limit, $offset, $count);
		} else {		
			$this->addContextLinks($results, $type);
			return $this->getFacetOutput($results);
		}
	}
}