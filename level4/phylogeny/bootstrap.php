<?php

require_once('treelib/node_iterator.php');
require_once('treelib/svg.php');
require_once('treelib/tree_drawer.php');


//--------------------------------------------------------------------------------------------------


$tree_num = 0;

if (isset($_GET['tree_num']))
{
	$tree_num = $_GET['tree_num'];
}

$next = $tree_num + 1;
$skip = 1;

$filename = 'bs.tre';

$file_handle = fopen($filename, "r");

$count = 0;
while (!feof($file_handle)) 
{
	$newick = trim(fgets($file_handle));

	if ($count == ($tree_num + $skip))
	{
		
		// Read tree
		$t = new Tree();
		$t->Parse($newick);
		
		// labels
		
		$ni = new NodeIterator ($t->GetRoot());
		$q = $ni->Begin();
		while ($q != NULL)
		{	
			if ($q->IsLeaf ())
			{
				// Get children
				switch($q->GetLabel())
				{
					case '2':
						$q->SetLabel('human');
						break;
					case '3':
						$q->SetLabel('chimp');
						break;
					case '4':
						$q->SetLabel('gorilla');
						break;
					case '5':
						$q->SetLabel('orang');
						break;
					case '1':
						$q->SetLabel('gibbon');
						break;
	
						
					default:
						break;
				}
			}
			$q = $ni->Next();
		}
	
	// draw...
	
		$t->BuildWeights($t->GetRoot());
		
		
		$height = 400;
		$width = 400;
		$tree_width 	= $width - 100;
		
		// Drawing properties
		$attr = array();
		//$attr['inset']			= 0;
		$attr['width'] 			= $tree_width;
		$attr['height'] 		= $height;
		
		// font size
		$font_height = 24;
		
		$attr['font_height'] 	= $font_height;
		$attr['line_width'] 	= 1;
		
		// Don't draw labels (we do this afterwards)
		$attr['draw_leaf_labels'] = true;
		$attr['draw_internal_labels'] = false;
		$attr['draw_scale_bar'] = false;
		
		$td = NULL;
		
		if ($t->HasBranchLengths())
		{
			$td = new PhylogramTreeDrawer($t, $attr);
		}
		else
		{
			$td = new RectangleTreeDrawer($t, $attr);
		}
		
		
		$td->CalcCoordinates();	
		$port = new SVGPort('', $width, $height, $font_height, false);
		
		$port->StartGroup('tree', true);
		$td->Draw($port);
		$port->EndGroup();
		
		
		$svg = $port->GetOutput();
		
		echo '<a href=".">Home</a>';
		
		echo '<h1>Boostrap trees</h1>';
		echo '<p>When we boostrap a dataset we <b>resample</b> the data and generate a <b>new tree</b> from that resampled data. 
		Clicking <a href="bootstrap.php?tree_num=' . $next . '">Next</a> below will show the tree
		generated from the next sample of the data. As you click on next, keep track of what groups appear in
		the trees. Are they always the same?</p>';
		
		echo '<h2>Tree ' . $tree_num . '</h2>';
		
		echo $svg;
		echo '<br/>';
		echo '<a href="bootstrap.php?tree_num=' . $next . '">Next</a>';
		exit();
	}
	$count++;
}





?>
