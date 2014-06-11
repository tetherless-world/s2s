<?php

///
/// global variables
///

//namespaces
$rdfs = "http://www.w3.org/2000/01/rdf-schema#";
$foaf = "http://xmlns.com/foaf/0.1/";
$time = "http://www.w3.org/2006/time#";
$skos = "http://www.w3.org/2004/02/skos/core#";
$xsd = "http://www.w3.org/2001/XMLSchema#";

//endpoint info
$endpoint = "http://example.com";
$param = "query";

///
/// Query Utilities
///

/**
 * Builds a basic SPARQL query
 * @param string $query SPARQL query string
 * @return array an array of associative arrays containing the bindings
 */
function sparqlSelect($query) {
	global $endpoint;
	$curl = curl_init();
	curl_setopt($curl, CURLOPT_URL, $endpoint);
	curl_setopt($curl, CURLOPT_POST, true);
	curl_setopt($curl, CURLOPT_POSTFIELDS, getQueryData($query));
	curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
	$content = curl_exec($curl);
	curl_close($curl);
	$json = json_decode($content, true);
	$results = array();
	foreach ($json['results']['bindings'] as $i => $result) 
	{
		$arr = array();
		foreach ($result as $label => $binding) 
		{
			$arr[$label] = $binding["value"];
		}
		array_push($results, $arr);
	}
	return $results;
}

/**
 * Builds a basic SPARQL query
 * @param string $query SPARQL query string
 * @param string $suffix other options to append to request
 * @return string a URL to make a SPARQL query request
 */
function getQueryData($query, $suffix = '') {
	global $param;
	return $param . '=' . urlencode($query) . '&output=json' . urlencode($suffix);
}


///
/// Build and run query and write results
///

/**
 * This is the main function called to build the sparql query, run the query, and write the results
 */
function main($type, $limit, $offset, $sort) {
  	$query .= getQueryHeader($type);
	$query .= getQueryConstraintBody();
	$query .= getQuerySelectBody($type);	
	$query .= getQueryFooter($type, $limit, $offset);	
	$results = sparqlSelect($query);
	writeOutput($results, $type);
}

/**
 * Based on the type of request, write the output (whether JSON or HTML...) 
 */ 
function writeOutput($results, $type) {
	//add stuff here!!!
}

function getPrefixes() {
	//TODO: add prefixes as needed
	global $foaf, $rdfs, $skos, $time, $xsd;
	$arr = array(
		'foaf' => $foaf, 'rdfs' => $rdfs, 'skos' => $skos,
		'time' => $time, 'xsd' => $xsd
	);
	$output = '';
	foreach ($arr as $prefix => $uri) 
		$output .= "PREFIX $prefix: <$uri>" . "\n";
	return $output;
}

function getQueryHeader($type) {
	$header .= getPrefixes();
	switch ($type) 
	{
		//TODO: add header where different
		default:
			$header .= 'SELECT DISTINCT ?label ?id count(?dataset) AS ?count';
			break;
	}
	return $header . ' WHERE { ';
}

/**
 * Builds (and orders) all the necessary facet constraints
 */
function getQueryConstraintBody() {
	$body = '';
	//TODO: append constraint for each facet
	return $body;
} 

function getQuerySelectBody($type) {
	$body = '';
	switch($type) 
	{
		//TODO: write selection clause for each type
	}
	return $body;
}

/** 
 * Append footer information to the query (GROUP BY, ORDER BY, LIMIT, etc.)
 */
function getQueryFooter($type = null, $limit = null, $offset = 0, $sort = null) {
	$footer = '} }';
	if ($limit)
	   	$footer .= " LIMIT $limit OFFSET $offset"
	if ($type != null) 
		$footer .= ' GROUP BY ?label ?id';		
	return $footer;
}


///
/// Constraint functions
///

//TODO: write constraint functions

///
/// Test
///


//main(null /* use freetext string */, 
//     null /* use start date (in ISO 8601) */,
//     null /* use end date (in ISO 8601) */,
//     null /* use array of author URIs */,
//     null /* use array of event URIs */,
//     null /* use array of project URIs */,
//     null /* use array of theme URIs */,
//     'publications' /* specify type of request */,
//     null /* set integer limit */,
//     null /* set integer offset */, 
//     null /* set sorting request */);

?>
