<?php

/**
 * global variables
 */

//open log file

//namespaces
$rdfs = "http://www.w3.org/2000/01/rdf-schema#";
$foaf = "http://xmlns.com/foaf/0.1/";
$time = "http://www.w3.org/2006/time#";
$skos = "http://www.w3.org/2004/02/skos/core#";
$xsd = "http://www.w3.org/2001/XMLSchema#";
$graphDir = "http://escience.rpi.edu/ontology/vsto/2/0/";
$vsto = $graphDir . "vsto.owl#";
$cedar = $graphDir . "cedar.owl#";
$vstotime = $graphDir . "time.rdf#";

//endpoint info
$endpoint = "http://escience.rpi.edu:8890/sparql";
$param = "query";
$nested = 50;
$inferencing = false;
$logfile = "log.txt";

/**
 * Builds a basic SPARQL query
 * @param string $query SPARQL query string
 * @return array an array of associative arrays containing the bindings
 */
function sparqlSelect($query) {
	global $endpoint;
	echo $query,"\n";
	$curl = curl_init();
	curl_setopt($curl, CURLOPT_URL, $endpoint);
	curl_setopt($curl, CURLOPT_POST, true);
	curl_setopt($curl, CURLOPT_POSTFIELDS, getQueryData($query));
	curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
	$content = curl_exec($curl);
	curl_close($curl);
	
	$xml = simplexml_load_string($content);
	$results = array();
	foreach ($xml->results->result as $result) 
	{
		$arr = array();
		foreach ($result->binding as $binding) 
		{
			$name = $binding['name'];
			$arr["$name"] = (string)$binding->children();
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
	return $param . '=' . urlencode($query) . urlencode($suffix);
}

function getResponse($type, $box, $start, $ndays, $year, $month, $instruments, $instrumentClasses, $parameters, $parameterClasses, $limit, $offset) {
	$query = getInferencing($type, $instrumentClasses, $parameterClasses); 
  	$query .= getQueryHeader($type);
	$query .= getQueryConstraintBody($box, $start, $ndays, $year, $month, $instruments, $instrumentClasses, $parameters, $parameterClasses);
	$query .= getQuerySelectBody($type);	
	$query .= getQueryFooter($type, $limit, $offset);	
//	echo $query, "\n";
	$results = sparqlSelect($query);
	writeOutput($results, $type);
}

function addParameters(&$results)
{
	//write SPARQL to get parameters
	//execute SPARQL
	//add parameters to results
}

function writeOutput($results, $type) {
	global $inferencing;
//	if ($inferencing) $results = getLabels($type, $results);
/*	if ($type == 'datasets')
	{
		addParameters($results);
	}
	if ($type == 'dates') {
		$arr = array();
		for($i = 0; $i < count($results); ++$i) {
			$y = substr($results[$i]['year'],0,4);
			$m = substr($results[$i]['month'],2);
			$d = substr($results[$i]['day'],3);
			array_push($arr,"$y-$m-$d");
		}
		$results = $arr;
	}
*/
	header("Access-Control-Allow-Origin: *");
	header("Content-Type: application/json");
	echo json_encode($results);
}

function getInferencing($type, $instrumentClasses, $parameterClasses) {
	global $inferencing;
	if ($type == 'instrumentClasses' || $type == 'parameterClasses' || $instrumentClasses != null || $parameterClasses != null) {
		$inferencing = true;
		return 'DEFINE input:inference "vsto-rdfs" ';
	}
	return '';
}

function getPrefixes() {
	global $foaf, $rdfs, $skos, $time, $xsd, $vsto, $cedar, $vstotime, $graphDir;
	$arr = array(
		'foaf' => $foaf, 'rdfs' => $rdfs, 'skos' => $skos,
		'time' => $time, 'xsd' => $xsd, 'vsto' => $vsto,
		'vstotime' => $vstotime, 'cedar' => $cedar, 'gdir' => $graphDir
	);
	$output = '';
	foreach ($arr as $prefix => $uri) 
		$output .= "PREFIX $prefix: <$uri> ";
	return $output;
}

function getQueryHeader($type) {
	global $inferencing;
	$header = '';
	$header .= getPrefixes();
	if ($type == 'dates') $header .= 'SELECT DISTINCT';
        else $header .= 'SELECT DISTINCT ';
	switch ($type) 
	{
		case 'datasets':
			$header .= '?dfilelabel ?instrument ?opmode';
			break;
		case 'instrumentClasses':
			$header .= '?id count(?dataset) AS ?count ?parent ?label';
			break;
		case 'parameterClasses':
			$header .= '?id count(?dataset) AS ?count ?parent ?label';
			break;
		case 'dates':
			$header .= '?date'; 
			break;
		case 'years':
			$header .= '?year'; 
			break;
		case 'months':
			$header .= '?month'; 
			break;
		case 'days':
			$header .= '?day'; 
			break;
		case 'instruments':
			$header .= '?label ?id count(?dataset) AS ?count';
			break;
		case 'parameters':
			$header .= '?label ?id count(?dataset) AS ?count';
			break;
	}
	return $header . ' WHERE { GRAPH gdir:cedar.owl { ';
}

function getQueryConstraintBody($box, $start, $ndays, $year, $month, $instruments, $instrumentClasses, $parameters, $parameterClasses) {
	$body = '';
	if ($instruments) 
		$body .= instrumentConstraint($instruments);
	if ($instrumentClasses)
		$body .= instrumentClassConstraint($instrumentClasses);
	if ($parameters) 	
		$body .= parameterConstraint($parameters);
	if ($parameterClasses)
		$body .= parameterClassConstraint($parameterClasses);
	if ($start && $ndays)
		$body .= intervalConstraint($start, $ndays);
	else if ($year || $month)
		$body .= dateConstraint($year, $month);
	return $body;
} 

function getQuerySelectBody($type) {
	global $inferencing;
	$body = '';
	switch($type) 
	{
		case 'datasets':
			$body .= '?dataset a vsto:Dataset . ' .
					 '?dataset cedar:isFromDataFile ?datafile . ' .
					 '?dataset vsto:isFromInstrument ?instrument . ' .
					 '?dataset vsto:isFromInstrumentOperatingMode ?opmode . ' .
					 '?datafile rdfs:label ?dfilelabel . ';
			break;
		case 'instruments':
			$body .= '?dataset vsto:isFromInstrument ?instrument . ' .
				 	 '?instrument vsto:hasDescription ?label . ' .
				 	 '?instrument vsto:hasIdentifier ?id . ';
			break;
		case 'instrumentClasses':
			$body .= '?dataset vsto:isFromInstrument ?instrument . ' .
					 '?instrument a vsto:Instrument . ' . 
					 '?instrument a ?id . ' .
					 'GRAPH gdir:vsto.owl { ' .
					 'OPTIONAL { ?id rdfs:subClassOf ?parent . } ' . 
					 '?id rdfs:label ?label . } ';
			break;
		case 'parameters':
			$body .= '?dataset vsto:hasContainedParameter ?parameter . ' . 
					 '?parameter vsto:hasName ?label . ' .
					 '?parameter vsto:hasIdentifier ?id . ';
			break;
		case 'parameterClasses':
			$body .= '?dataset vsto:hasContainedParameter ?parameter . ' .
					 'GRAPH gdir:vsto.owl {?id rdfs:subClassOf vsto:Parameter OPTION(TRANSITIVE) . } ' .
					 '?parameter a vsto:Parameter . ' .
					 '?parameter a ?id . GRAPH gdir:vsto.owl { ' .
					 '?id rdfs:subClassOf ?parent . ' .
					 '?id rdfs:label ?label . } ';
			break;
    	case 'dates':
			$body .= '?dataset vsto:hasDateTimeCoverage ?interval . GRAPH gdir:time.rdf { ' .
					 '?interval vsto:hasDate ?date . }' ; 
			break;
		case 'years':
			$body .= '?dataset vsto:hasDateTimeCoverage ?interval . GRAPH gdir:time.rdf { ' .
					 '?interval time:hasDateTimeDescription ?desc . ' .
					 '?desc time:year ?year . } ';
			break;   
		case 'months':
			$body .= '?dataset vsto:hasDateTimeCoverage ?interval . GRAPH gdir:time.rdf { ' .
					 '?interval time:hasDateTimeDescription ?desc . ' .
					 '?desc time:month ?month . } ';
			break;
		case 'days':
			$body .= '?dataset vsto:hasDateTimeCoverage ?interval . GRAPH gdir:time.rdf { ' .
					 '?interval time:hasDateTimeDescription ?desc . ' .
					 '?desc time:day ?day . } ';
			break;
	}
	return $body;
}
 
function getQueryFooter($type = null, $limit = null, $offset = 0, $group = null) {
	global $inferencing;
	$footer = '} }';	
	if ($type && $type == 'datasets')
	{
		if ($limit) 
			$footer .= " LIMIT $limit OFFSET $offset";
		if ($group) 
			$footer .= " GROUP BY $group";
	} 
	else if ($type == 'instrumentClasses' || $type == 'parameterClasses')
		$footer .= 'GROUP BY ?id ?parent ?label';
	else if ($type != 'dates' && $type != 'years' && $type != 'months' && $type != 'days') 
		$footer .= ' GROUP BY ?label ?id';		
	return $footer;
}

function instrumentConstraint($insts) {
	$arr = array();
	for ($i = 0; $i < count($insts); ++$i) 
		array_push($arr, '{ ?instrument vsto:hasIdentifier "' . $insts[$i] . '"^^xsd:string . }');
	return implode(' UNION ', $arr) . ' ?dataset vsto:isFromInstrument ?instrument . ';
}

function instrumentClassConstraint($classes) {
	$arr = array();
	for ($i = 0; $i < count($classes); ++$i) 
		array_push($arr, '{ ?instrument a <'.$classes[$i].'>  . }');
	return implode(' UNION ', $arr) . ' ?dataset vsto:isFromInstrument ?instrument . ';
}

function parameterConstraint($params) {
	$arr = array();
	for ($i = 0; $i < count($params); ++$i)
		array_push($arr, '{ ?parameter vsto:hasIdentifier "' . $params[$i] . '"^^xsd:string . }');
	return implode(' UNION ', $arr) . ' ?dataset vsto:hasContainedParameter ?parameter . ';
}

function parameterClassConstraint($classes) {
	$arr = array();
	for ($i = 0; $i < count($classes); ++$i)
		array_push($arr, '{ ?parameter a <'.$classes[$i].'> . }');
	return implode(' UNION ', $arr) . ' ?dataset vsto:hasContainedParameter ?parameter . ';
}

function intervalConstraint($start, $ndays) {
	$arr = array();
	$arr = getDateTimeStrings($start, $ndays);
	return ' GRAPH gdir:time.rdf { ' .
		'?dti a time:DateTimeInterval . ' .
		'FILTER (str(?dti) >= vstotime:day_' . $arr[0] . ' && str(?dti) <= vstotime:day_' . $arr[$ndays-1] . ') } . ' .
		'?dataset vsto:hasDateTimeCoverage ?dti . ';
}

function dateConstraint($year, $month) {
	$output = '?dataset vsto:hasDateTimeCoverage ?interval . GRAPH gdir:time.rdf { ?interval time:hasDateTimeDescription ?desc . ';
	if ($year) $output .= '?desc time:year "' . $year . '"^^xsd:gYear . ';
	if ($month) $output .= '?desc time:month "--' . $month . '"^^xsd:gMonth . ';
	$output .= '} ';
	return $output;
}

function getDateTimeStrings($start, $days) {
	$output = array();
	$d = substr($start, 0, 10);
	$arr = explode('-', $d);
	$i = 0;
	$uts = mktime(0, 0, 0, $arr[1], $arr[2], $arr[0]);
	while ($i++ < $days) 
	{
		$output[] = date('Ymd', $uts);
		$uts = $uts + 86400;
	}
	return $output;
}

function getLabels($type, $results) {
	global $nested;
	$count = 0;
	
	while ($count < count($results)) {
		$j = 0; $arr = array(); $data = array();
		
		/* Build query */
		$query = getPrefixes();
		if ($type == 'instrumentClasses' || $type == 'parameterClasses')
			$query .= 'SELECT DISTINCT ?id ?label WHERE { GRAPH gdir:vsto.owl {';
		else $query .= 'SELECT DISTINCT ?id ?code ?label WHERE { GRAPH gdir:cedar.owl { ';
		for($i = $count; $i < count($results) && $j < $nested; ++$i, ++$count) {
			if (!isset($results[$i]['label'])) {
				if ($type == 'instrumentClasses' || $type == 'parameterClasses')
					array_push($arr, '{ ?id rdfs:label ?label . FILTER ( ?id = <'.$results[$i]['id'].'> ) }');
				else {
					$str = '{ ?id vsto:hasIdentifier ?code . OPTIONAL { ?id vsto:hasDescription ?label . } FILTER ( ?id = <';
					if ($type == 'instruments') $str .= $results[$i]['instrument'];
					else $str .= $results[$i]['parameter'];
					$str .= '> ) }';
					array_push($arr, $str);
				}
				$j++;
			}
		}
		
		$query .= implode(' UNION ', $arr);
		$query .= ' } }';
				
		/* Run query, create data structure */
		$l = sparqlSelect($query);
		for($i = 0; $i < count($l); ++$i) {
			$data[$l[$i]['id']]['label'] = $l[$i]['label'];
			if ($type != 'instrumentClasses' && $type != 'parameterClasses')
				$data[$l[$i]['id']]['id'] = $l[$i]['code'];
		}
		
		/* Use data structure to create results for classes */
		if ($type == 'instrumentClasses' || $type == 'parameterClasses') {
			for($i = 0; $i < count($results); ++$i) {
				$id = $results[$i]['id'];
				if (isset($data[$id])) {
					$results[$i]['label'] = $data[$id]['label'];
				}
			}
		} 
		
		/* Use data structure to create results for instruments/parameters */
		else {
			for($i = 0; $i < count($results); ++$i) {
				$id = '';
				if (isset($results[$i]['instrument']))
					$id = $results[$i]['instrument'];
				else if (isset($results[$i]['parameter'])) 
					$id = $results[$i]['parameter'];
				if (isset($data[$id])) {
					if (isset($results[$i]['instrument'])) 
						unset($results[$i]['instrument']);
					else if (isset($results[$i]['parameter']))
						unset($results[$i]['parameter']);
					$results[$i]['label'] = $data[$id]['label'];
					$results[$i]['id'] = $data[$id]['id'];
				}
			}
		}
	}
	
	return $results;
}

function writeHierarchyOutput($classes,$instances,$classType,$instanceType)
{
	$classLabels = getLabels($classType, $classes);
	$instanceLabels = getLabels($instanceType,$instances);
	$results = array("classes" => $classLabels, "instances" => $instanceLabels);
	header("Access-Control-Allow-Origin: *");
	header("Content-Type: application/json");
	echo json_encode($results);
}

getResponse('dates',null,null,null,null,null,null,array("http://escience.rpi.edu/ontology/vsto/2/0/vsto.owl#FabryPerot"),null,null,null,null);

?>
