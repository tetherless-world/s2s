<?php

include_once("rds.php");

$authors = null;
$contributors = null;
$keywords = null;
$catalogs = null;
$type = null;
$limit = 10;
$offset = 0;
$sort = null;

$constraints = array();

if (@$_GET['authors'] && @$_GET['authors'] != '') {
    $authors = explode(";", $_GET['authors']);
    $constraints["authors"] = $authors;
}

if (@$_GET['contributors'] && @$_GET['contributors'] != '') {
    $contributors = explode(";", $_GET['contributors']);
    $constraints["contributors"] = $contributors;
}

if (@$_GET['keywords'] && @$_GET['keywords'] != '') {
    $keywords = explode(";", $_GET['keywords']);
    $constraints["keywords"] = $keywords;
}

if (@$_GET['catalogs'] && @$_GET['catalogs'] != '') {
    $catalogs = explode(";", $_GET['catalogs']);
    $constraints["catalogs"] = $catalogs;
}

if (@$_GET['limit'] && @$_GET['limit'] != '') {
    $limit = $_GET['limit'];
}

if (@$_GET['offset'] && @$_GET['offset'] != '') {
    $offset = $_GET['offset'];
}

if (@$_GET['sort'] && @$_GET['sort'] != '') {
    $sort = $_GET['sort'];
}

if (@$_GET['request'] && @$_GET['request'] != '') {
    $type = $_GET['request'];
}

$rds = new RDS_S2SConfig();
$out = $rds->getResponse(@$type, @$constraints, @$limit, @$offset, @$sort);
$size = strlen($out);
header("Content-length: $size");
echo $out;
