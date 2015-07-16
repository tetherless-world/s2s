<?php

include_once "utils.php";

abstract class S2SConfig
{
	/**
	* Return SPARQL endpoint URL
	* @return string SPARQL endpoint URL
	*/
	abstract protected function getEndpoint();

	/**
	* Return array of prefix, namespace key-value pairs
	* @return array of prefix, namespace key-value pairs
	*/
	abstract protected function getNamespaces();
		
	/**
	* Execute SPARQL select query
	* @param string $query SPARQL query to execute
	* @return array an array of associative arrays containing the bindings of the query results
	*/
	abstract protected function sparqlSelect($query);
		
	/**
	* Return representation (HTML or JSON) of response to send to client
	* @param array $results array of associative arrays with bindings from query execution
	* @param string $type search type (e.g. 'datasets', 'authors', 'keywords')
	* @param array $constraints array of arrays with search constraints
	* @param int $limit size of result set
	* @param int $offset offset into result set
	* @return string representation of response to client
	*/
	abstract protected function getOutput(array $results, $type, array $constraints, $limit=0, $offset=0);
	
	/**
	* Create HTML of search result
	* @param array $result query result to be processed into HTML
	* @return string HTML div of search result entry
	*/
	abstract protected function getSearchResultOutput(array $result);
		
	/**
	* Return count of total search results for specified constraints
	* @param array $constraints array of arrays with search constraints
	* @result int search result count
	*/
	abstract protected function getSearchResultCount(array $constraints);
			
	/**
	* Return SPARQL query header component
	* @param string $type search type (e.g. 'datasets', 'authors', 'keywords')
	* @return string query header component (e.g. 'SELECT ?id ?label')
	*/
	abstract protected function getQueryHeader($type);
	
	/**
	* Return SPARQL query footer component
	* @param string $type search type (e.g. 'datasets', 'authors', 'keywords')
	* @param int $limit size of result set
	* @param int $offset offset into result set
	* @param string $sort query result sort parameter
	* @return string query footer component (e.g. 'GROUP BY ?label ?id')
	*/
	abstract protected function getQueryFooter($type, $limit=null, $offset=0, $sort=null);
	
	/**
	* Return SPARQL query WHERE clause minus constraint clauses for specified search type
	* @param string $type search type (e.g. 'datasets', 'authors', 'keywords')
	* @return string WHERE clause component minus constraint clauses (e.g. '?dataset a dcat:Dataset . ')
	*/
	abstract protected function getQueryBody($type);
	
	/**
	* Return constraint clause to be included in SPARQL query
	* @param string $constraint_type constraint type (e.g. 'keywords')
	* @param string $constraint_value constraint value (e.g. 'Toxic')
	* @return string constraint clause to be included in SPARQL query
	*/	
	abstract protected function getQueryConstraint($constraint_type, $constraint_value);
		
	/**
	* Returns string prefix section of SPARQL query, depends on subclass implementation of getNamespaces()
	* @return string prefix section of SPARQL query
	*/
	protected function getPrefixes() {
	
		$namespaces = $this->getNamespaces();
		if( !$namespaces || empty( $namespaces ) ) {
			trigger_error("No namespaces defined");
		}
	
		$output = "";
		foreach ($namespaces as $prefix => $uri) {
			$output .= "PREFIX $prefix: <$uri> ";
		}
		return $output;
	}

	private $config;
	private $config_file = "opensearch.ini";

	/**
	 * @return array configuration
	 */
	private function getConfig()
	{
		if (is_null($this->config) && file_exists($this->config_file)) {
			$this->config = parse_ini_file($this->config_file, true);
		}
		return $this->config;
	}

	/**
	 * @return bool use caching
	 */
	protected function useCaching() {
		$config = $this->getConfig();
		return (!is_null($config) && $config['caching']['use_caching'] == 1);
	}

	/**
	 * @param $key string cache key
	 * @return bool is entry in cache for specified key
	 */
	protected function exists_in_cache($key) {
		return $this->useCaching() and apc_exists($key);
	}

	/**
	 * @param $key string cache key
	 * @return mixed get value from cache for specified key
	 */
	protected function fetch_from_cache($key) {
		return ($this->useCaching() ? apc_fetch($key) : null);
	}

	/**
	 * @param $key string cache key
	 * @param $value string value to cache
	 */
	protected function store_in_cache($key, $value) {
		if($this->useCaching()) {
			$config = $this->getConfig();
			$ttl = (isset($search_array['caching']['ttl']) ? $config['caching']['ttl'] : 0);
			apc_store($key, $value, $ttl);
		}
	}

	/**
	* Return constraints component of SPARQL query
	* @param array $constraints array of arrays with search constraints
	* @return string constraints component of SPARQL query
	*/
	public function getQueryConstraints(array $constraints) {

        $body = "";
		foreach($constraints as $constraint_type => $constraint_values) {
            $arr = array();
            foreach($constraint_values as $i => $constraint_value) {
				$constraint_clause = $this->getQueryConstraint($constraint_type, $constraint_value);
				array_push($arr, $constraint_clause);
			}
            $body .= implode('UNION', $arr) . ' ';
		}
		return $body;
	}
	
	/**
	* Return SPARQL select query to execute
	* @param string $type search type (e.g. 'datasets', 'authors', 'keywords')
	* @param array $constraints array of arrays with search constraints
	* @param int $limit size of result set
	* @param int $offset offset into result set
	* @param string $sort query result sort parameter
	* @return string SPARQL query
	*/
	public function getSelectQuery($type, array $constraints, $limit=null, $offset=0, $sort=null) {
				
		$query = "";
		$query .= $this->getPrefixes();
		$query .= "SELECT DISTINCT ";
		$query .= $this->getQueryHeader($type);
		$query .= " WHERE { ";
		$query .= $this->getQueryBody($type);
		$query .= $this->getQueryConstraints($constraints);
		$query .= " } ";
		$query .= $this->getQueryFooter($type, $limit, $offset, $sort);
				
		return $query;
	}
	
	/**
	* Return HTML representation of search results to display
	* @param array $results array of associative arrays with bindings from query execution
	* @param int $limit size of result set
	* @param int $offset offset into result set
	* @param int $count total count of result set
	* @return string HTML encoding of search results to display
	*/
	public function getSearchResultsOutput(array $results, $limit=0, $offset=0, $count=0) {
				
		header("Access-Control-Allow-Origin: *");
		header("Content-Type: text/html");
				
		if($this->setCacheControlHeader()) {
			header($this->getSearchResultCacheControlHeader());
		}
				
		$html = "";
		if ($count > 0) {
			$html .= "<div>";
			$html .= "<input type='hidden' name='startIndex' value='$offset'/>";
			$html .= "<input type='hidden' name='itemsPerPage' value='$limit'/>";
			$html .= "<input type='hidden' name='totalResults' value='$count'/>";
			$html .= "</div>";
		}
			
		$html .= "<div class='result-list'>";
		foreach($results as $i => $result) {

			$cache_key = "search_result:".implode($result);
			$rs = ($this->exists_in_cache($cache_key)
				? $this->fetch_from_cache($cache_key)
				: $this->getSearchResultOutput($result));

			$html .= $rs;

			if($this->useCaching() and !$this->exists_in_cache($cache_key)) {
				$this->store_in_cache($cache_key, $rs);
			}
		}
		$html .= "</div>";
		return $html;
	}
	
	/**
	* Return JSON representation of facet content
	* @param array $results array of associative arrays with bindings from query execution
	* @return string JSON representation of facet content
	*/
	public function getFacetOutput(array $results) {

		header("Access-Control-Allow-Origin: *");
		header("Content-Type: application/json");
		
		if($this->setCacheControlHeader()) {
			header($this->getFacetCacheControlHeader());
		}		
		
		return json_encode($results);
	}
	
	/**
	* Construct and execute SPARQL query and return response to send to client
	* @param string $type search type (e.g. 'datasets', 'authors', 'keywords')
	* @param array $constraints array of arrays with search constraints
	* @param int $limit size of result set
	* @param int $offset offset into result set
	* @param string $sort query result sort parameter
	* @return string representation of response to client
	*/
	public function getResponse($type, array $constraints, $limit=null, $offset=0, $sort=null)
	{
		$key = implode(func_get_args());
		if($this->exists_in_cache($key)) {
			return $this->fetch_from_cache($key);
		}

		$query = $this->getSelectQuery($type, $constraints, $limit, $offset, $sort);
		$results = $this->sparqlSelect($query);
		$response = $this->getOutput($results, $type, $constraints, $limit, $offset);

		if ($this->useCaching() and !$this->exists_in_cache($key)) {
			$this->store_in_cache($key, $response);
		}

		return $response;
	}
	
	/**
	* Return if cache control header should be set
	* @return boolean if cache control header should be set
	*/
	public function setCacheControlHeader() {
		return True;
	}
	
	/**
	* Return cache control header value of search result response
	* @return string cache control header value of search result response
	*/
	public function getSearchResultCacheControlHeader() {
		return "Cache-Control: s-maxage=900, public";
	}
	
	/**
	* Return cache control header value for facet response
	* @return string cache control header value for facet response
	*/
	public function getFacetCacheControlHeader() {
		return "Cache-Control: s-maxage=900, public";
	}
}
