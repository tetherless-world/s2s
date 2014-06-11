<?php

require_once("sparql.php");

///
/// Global Variables
///

$endpoint = "http://aquarius.tw.rpi.edu:8890/sparql";
$graph = "http://essi-lod.org/instances/";

///
/// Query Components
///

function getQueryHeader($type) {
	global $graph;
	$header = getPrefixes();
	$header .= 'SELECT DISTINCT ';
	switch ($type) {
		case "abstracts":
		    $header .= '?abstract ?title ?text';
			break;
		case "count":
			$header .= '(count(?abstract) AS ?count)';
			break;
		case "years":
			$header .= '?label ?id (count(?abstract) AS ?count)';
			break;
		case "sections":
                        $header .= '?label ?id (count(?abstract) AS ?count)';
                        break;
		default:
			$header .= '?label ?id ?context (count(?abstract) AS ?count)';
	}
	return $header . ' WHERE { GRAPH <' . $graph . '> { ';
}

function getQueryConstraintBody($search, $years, $meetings, $sections, $sessions, $authors, $keywords) {
	$body = '';
	if ($years) $body .= yearsConstraint($years);
	else if ($meetings) $body .= meetingsConstraint($meetings);
	if ($sections) $body .= sectionsConstraint($sections);
	if ($sessions) $body .= sessionsConstraint($sessions);
	if ($authors) $body .= authorsConstraint($authors);
	if ($keywords) $body .= keywordsConstraint($keywords);
	if ($search) $body .= searchTermsConstraint($search);
	return $body;
}

function getQuerySelectBody($type) {
	$body = '';
	switch ($type) {
		case 'years':
			$body .= ' ?abstract swc:relatedToEvent ?session . ' .
			        '?session swc:isSubEventOf ?section . ' .
				'?section swc:isSubEventOf ?meeting . ' .
				'?meeting swrc:year ?id . ';
			break;
		case 'meetings':
			$body .= ' ?abstract swc:relatedToEvent ?session . ' .
			        '?session swc:isSubEventOf ?section . ' .
				'?section swc:isSubEventOf ?context . ' .
				'?context swrc:eventTitle ?label . ' .
				'?context dcterms:identifier ?id . ';
			break;
		case 'sections':
			$body .= ' ?abstract swc:relatedToEvent ?session . ' .
			        '?session swc:isSubEventOf ?context . ' .
				'?context swrc:eventTitle ?label . ' .
				'?context dcterms:identifier ?id . ';
			break;
		case 'sessions':
			$body .= ' ?abstract swc:relatedToEvent ?context . ' .
				'?context swrc:eventTitle ?label . ' .
				'?context dcterms:identifier ?id . ';
			break;
		case 'authors':
			$body .= ' ?abstract tw:hasAgentWithRole ?role . ' .
			        '?id tw:hasRole ?role . ' .
				'?context tw:hasRole ?role . ' .
				'?id foaf:name ?label . ';
			break;
		case 'keywords':
			$body .= ' ?abstract swc:hasTopic ?context . ' .
			      	'?context dcterms:identifier ?id . ' .
				'?context dcterms:subject ?label . ';
			break;
		case 'abstracts':
			$body .= ' ?abstract dcterms:title ?title . ' .
			        '?abstract swrc:abstract ?text . ';
			break;
		case 'count':
			$body .= ' ?abstract a essi:Abstract . ';
	}
	return $body;
}

function getQueryFooter($type = null, $limit = null, $offset = 0, $sort = null) {
	$footer = '} }';
	if ($type && $type == 'abstracts') {
		if ($limit) $footer .= " LIMIT $limit OFFSET $offset";
		if ($sort) {
			$sortArray = explode(',',$sort);
			$sortLabels = array();
			for ($i = 0; $i < count($sortArray); $i++) {
				if ($sortArray[$i] == '1') {
					$sortLabels[] = "?dcname";
				} else if ($sortArray[$i] == '2') {
					$sortLabels[] = "?pname";
				} else if ($sortArray[$i] == '3') {
					$sortLabels[] = "?depname";
				}
			}
			$footer .= " ORDER BY " . implode(' ', $sortLabels);
		}
	} else if ($type == 'years' || $type == 'sections') {
	       	$footer .= " GROUP BY ?label ?id";
	} else if ($type != null) {
	        $footer .= " GROUP BY ?label ?id ?context";
	}
	return $footer;
}

function yearsConstraint($years) {
	$arr = array();
	for ($i = 0; $i < count($years); ++$i)
		array_push($arr,'{ ?meeting swrc:year "' . $years[$i] . '"^^xsd:int . }');
	return implode(' UNION ',$arr) . '?section swc:isSubEventOf ?meeting . ' .
	       '?session swc:isSubEventOf ?section . ' .
	       ' ?abstract swc:relatedToEvent ?session . ';
}

function meetingsConstraint($meetings) {
	$arr = array();
	for ($i = 0; $i < count($meetings); ++$i) {
		array_push($arr,'{ ?meeting dcterms:identifier "' . $meetings[$i] . '"^^xsd:string . }');
	}
        return implode(' UNION ',$arr) . ' ?section swc:isSubEventOf ?meeting . ' .
               '?session swc:isSubEventOf ?section . ' .
	       '?abstract swc:relatedToEvent ?session . ';
}

function sectionsConstraint($sections) {
        $arr = array();
        for ($i = 0; $i < count($sections); ++$i) {
                array_push($arr,'{ ?section dcterms:identifier "' . $sections[$i] . '"^^xsd:string . }');
        }
        return implode(' UNION ',$arr) . '?session swc:isSubEventOf ?section . ' .
               '?abstract swc:relatedToEvent ?session . ';
}

function sessionsConstraint($sessions) {
        $arr = array();
        for ($i = 0; $i < count($sessions); ++$i) {
                array_push($arr,'{ ?session dcterms:identifier "' . $sessions[$i] . '"^^xsd:string . }');
        }
        return implode(' UNION ',$arr) . '?abstract swc:relatedToEvent ?session . ';
}

function authorsConstraint($authors) {
	$arr = array();
	for ($i = 0; $i < count($authors); ++$i) {
		array_push($arr,'{ ?person tw:hasRole ?role . ?person foaf:name ?name . ?name bif:contains "' . $authors[$i] . '"^^xsd:string . }');
	}
	return implode(' UNION ',$arr) . ' ?abstract tw:hasAgentWithRole ?role . ';
}

function keywordsConstraint($keywords) {
	$arr = array();
	for ($i = 0; $i < count($keywords); ++$i) {
		array_push($arr,'{ ?keyword dcterms:identifier "' . $keywords[$i] . '"^^xsd:string . }');
	}
	return implode(' UNION ',$arr) . ' ?abstract swc:hasTopic ?keyword . ';
}

function searchTermsConstraint($search) {
	return ' { ?abstract swrc:abstract ?text . ?text bif:contains  "' . $search . '" . } UNION { ?abstract dcterms:title ?title . ?title bif:contains "' . $search . '" } ';
}

///
/// Services
///

function getResponse($search, $years, $meetings, $sections, $sessions, $authors, $keywords, $type, $limit = null, $offset = 0, $sort = null) {
	global $endpoint;
	$query = buildQuery($search, $years, $meetings, $sections, $sessions, $authors, $keywords, $type, $limit, $offset, $sort);
	$results = sparqlSelect($query,$endpoint);
	if ($type == "abstracts")
		$count = getAbstractCount($search, $years, $meetings, $sections, $sessions, $authors, $keywords);
	getOutput($results,$type,$limit,$offset,@$count);
}

function getAbstractCount($search, $years, $meetings, $sections, $sessions, $authors, $keywords) {
	global $endpoint;
	$query = getQueryHeader('count');
	$query .= getQueryConstraintBody($search, $years, $meetings, $sections, $sessions, $authors, $keywords);
	$query .= getQuerySelectBody('count');
	$query .= getQueryFooter();
	$results = sparqlToArray(sparqlSelect($query,$endpoint));
	$count = 0;
	foreach ($results as $i => $info)
	{
		$count = 0+$info['count'];
	}
	return $count;
}

function buildQuery($search, $years, $meetings, $sections, $sessions, $authors, $keywords, $type, $limit = null, $offset = 0, $sort = null) {
	$query = getQueryHeader($type);
	$query .= getQueryConstraintBody($search, $years, $meetings, $sections, $sessions, $authors, $keywords);
	$query .= getQuerySelectBody($type);
	$query .= getQueryFooter($type, $limit, $offset, $sort);
	return $query;
}

function getOutput($results,$type,$limit = 0,$offset = 0,$count = 0) {
	$results = sparqlToArray($results);
	if ($type == 'abstracts') {
		header("Access-Control-Allow-Origin: *");
		header("Content-Type: application/json");
		$out = array( "entries" => $results, "limit" => $limit, "offset" => $offset, "count" => $count );
		echo json_encode($out);
	} else {
		header("Access-Control-Allow-Origin: *");
		header("Content-Type: application/json");
		echo json_encode($results);
	}
}

//getResponse("ontology", null,null,null,null,array( "Fox, P" ),null,"keywords",10,10);



