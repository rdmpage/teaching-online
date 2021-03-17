<?php

//----------------------------------------------------------------------------------------
/**
 * @brief Convert a decimal latitude or longitude to deg° min' sec'' format in HTML
 *
 * @param decimal Latitude or longitude as a decimal number
 *
 * @return Degree format
 */
function decimal_to_degrees($decimal)
{
	$decimal = abs($decimal);
	$degrees = floor($decimal);
	$minutes = floor(60 * ($decimal - $degrees));
	$seconds = round(60 * (60 * ($decimal - $degrees) - $minutes));
	
	if ($seconds == 60)
	{
		$minutes++;
		$seconds = 0;
	}
	
	// &#176;
	$result = $degrees . '°' . $minutes . "'";
	if ($seconds != 0)
	{
		$result .= $seconds . '"';
	}
	return $result;
}

//----------------------------------------------------------------------------------------
/**
 * @brief Convert decimal latitude, longitude pair to deg° min' sec'' format in HTML
 *
 * @param latitude Latitude as a decimal number
 * @param longitude Longitude as a decimal number
 *
 * @return Degree format
 */
function format_decimal_latlon($latitude, $longitude)
{
	$html = decimal_to_degrees($latitude);
	$html .= ($latitude < 0.0 ? 'S' : 'N');
	$html .= ' ';
	$html .= decimal_to_degrees($longitude);
	$html .= ($longitude < 0.0 ? 'W' : 'E');
	return $html;
}



function points2kml($text)
{
	$rows = explode("\n", trim($text));
	
	$pts = array();
	foreach ($rows as $row)
	{
		$pt = preg_split('/,?\s+/', $row);
		$pts[] = $pt;
	}

	$kml = '';

	$kml .= "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n";
	$kml .= "<kml xmlns=\"http://earth.google.com/kml/2.1\">\n";
    $kml .= "<Document>\n";
	
	
	$kml .= "<Style id=\"whiteBall\">\n";
	$kml .= "<IconStyle>\n";
	$kml .= "<Icon>\n";
	$kml .= "<href>http://iphylo.org/~rpage/phyloinformatics/images/whiteBall.png</href>\n";
	$kml .= "</Icon>\n";
	$kml .= "</IconStyle>\n";
	$kml .= "</Style>\n";
	
	$count = 1;
	
	foreach ($pts as $pt)
	{
		$kml .= '<Placemark><styleUrl>#whiteBall</styleUrl>';
		
		$kml .= '<name>' . 'Point ' . $count . '</name>';
		$kml .= '<description>' . format_decimal_latlon($pt[0],$pt[1]) . '</description>';
		
		$kml .= '<Point>';
		$kml .= '<extrude>0</extrude><altitudeMode>absolute</altitudeMode>';
		$kml .= '<coordinates>' . $pt[1] . ',' . $pt[0] . '</coordinates>';
		$kml .= '</Point>';
		
		$kml .= '</Placemark>';
		
		$count++;
	}


	
    $kml .= "</Document>\n";
    $kml .= "</kml>\n";
    
    echo $kml;


	
}


$text = '';

if (isset($_POST['points']))
{
	$text = ($_POST['points']);
}


if ($text != '')
{
	// Make KML
	header('Content-disposition: attachment; filename=points.kml');
	header('Content-type: application/vnd.google-earth.kml+xml');
	points2kml($text);
}
else
{
	echo 
'<html>
<head>
	<style>
		.body {
			padding:20px;
		}
	</style>
</head>
<body>
<h1>Make a KML file</h1>
<p>Paste in some latitude and longitude coordinates, one pair per row.<br />Coordinates MUST be decimal
numbers, and use -ve for W and S.</p>
<form method="post" action="kml.php">
	<textarea id="points" name="points" rows="10" cols="60"></textarea>
	<br/>
	<button style="font-size: 16px;background:blue;color:white;" type="submit" value="Submit">Make KML file</button>
</form>


</body>
</html>';
}
?>	