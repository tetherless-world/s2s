<?php

/**
* Send a GET request using cURL
* @param string $url to request
* @param array $get values to send
* @param array $options for cURL
* @param string $status status code of HTTP response
* @return string
*/
function curl_get($url, array $get = NULL, array $options = array(), &$status = NULL) {
	$defaults = array(
        CURLOPT_URL => $url. (strpos($url, '?') === FALSE ? '?' : ''). http_build_query($get),
        CURLOPT_HEADER => 0,
        CURLOPT_RETURNTRANSFER => TRUE,
        CURLOPT_TIMEOUT => 4
    );

    $ch = curl_init();
    curl_setopt_array($ch, ($options + $defaults));

    if( ! $result = curl_exec($ch))
    {
        trigger_error(curl_error($ch));
    }

    $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return $result;
}

/**
* Send a POST request using cURL
* @param string $url to request
* @param string $post to send
* @param array $options for cURL
* @param string $status status code of HTTP response
* @return string
*/
function curl_post($url, $post = NULL, array $options = array(), &$status = NULL)
{
    $defaults = array(
        CURLOPT_POST => 1,
        CURLOPT_HEADER => 0,
        CURLOPT_URL => $url,
        CURLOPT_FRESH_CONNECT => 1,
        CURLOPT_RETURNTRANSFER => 1,
        CURLOPT_FORBID_REUSE => 1,
        CURLOPT_POSTFIELDS => $post
    );

    $ch = curl_init();
    curl_setopt_array($ch, ($options + $defaults));

    if( ! $result = curl_exec($ch))
    {
        trigger_error(curl_error($ch));
    }

    $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return $result;
}

/**
 * Executes a basic SPARQL query
 * @param string $endpoint SPARQL endpoint to run the query against
 * @param string $payload SPARQL query string encoded as POST payload
 * @param array $options CURL options
 * @param string $status status code of HTTP response
 * @return array an array of associative arrays containing the bindings
 */
function execSelect($endpoint, $payload, $options, &$status = NULL) {

	$content = curl_post($endpoint, $payload, $options, $status);

	$xml = simplexml_load_string($content);
	$results = array();
	foreach ($xml->results->result as $result) {
		$arr = array();
		foreach($result->binding as $binding) {
			$name = $binding['name'];
			$arr["$name"] = (string) $binding->children();
		}
		array_push($results, $arr);
	}
	return $results;
}