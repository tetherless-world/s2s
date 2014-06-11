<?php

require_once("query.php");

//create log file
//print time stamp to log file
//dump $_GET (print_r($_GET)) to log file

$box = null;
$date = null;
$ndays = null;
$year = null;
$month = null;
$insts = null;
$instCls = null;
$params = null;
$paramCls = null;
$limit = 10;
$offset = 0;

if (@$_GET['bbox'] && @$_GET['bbox'] != '')
  $box = explode(",",$_GET['bbox']);
if (@$_GET['start'] && @$_GET['start'] != '')
  $begin = $_GET['start'];
if (@$_GET['ndays'] && @$_GET['ndays'] != '')
  $end = $_GET['ndays'];
if (@$_GET['year'] && @$_GET['year'] != '')
  $year = $_GET['year'];
if (@$_GET['month'] && @$_GET['month'] != '')
  $month = $_GET['month'];
if (@$_GET['instruments'] && @$_GET['instruments'] != '')
  $insts = explode(",",$_GET['instruments']);
if (@$_GET['instrumentClasses'] && @$_GET['instrumentClasses'] != '')
  $instCls = explode(",",$_GET['instrumentClasses']);
if (@$_GET['parameters'] && @$_GET['parameters'] != '')
  $params = explode(",",$_GET['parameters']);
if (@$_GET['parameterClasses'] && @$_GET['parameterClasses'] != '')
  $paramCls = explode(",",$_GET['parameterClasses']);
if (@$_GET['limit'] && @$_GET['limit'] != '')
  $limit = $_GET['limit'];
if (@$_GET['offset'] && @$_GET['offset'] != '')
  $offset = $_GET['offset'];
if (@$_GET['request'] && @$_GET['request'] != '')
  $type = $_GET['request'];

if ($type == "wms") { echo "http://vmap0.tiles.osgeo.org/wms/vmap0"; exit; }
else if ($type) getResponse(@$type, @$box, @$begin, @$end, @$year, @$month, @$insts, @$instCls, @$params, @$paramCls, @$limit, @$offset);
else header("HTTP/1.1 400 Bad Request");

?>