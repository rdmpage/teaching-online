<?php

$callback = '';

$data = '{"tree":"((((Rat:0.007148,Human:0.001808):0.024345,Platypus:0.016588):0.012920,(Ostrich:0.018119,Alligator:0.006232):0.004708):0.028037,Frog:0);\n\t","dates":[{"left":"Rat","right":"Ostrich","mya":"260"},{"left":"Human","right":"Platypus","mya":"125"},{"left":"Alligator","right":"Ostrich","mya":"150"}]}';

$result = new stdclass;


if (isset($_GET['input']))
{
	$data = $_GET['input'];
}
if (isset($_GET['callback']))
{
	$callback = $_GET['callback'];
}

$obj = json_decode($data);

$input = array();

$input[] = "Sequence length=1000;";
$input[] = $obj->tree;

$n = count($obj->dates);
for ($i = 0; $i < $n; $i++)
{
	$str =  'mrca: ' . $obj->dates[$i]->left . ', ' . $obj->dates[$i]->right;
	
	if ($i == 0)
	{
		$str .=  ', fixage=' . $obj->dates[$i]->mya . ';';
	}
	else
	{
		$str .=  ', minage=' . $obj->dates[$i]->mya . ';';
	} 
	
	$input[] = $str;
}

$basefilename = 'tmp/' . time();
//$basefilename = time();

$input_filename = $basefilename . '-input.txt';

file_put_contents($input_filename, join("\n", $input));

$output_filename = $basefilename . '-output.txt';


// create input file




$command = "PATHd8/PATHd8  $input_filename  $output_filename 1>$basefilename.log";

//echo $command;

system($command);

// get results

$output = file_get_contents($output_filename);

//$result->output = $output;

$rows = explode("\n", $output);

// print_r($rows);

foreach ($rows as $row)
{
	if (preg_match('/d8 tree\s+:\s*(?<tree>.*)/', $row, $m))
	{
		$result->tree = $m['tree'];
	}
}

	if ($callback != '')
	{
		echo $callback . '(';
	}
	//echo json_encode($result, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
	echo json_encode($result);
	if ($callback != '')
	{
		echo ')';
	}


