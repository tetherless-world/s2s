<?php

require_once("utils.php");

$begin = null;
$end = null;
$authors = null;
$themes = null;
$projects = null;
$events = null;
$freetext = null;
$limit = 10;
$offset = 0;
$sort = null;
$type = null;

if (@$_REQUEST['startDate'] && @$_REQUEST['startDate'] != '')
  $begin = $_REQUEST['startDate'];
if (@$_REQUEST['endDate'] && @$_REQUEST['endDate'] != '')
  $end = $_REQUEST['endDate'];
if (@$_REQUEST['authors'] && @$_REQUEST['authors'] != '')
  $authors = explode(";",$_REQUEST['authors']);
if (@$_REQUEST['events'] && @$_REQUEST['events'] != '')
  $events = explode(";",$_REQUEST['events']);
if (@$_REQUEST['themes'] && @$_REQUEST['themes'] != '')
  $themes = explode(";",$_REQUEST['themes']);
if (@$_REQUEST['projects'] && @$_REQUEST['projects'] != '')
  $projects = explode(";",$_REQUEST['projects']);
if (@$_REQUEST['q'] && @$_REQUEST['q'] != '')
  $freetext = $_REQUEST['q'];
if (@$_REQUEST['limit'] && @$_REQUEST['limit'] != '')
  $limit = $_REQUEST['limit'];
if (@$_REQUEST['offset'] && @$_REQUEST['offset'] != '')
  $offset = $_REQUEST['offset'];
if (@$_REQUEST['sort'] && @$_REQUEST['sort'] != '')
  $sort = $_REQUEST['sort'];
if (@$_REQUEST['request'] && @$_REQUEST['request'] != '')
  $type = $_REQUEST['request'];

main($freetext, $begin, $end, $authors, $events, $projects, $themes, $type, $limit, $offset, $sort);

?>