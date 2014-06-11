<?php

require_once("utils/agu-utils.php");

$search = null;
$years = null;
$meetings = null;
$sections = null;
$sessions = null;
$authors = null;
$keywords = null;
$limit = 10;
$offset = 0;
$sort = null;

$delimiter = ';';

if (@$_GET['q'] && $_GET['q'] != '')
  $search = $_GET['q'];
if (@$_GET['years'] && @$_GET['years'] != '')
  $years = explode($delimiter,$_GET['years']);
if (@$_GET['meetings'] && @$_GET['meetings'] != '')
  $meetings = explode($delimiter,$_GET['meetings']);
if (@$_GET['sections'] && @$_GET['sections'] != '')
  $sections = explode($delimiter,$_GET['sections']);
if (@$_GET['sessions'] && @$_GET['sessions'] != '')
  $sessions = explode($delimiter,$_GET['sessions']);
if (@$_GET['authors'] && @$_GET['authors'] != '')
  $authors = explode($delimiter,$_GET['authors']);
if (@$_GET['keywords'] && @$_GET['keywords'] != '')
  $keywords = explode($delimiter,$_GET['keywords']);
if (@$_GET['request'] && @$_GET['request'] != '')
  $type = $_GET['request'];
if (@$_GET['limit'] && @$_GET['limit'] != '')
  $limit = $_GET['limit'];
if (@$_GET['offset'] && @$_GET['offset'] != '')
  $offset = $_GET['offset'];

getResponse(@$search, @$years, @$meetings, @$sections, @$sessions, @$authors, @$keywords, @$type, @$limit, @$offset, @$sort);

?>