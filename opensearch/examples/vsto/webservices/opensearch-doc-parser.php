<?php

// A s2s OpenSearch document parsing service.
$url = null;
$q = null;

if (@$_GET["q"] && @$_GET["q"] != "")
	$query = $_GET["q"];
if (@$_GET["request"] && @$_GET["request"] != "")
	$url = $_GET["request"];

if ($url)
	parseOpenSearchDocument($url, $query);
else header("HTTP/1.1 400 Bad Request");

function parseOpenSearchDocument($url, $q=null)
{
	$file = file_get_contents($url);
	if (! $file) {
		header("HTTP/1.0 404 Not Found");
		die("Cound not find the document!");
	}
	$xml = new SimpleXmlElement($file);
	$namespaces = $xml->getDocNameSpaces();
	$queries = array();
	$queryURIs = array();
	foreach ($xml->Url as $entry) {
		// Construct an array for each query to hold the uri, url, and the constraint parameters.
		$query = array(
			"query" => array(
				"label" => null,
				"uri" => null,
				"url" => null,
				"parameters" => array()
			)
		);
		// Parse the query label and uri.
		$rel = $entry->attributes()->rel;
		$temp1 = explode(":", $rel);
		$query["query"]["label"] = $temp1[1];
		$query["query"]["uri"] = $namespaces[$temp1[0]] . $temp1[1];
		array_push($queryURIs, $query["query"]["uri"]);
		// Parse the query url and constraint parameters
		$template = $entry->attributes()->template;
		$temp2 = explode("&", $template);
		$query["query"]["url"] = $temp2[0];
		// An array to hold constraint parameters
		$params = array();
		for ($i=1; $i < count($temp2); $i++) {
			$temp3 = explode("{", $temp2[$i]);
			$paramAlias = trim($temp3[0], "=");
			if (strpbrk($temp3[1], ":") != FALSE) {
				$temp4 = explode(":", trim($temp3[1], "}"));
				$param = $namespaces[$temp4[0]] . trim($temp4[1], "?");
			}
			else {
				$param = trim($temp3[1], "?}");
			}
			$params[$paramAlias] = $param;
		}
		$query["query"]["parameters"] = $params;
		array_push($queries, $query);	
	}
	if (in_array($q, $queryURIs) && $q != null) {
		foreach ($queries as $query) {
			if ($query["query"]["uri"] === $q) 
				writeOutput($query);
		}
	}
	else {
		writeOutput($queries);
	}
}

function writeOutput($output)
{
	header("Access-Control-Allow-Origin: *");
	header("Content-Type: application/json");
	echo json_encode($output);
}

//parseOpenSearchDocument("http://aquarius.tw.rpi.edu/s2s/VSTO/2.0/opensearch.xml", "http://escience.rpi.edu/ontology/sesf/s2s-vsto/1/0/InstrumentsQuery");

?>
