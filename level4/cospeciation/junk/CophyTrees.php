<?php


//$filename = "example_task3.txt";




echo "<!DOCTYPE html>";
echo "<html>";
echo "<head>";
echo "<title>CophyTrees - Cophylogeny Viewer</title>";
echo "<meta charset=\"utf-8\">";
echo "<script>";
echo "var error=\"\";";
echo "</script>";
echo "";
echo "<!--PLACEHOLDER 2-->";
echo "<link rel=\"stylesheet\" href=\"//eucalypt.gforge.inria.fr/viewer.css\" type=\"text/css\">";
echo "";
echo "</head>";
echo "";
echo "<body> ";
echo "    <!-- Page contents -->";
echo "    <div id=\"titleBox\">";
echo "       <h2 style=\"text-align:center; font-family:sans\"> <a href=\"http://eucalypt.gforge.inria.fr/index.html\"><img style=\"vertical-align:-18px; margin-right:30px\" src=\"images/logo_eucalypt.png\" height=\"80px\"></a> CophyTrees</h2>";
echo "       <i>A cophylogeny viewer for Eucalypt</i>";
echo "    </div>";
echo "    <div id = \"menubar\">";
echo "       <a href=\"viewer.html\" id=\"back\" title=\"CophyTrees home\" ><img src=\"http://eucalypt.gforge.inria.fr/images/btn-back.svg\" ></a>";
echo "       <a href=\"#\" id=\"reset\" title=\"Reset the view\" onclick=\"forest.reset()\"><img src=\"http://eucalypt.gforge.inria.fr/images/btn-reload.svg\" ></a>";
echo "       <a href=\"#\" id=\"save\" title=\"Download this image as SVG\" download=\"CophyTrees.svg\" target=\"blank\"><img src=\"http://eucalypt.gforge.inria.fr/images/btn-svg.svg\" ></a>";
echo "       <a href=\"#\"  id=\"savePng\" title=\"Download this image as PNG\"  ><img src=\"http://eucalypt.gforge.inria.fr/images/btn-png.svg\" ></a>";
echo "    </div>";
echo "    <span id=\"svgWindow\"></span>";
echo "    <div style=\" overflow:auto;max-height:300px;\"><div id=\"TreeSelector\"></div></div>";
echo "    <canvas width=\"2000\" height=\"1200\" scale=\"2\" style=\"display:none\"></canvas>";
echo "     ";
echo "    ";
echo "    <!-- Scripts and Data  --> ";
echo " ";
echo "<script src=\"d3.v3.min.js\"></script>";
echo "<script src=\"//eucalypt.gforge.inria.fr/trees.js\" ></script>";
echo "<script> var InputData = [];";


echo "</script> ";
echo "<script>";
echo "";
echo "var forest= cophyloForest();";
echo "";
echo "forest.svgWindow(\"#svgWindow\").treeSelector(\"#TreeSelector\").size([1000,600]);";
echo "";
echo "defineSaveButton(d3.select(\"#save\"));";
echo "defineSavePngButton(d3.select(\"#savePng\"));";
echo "";
echo "if (typeof InputData== \"undefined\") { ";
echo "  d3.select(\"body\").append(\"div\").attr(\"class\",\"msgBox\").style(\"position\", \"absolute\").style(\"top\",0).style(\"border-radius\", \"5px\").html(error+(error.length>0?\"<br>\":\"\")+ \"Displaying the <a href=\\\"default\\\">demonstration</a> file\") ";
echo "  d3.text(\"default\", function (err, d) {";
echo "     forest.open(d)";
echo "  })";
echo "} else {";
echo "  forest.open(InputData)";
echo "}";
echo "</script>";
echo "</body>";
echo "</html>"



?>


