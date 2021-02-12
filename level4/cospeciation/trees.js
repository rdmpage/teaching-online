
var nHovering=0;
//infix ordering:
// x.infix = index, starting at 0 (left-most leaf)
// x.min = min index in a sub-tree
// x.max = max index in a sub-tree
d3.selection.prototype.first = function() {
  return d3.select(this[0][0]);
};


function cophyloLayout() {

var cophylo = {};


function prefixDFS(root, f) {   
   f(root);
   if (root.children) {
     root.children.forEach(function(n) {prefixDFS(n,f)})
   }
}

function pathToRootWithArcs(start, f, startWithNode) {
   if (typeof startWithNode == "undefined")
     startWithNode = true;
   f(start,startWithNode ); 
   if (startWithNode) { 
      if (start.uplink) {
        pathToRootWithArcs(start.uplink,f, false)
      }
   } else {
      pathToRootWithArcs(start.source,f, true)
   }
}
function visitWithArcs(root, f, startWithNode) {   
   if (typeof startWithNode == "undefined")
     startWithNode = true;
   
   f(root,startWithNode ); 
 //  console.log(startWithNode + "  children:"+root.children)
   if (startWithNode) { 
      if (root.children) {
        root.children.forEach(function(n) {visitWithArcs(n.uplink,f, false)})
      }
   } else {
      visitWithArcs(root.target,f, true)
   }
}

function highlightVisit(root, f, startWithNode) {
    visitWithArcs(root, f, startWithNode);
    pathToRootWithArcs(root, f, startWithNode);
}
function suffixDFS(root, f) {   
   if (root.children) {
     root.children.forEach(function(n) {suffixDFS(n,f)})
   }
   f(root);
}

function infixIndex(root, id) {
   if (typeof id == "undefined") {
      id=0;
   }
   if (root.children) {
      root.min =  +id;   
      id=infixIndex(root.children[0], id);      
      id++;
      root.infix=+id;
      id++;
      id=infixIndex(root.children[1], id);
      root.max = +id;
      return id;
   } else {
      root.min =  +id;   
      root.infix = +id;
      root.max = +id;
      return id;
   } 

}

function sortNumber(a,b) {
    return a - b;
}

function balance(root) {
   if (root.children) {
     
      var debug=0;
      var swap=0;
      var epsilon=1/(jumps.length+2);
      if (root.infix-root.min>root.max-root.infix) 
         epsilon*=-1;
      var leftweight=root.infix-root.min+5;
      var rightweight=root.max-root.infix+5; 
      
       if (debug) console.log(root.name + "   ***     " +root.min+" __ "+ root.infix + " __ "+root.max) 
      jumps.forEach(function(j){         
         ids = [+j[0].infix, +j[1].infix];
         ids.sort(sortNumber);
        // if (debug) console.log(root.name + "    ..........     jump   " +ids) 
         if (ids[0]<root.min && ids[1]>=root.min && ids[1]<=root.max && ids[1] != root.infix) {
            if (ids[1]<root.infix) {
               swap -=rightweight; //swap would create crossings with the right part of the tree
               if (debug) console.log("don't want to swap "+root.name + " (link to the left) -" +rightweight)
            } else {
               swap +=leftweight; //swap would remove crossings with the left part of the tree
               if (debug) console.log("want to swap "+root.name+ " (link to the left) +"+leftweight)
             }
         }
         if (ids[1]>root.max && ids[0]<=root.max && ids[0]>=root.min && ids[0] != root.infix) {
            if (ids[0]>root.infix) {
               swap -= leftweight; //swap would create crossings with the left part of the tree
               if (debug) console.log("don't want to swap "+root.name+ " (link to the right) -"+leftweight)
            } else {
               swap += rightweight; //swap would remove crossings with the right part of the tree
               if (debug) console.log("want to swap "+root.name+ " (link to the right) +"+rightweight)
            }
         }  
         if (ids[0]==root.infix && ids[1]>root.max)  swap+= epsilon;
         if (ids[1]==root.infix && ids[0]>root.max)  swap+= epsilon;
         if (ids[0]==root.infix && ids[1]<root.min)  swap-= epsilon;
         if (ids[1]==root.infix && ids[0]<root.min)  swap-= epsilon;
              
         
         
           
      });
      if (swap>0) {
          if (debug) console.log(root.infix +" SWAPPING " +root.name + " -- "+swap) 
         root.children.reverse();
         infixIndex(root, root.min);
      } else {
      
          if (debug) console.log(root.infix +" NOT SWAPPING " +root.name + " -- "+swap) 
      }
      
      balance(root.children[0]);      
      balance(root.children[1]);
    }
}

function isDescendant(top, bottom) {
   return (bottom.infix<= top.max) && (bottom.infix>=top.min);
}

function isStrictDescendant(top, bottom) {
   return top!=bottom && isDescendant(top,bottom)
}
function isChild(top, bottom) {
   return bottom.parent == top
}
function isJump(link) {
   return !(isDescendant(link.source.host,link.target.host)) && !(isDescendant(link.target.host, link.source.host))
}


var width = 1400,
    height = 1000,
    jumps, nodes, links;
    
var isDragging=false;
var isHovering=false;
var Hcluster ;
var Pcluster;



var Pforce;
//shortcuts to svg elements
var pnode, hnode,plink,hlink, hplink;
var svg ;
var zoomArea;
var glinkout
var gHtree
var gPlink
var glinkin
var gPtree


//scale parameters
var ysep=5;  
var radius=20;
var pradius=9;
var scale;

var diagonal = d3.svg.diagonal()
    .projection(function(d) { return [d.x, d.y]; });

var zoom; 


/*style functions*/
function pnodeColor(p) {
  if (p.fixed){
  //  if (p.hovering || !isHovering) {
      return  "#800";
  //  } else {
  //    return "#C88";
   // }
  } else {
   // if (p.hovering || !isHovering ) {
    
      return  "#000";
   // } else {
   //   return "#888";
    
   // }
  }
}

/*function pnodeColor(p) {
  if (p.fixed){
    if (p.hovering) {
      return  "#A38";
    } else {
      return "#63A";
    }
  } else {
    if (p.hovering) {
    
      return  "#808";
    } else {
      return "#000";
    
    }
  }*/
function plinkColor(l) {
  //  if (l.hovering || !isHovering) {
      return l.jump?"#55f":"#44e";
  //  } else {
  //    return "#88F";
  //  }
// .style("stroke", "#55f")
}

function setZoom(translate, scale) {
   
   zoomArea.attr("transform", "translate(" + translate + ")scale(" + scale + ")");
   cophylo.root.zoom = {"translate":translate, "scale":scale};
}

function zoomed() {
   setZoom(d3.event.translate, d3.event.scale);
}


    
   
   
    
    
function hpPath(d,i) {

 
 return "M "+d.x+","+d.y   
        +" L"+d.host.x+","+d.host.y;
}    


function path(d,i) {
 var bezier=1.5;
  var maxdx=radius;
  var dys=radius/0.866025/2;
  var dyt=-radius/0.866025;
 bezier=Math.max(bezier,1);
 if (d.ishost==false) {
   maxdx *=0.8;
   dys *=0.8;
   dyt *=0.8;
   bezier*=1.2;
 }
  
 if (d.source.x>d.target.x) dx *= -1;
 
 var dx=(d.target.x-d.source.x)/bezier;
 dx=Math.max(Math.min(dx,maxdx),-maxdx);
 
 if (!d.source.ishost  && ! d.source.original) {
         dx= 0;
         dys=0; 
 } 
 if (!d.target.ishost && ! d.target.original) {
         dyt0=0; 
 }
 
 
 return "M "+d.source.x+","+d.source.y     
     +" L"+(d.source.x+dx)+","+(d.source.y+dys)
     +" C"+(d.source.x+dx*bezier)+","+(d.source.y+dys*bezier)
     + " "+d.target.x+","+(d.target.y+dyt*bezier)
     + " "+d.target.x+","+(d.target.y+dyt)
     +" L"+d.target.x+","+d.target.y;
}    

function Ppath(d,i) {
 if (d.local) {
    return "M "+d.source.x+","+d.source.y   
        +" L"+d.target.x+","+d.target.y;
 } else {
    return path(d,i)
 }
}    

function tick(e) {
   var k=Math.min(1,e.alpha*2.5);
   Pnodes.forEach(function(p) {
      if (!p.fixed) { 
         p.x+=(p.host.x-p.x)*k;
         p.y+=(p.host.y-p.y)*k;
      }
   });
   for (var x=0; x<5; x++) {
      Plinks.forEach(function(l) {
         var sep=ysep * (l.local? 1 :3)*(1+e.alpha);
         
         if (!l.jump && l.source.y>l.target.y-sep && (!l.source.fixed||!l.target.fixed)) {
            var mid=(l.source.y+l.target.y)/2;
            
            if ( !l.source.fixed ) {
               if ( !l.target.fixed ) {
                   l.source.y=mid-sep/2;
                   l.target.y=mid+sep/2;
               } else {
                   l.source.y=l.target.y-sep;
               }
            } else {
                l.target.y=l.source.y+sep;
            }
         }
      })   
   }
    
   Plinks.forEach(function(l) {  
      if (!l.local) {
         k=Math.min(e.alpha*1.5, 1);
     /*    if (l.source.host.x > l.target.host.x) {
           k*=-1;
         }
         if (!l.source.alone && !l.source.fixed)
            l.source.x+=k
         if (l.jump && !l.target.alone  && !l.target.fixed) l.target.x-=k
       */  
      //   if (!l.jump) {
            dx = (l.target.x-l.deltax-l.source.x)*k*0.5*(l.jump?0.2:1)  
            dy = (l.target.y-l.deltay-l.source.y)*k*0.5*(l.jump?0.2:1)
            if (!l.source.fixed) {
               l.source.x += dx
               l.source.y += dy
            } 
            if (!l.target.fixed) {
               l.target.x -= dx
               l.target.y -= dy
            }     
        // }
         
         
      }
   });

   /* link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });*/
        
   if (!backgroundRunning) {     
      hplink
         .attr("d", hpPath)
      pnode     
         .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
         .select("circle")
      //   .attr("fill", function(d) {return d.fixed? "#800":"#000"});
         plink.select("path").attr("d", Ppath);
   }
}


var node_drag = d3.behavior.drag()
        .on("dragstart", dragstart)
        .on("drag", dragmove)
        .on("dragend", dragend);
    function dragstart(d, i) {
       d3.event.sourceEvent.stopPropagation();
       d.fixed = true; // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
       // Pforce.stop() // stops the force auto positioning before you start dragging
       isDragging=true;
       updateNodeStyle(d);
    }
    function dragmove(d, i) {
        d.px += d3.event.dx;
        d.py += d3.event.dy;
        d.x += d3.event.dx;
        d.y += d3.event.dy;
    }
    function dragend(d, i) {
        d.fixed = true; 
        isDragging=false;
        prefixDFS(d, updateNodeStyle)
        Pforce.resume();
        console.log("end drag");
        updateNodeStyle(d);
    }
    function releasenode(d) {
        console.log("release");
        //
        if (!isDragging) {
          d.fixed = false;
          updateNodeStyle(d)
      //    d3.event.sourceEvent.stopPropagation();
        }
        //force.resume();
    }

function populatePtree(tree){   

  tree.original=true;
  if (typeof tree.children != "undefined") {
     var c=tree.children.slice();
 
     tree.children=[];
     c.forEach(function(t) {
       var interm=t;  
       if (isStrictDescendant(tree.host, t.host)) {
          while (!isChild(tree.host, interm.host)) {
             var u={"name":"xxx", "children":[interm], "host":interm.host.parent , "original":false } ;
             interm.parent=u;
             interm=u;
          }
       }
       tree.children.push(interm);
       populatePtree(t);
     });
  }
}

var idealheight=100, idealwidth=100;

var backgroundRunning=false;
function parseData(root) { 


      
      //cluster structure: directly builds the set of links
      Hcluster = d3.layout.cluster();
      Pcluster = d3.layout.cluster();
      Hnodes = root.Hnodes = Hcluster.nodes(root.host);
      Hlinks = root.Hlinks = Hcluster.links(Hnodes);   
      
      Pnodes = Pcluster.nodes(root.parasite);
      
      //compute inner size of the layout (number of leaves * 2 * radius * 2.8) 
      var treedepth= d3.max(Hnodes, function (n) {return n.depth});
      
      //ideal sizes will be used for tree "inner" coordinates
      idealheight = root.idealheight=radius*treedepth*4.5;
      idealwidth = root.idealwidth=radius*Hnodes.length*2.8;
      
      //scale is used as a default zoom, 
      scale=Math.min(width/idealwidth, height/(idealheight+200));
         
      //first infix visit of the host: gives descendency relations
      infixIndex(root.host);
      
      //nodemap = {name ->  node object }
      // (contains only "standard" nodes)  
         
      var nodemap={};
      Hnodes.forEach(function (n) {nodemap[n.name] = n});
      Pnodes.forEach(function (n) {nodemap[n.name] = n});
     
     
      //write host/parasite relations
      Hnodes.forEach(function (h) {h.parasites=[]});  
      root.mapping.forEach(function(m) {
         var p=nodemap[m[0]];
         var h=nodemap[m[1]];
         h.parasites.push(p);
         p.host=h;      
      });
     
     //insert intermediate "blank" parasite nodes (so that parasite tree "follows" host tree)
     populatePtree(root.parasite);
     
     //recompute nodes and links
     Pnodes = root.Pnodes = Pcluster.nodes(root.parasite);
     Plinks = root.Plinks = Pcluster.links(Pnodes);   
     
     //classification       
     Hnodes.forEach(function(n) {
         n.ishost=true
         n.isNode=true;
     });
     
     Hlinks.forEach(function(l) {
         l.ishost=true
         l.isNode=false;    
      });
     
     Pnodes.forEach(function(p) {
         p.ishost=false;
         p.alone=(p.host.parasites.length==1); //alone: is a parasite alone in its host?
         p.radius = p.original ? pradius*2/ Math.min(p.host.parasites.length+1, 5) : 2;
         p.hovering=false;
         p.isNode=true;
     });
     
     Plinks.forEach(function(l) {
         l.ishost=false;
         l.target.uplink = l;
         l.hovering=false;
         l.isNode=false;         
     });
  
     
    
  
  createJumps () ;
  
}
function createJumps () {
  //compute list of jumps
  jumps = [];      
  Plinks.forEach(function(l) {
      l.local= (l.source.host==l.target.host);
      if (isJump(l) ) {
         l.jump=true;
         jumps.push([l.source.host,l.target.host]);
      } else {
         l.jump=false;   
      }
  });
}  

function setupSvg(where) {    
   var xtr=(width-idealwidth*scale)/2;
   var ytr=(height-idealheight*scale)/2;

   zoom=d3.behavior.zoom()
		.scale(scale)
		.on("zoom", zoomed);

    
  where.select("svg").remove(); //remove previous picture if any  
  var basesvg = where.append("svg")
    .attr("id","svgForest")
    .attr("width", width)
    .attr("height", height)
    .call(zoom);
//    .call(d3.behavior.zoom().on("zoom", update)).on("dblclick.zoom", null); 
   basesvg.on("dblclick.zoom", null);  
  
  basesvg.append("rect")
    .attr("x", 1)  
    .attr("y", 1)  
    .attr("width", width-2)  
    .attr("height", height-2)  
    .style("stroke", "none")
    .style("fill", "white");
  
 basesvg.append("text")
    .attr("id", "treeLabel")
    .attr("x","50%")
    .attr("y","590")
    .style("text-anchor","middle")
    .text("Hello");
  basesvg.append("filter")
    .attr("id", "f1")
    .attr("x","-100%")
    .attr("y","-20%")
    .attr("width","300%")
    .attr("height","140%")
    .append("feGaussianBlur")
    .attr("in", "SourceGraphic")
    .attr("stdDeviation",3)
    
var f=basesvg.append("filter")
    .attr("id", "f2")
    .attr("x","-20%")
    .attr("y","-20%")
    .attr("width","140%")
    .attr("height","140%");
    f.append("feGaussianBlur")
      .attr("in", "SourceGraphic")
      .attr("stdDeviation",1.5)
   // f.append("feOffset")
   //   .attr("dx", "0.5")
   //   .attr("dy", "0.0")
   
   f= basesvg.append("filter")
    .attr("id", "f3")
    f.append("feOffset")
     .attr("in", "SourceGraphic")
     .attr("dx", "-2")
     .attr("dy", "-2")
     .attr("result", "offOut")
    f.append("feGaussianBlur")
     .attr("in", "SourceAlpha")
     .attr("stdDeviation",1.5)
     .attr("result", "blurOut")
    f.append("feBlend")
     .attr("in", "offOut")
     .attr("in2", "blurOut")
     .attr("mode", "normal");
   
    
  svg=basesvg.append("g")
    .attr("id", "zoomArea")
    .attr("transform", "scale("+(scale)+")")
   .append("g")
    .attr("transform", "translate("+xtr+","+ytr+(radius*1.5)+")")
   .append("g")
  zoomArea=d3.select("#zoomArea");
  
  glinkout = svg.append("g").attr("id", "glinkout");
  glinkin = svg.append("g")
       .attr("id", "glinkin")  
      .attr("filter","url(#f2)");
  gHtree = svg.append("g").attr("id", "gHtree");
  gPlink = svg.append("g").attr("id", "gPlink");
  gPtree = svg.append("g").attr("id", "gPtree");
 
    
  basesvg.append("rect")
    .attr("x", 1)  
    .attr("y", 1)  
    .attr("width", width-2)  
    .attr("height", height-2)  
    .style("stroke", "black")
    .style("fill", "none")
    .style("stroke-width","4");
}

function recoverTree(root) {
   Hnodes = root.Hnodes; 
   Hlinks = root.Hlinks;
   Pnodes = root.Pnodes; 
   Plinks = root.Plinks;  
   idealheight = root.idealheight
   idealwidth = root.idealwidth 
   scale=Math.min(width/idealwidth, height/(idealheight+200));
   createJumps () ; 
}

function resetView() {
  zoom.translate([0,0]);
  zoom.scale(scale);
    setZoom([0,0], scale)
  Pnodes.forEach(function (d) {
   d.fixed=false;
   updateNodeStyle(d);
  });
  Plinks.forEach(function (d) {   
   updateLinkStyle(d);
  });
  Pforce.resume();
  

}

function updateLinkStyle(l) {
   if (l.svgElement) {
      l.svgElement.style("opacity",!isHovering || l.hovering ? "1" : "0.6"  )
      l.svgElement.select("path").style("stroke", plinkColor(l));
      l.svgElement.select("path").style("stroke-width", (l.hovering) ?"4px":"2px");
   }
}

function updateNodeStyle(p) {
   if (p.svgElement) {
      p.svgElement.style("opacity",!isHovering || p.hovering ? "1" : "0.6"  )
      p.svgElement.select("circle").style("fill", pnodeColor(p));
   }
}

function PTreeZOrder() {
 /* gPtree.selectAll("g").sort(function(a,b) {
      if (isHovering && (a.hovering!=b.hovering)) {
        return (a.hovering? 1 : -1)
      } else {
        return (a.isNode? 1 : -1)
      }  
  })*/

}
function backgroundRun() {
  var i;
  var j;
  backgroundRunning=true;
  for ( i=0; i<5; i++) {
      Pforce.resume();
      for ( j=0; j<10; j++) {
         Pforce.tick();
      }
  }
  
  backgroundRunning=false;
  Pforce.tick();
}
function hover(root, startOrEnd, isNode) {
  // console.log("***************hover : "+startOrEnd+", "+isNode+"**********************")
    nHovering+= startOrEnd? 1: -1;
    if (nHovering<0) {
      nHovering=0;
      console.log("xxxxxxxxxx");
    }
    isHovering  = (nHovering>0);
    highlightVisit(root, function(n, isNode) {
      n.hovering=startOrEnd;
    }, isNode);
    if (!isDragging) {
       Plinks.forEach( updateLinkStyle);
       Pnodes.forEach( updateNodeStyle);
    }
 /*   visitWithArcs(root, function(n, isNode) {
       if (!isDragging) {
         if (isNode) {
            updateNodeStyle(n); 
         } else {
            updateLinkStyle(n); 
         } 
       }
    }, isNode); */
    PTreeZOrder();
}

var newTree=true;

cophylo.build = function (where, root) {
  cophylo.root=root;
  
  if (root.visited) 
    newTree=false;
  
  root.visited=true;   
  if (newTree) {
     
     parseData(root);

      
     
     console.log("Manual layout of the host tree");
     infixIndex(root.host) 
     balance(root.host);
     
     
     console.log("Final build of the host tree");
     Hnodes = Hcluster
       .size([ idealwidth, idealheight])
       .nodes(root.host);
     Hlinks = Hcluster.links(Hnodes);   
     
     
     Pnodes.forEach(function (p) {    
        p.x=p.host.x;
        p.y=p.host.y; 
     });
     Plinks.forEach(function(l) {
        l.deltax=l.target.x - l.source.x;
        l.deltay=l.target.y - l.source.y;
     })
     
  } else {
     recoverTree(root);
  
  }
  
  
  console.log("Setting up the svg");
  setupSvg(where);
  
  if (!newTree && root.zoom) {
    zoom.translate(root.zoom.translate)
    zoom.scale(root.zoom.scale);
    setZoom(root.zoom.translate, root.zoom.scale)
  }
  d3.select("#treeLabel").text(root.label.replace(/^\[/,"").replace(/\]$/,""));
  
  console.log("Force layout for the parasites");
  Pforce = d3.layout.force()
    .nodes(Pnodes)
    .links(Plinks)
    .size([10, 10])
    .linkStrength(function (l) {return l.local?0.5 : 0.001} )
    .friction(0.9)
    .linkDistance(20)
    .charge(-300)
    .chargeDistance(2*radius)
    .gravity(0)
    .theta(0.8)
    .alpha(0.1)
    .start();
  
  
  console.log("Drawing Links");
  var line = d3.svg.line();

  var linko = glinkout.selectAll(".link")
      .data(Hlinks)
    .enter().append("path")
      .attr("class", "link")
      .style("fill", "none")
      .style("stroke-width","40px")
      .style("stroke-linecap","round")
      .style("stroke","black")
      .attr("d", path);
      
  var linki = glinkin.selectAll(".link")
      .data(Hlinks)
    .enter().append("path")
      .attr("class", "link")
      .style("fill", "none")
      .style("stroke-width","38px")
      .style("stroke-linecap","round")
      .style("stroke", "#eed")
     // .attr("filter","url(#f1)")
      .attr("d", path)
      .each(function(l) {l.svgElement=d3.select(this)});


  console.log("Drawing Nodes");

  hnode = gHtree.selectAll(".Hnode")
      .data(Hnodes)
    .enter().append("g")
      .attr("class", "Hnode")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
      .each(function(d){ d.svgElement = d3.select(this) });

  hnode.append("circle")
      .attr("r", radius/0.87)
      .style("fill", "#d8d8aa")
      .style("stroke-width", "0px")
      .attr("filter","url(#f3)");
      ;

  hplink = gHtree.selectAll(".HPlink")
      .data(Pnodes)
    .enter().append("path")
      .attr("class", "HPlink")
      .style("stroke-width",function (d) {return d.radius*2+"px"})
      .style("stroke","#d8d8aa")      
      .style("opacity", "0.6")
      .attr("d", hpPath)

  plink= gPtree.selectAll(".Plink")
      .data(Plinks)
    .enter()
      .append("g")
      .attr("class", "Plink")
      .each(function(l) {l.svgElement=d3.select(this)});
  plink.append("path")
      .style("fill", "none")
      .style("stroke", "#55f")
      .style("stroke-width","3px")
      .style("stroke-linecap","round")
      .style("stroke-dasharray", function (l) {return (l.jump?"2,5":"1,0")})
      .attr("d", Ppath)
      .on('mouseenter', function(d) {hover(d, true, false)})
      .on('mouseleave', function(d) {hover(d, false, false)})
  


  pnode = gPtree.selectAll(".Pnode")
      .data(Pnodes.filter(function(p){return p.original}))
    .enter().append("g")
      .attr("class", "Pnode")
      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })   
      .each(function(d){ d.svgElement = d3.select(this) })
      .on('dblclick', releasenode)
      .on('mouseenter', function(d) {hover(d, true, true)})
      .on('mouseleave', function(d) {hover(d, false, true)})
      .call(node_drag)
      
   
  pnode.append("circle")
      .attr("r", function (d) {return d.radius+"px"})
      .style("cursor", "pointer");   
      
      

  hnode.filter(function(d) {return d.children==null})
      .append("g")
      .attr("transform","rotate(90)")
      .append("text")
      .attr("dx", radius*1.5)
      .attr("dy", 0)
      .style("font","12px sans-serif")
      .style("text-anchor", "start")
      .style("stroke", "black")
      .text(function(d) { return d.name; });
      
   

  pnode.filter(function(d) {return d.children==null})
      .append("g")
      .attr("transform","rotate(45)")
      .append("text")
      .attr("dx", radius)
      .attr("dy", 0)
      .style("font","12px sans-serif")
      .style("text-anchor", "start")
      .style("stroke", "blue")
      //.style("display","none")
      .text(function(d) { return d.name; });
         
      
   /*  
  console.log("Drawing Jumps");
  
  svg.selectAll(".jump")   
      .data(jumps)
     .enter().append("line")
      .attr("class", "jump")
      .attr("x1", function (d) {return d[0].x})
      .attr("y1", function (d) {return d[0].y})
      .attr("x2", function (d) {return d[1].x})
      .attr("y2", function (d) {return d[1].y}) 
      .style("stroke","black")
      .style("stroke-width","1px");
      */
      
  Pforce.on("tick", tick);
  if (newTree) {
     backgroundRun(); 
  }
}

  cophylo.width = function(x) {
    if (!arguments.length) return width;
    width = x;
    return cophylo;
  };
  cophylo.height = function(x) {
    if (!arguments.length) return height;
    height = x;
    return cophylo;
  };
  cophylo.scale = function(x) {
    if (!arguments.length) return scale;
    scale = x;
    return cophylo;
  };
  cophylo.jumps= function() {return jumps; };
  cophylo.resetView= resetView;

//d3.select(self.frameElement).style("height", height + "px");


return cophylo;

}

function defineSaveButton(button) {

   button.on("mousedown", function(){
      var svgSrc = d3.select("#svgForest")
      .attr("version", 1.1)
      .attr("xmlns", "http://www.w3.org/2000/svg")
      .node().outerHTML;
          
      var imgsrc = 'data:image/svg+xml;base64,'+ btoa(svgSrc);
      button.attr("href", imgsrc); 
   }); 
}


function defineSavePngButton(button) {
 button.on("click", function(){
      var svgSrc = d3.select("#svgForest")
      .attr("version", 1.1)
      .attr("xmlns", "http://www.w3.org/2000/svg")
      .node().outerHTML;
          
      var imgsrc = 'data:image/svg+xml;base64,'+ btoa(svgSrc);
     // button.attr("href", imgsrc); 
     button.attr("href", "#"); 
     var canvas = document.querySelector("canvas"),
	     context = canvas.getContext("2d");
    
     var image = new Image;
     image.src = imgsrc;
     image.onload = function() {
	     context.drawImage(image, 0, 0, 2000,1200);
    
	     var canvasdata = canvas.toDataURL("image/png");
    
	     //var pngimg = '<img src="'+canvasdata+'">'; 
     	  //d3.select("#pngdataurl").html(pngimg);
    
	     var a = d3.select("body").append("a");
	     a.attr("download","Cophylogeny.png");
	     a.attr("href",canvasdata);
	     a.attr("target","blank");
	     a.node().click();
	     console.log(canvasdata);
     };
     console.log("...");
  })   
   
}


function deprecated() {
   cophyl = cophyloLayout().width(1500).height(800);
   d3.json("trees.json", function(error, root) {
     data=root;
     cophyl.build(d3.select("#svgWindow"), root["test3"]);
   })
 }
 
 
 function cophyloForest() {
    forest = {}
    var treeSelector="";
    var svgWindow="";
    forest.cophyl={};
    var data; 
    var size= [1000,600];

 

    
    var currentKey ="";
    var allData={};
     forest.treeSelector = function(x) {
       if (!arguments.length) return treeSelector;
       treeSelector = x;
       return forest;
     };    
     forest.svgWindow = function(x) {
       if (!arguments.length) return svgWindow;
       svgWindow = x;
       return forest;
     };
     forest.size = function(x) {
       if (!arguments.length) return size;
       size = x;
       return forest;
     };
 
       
       
      forest.reset = function () {
         forest.openTree(currentKey);
         forest.cophyl.resetView();
      }

      forest.openTree = function (root) {
        if (typeof root == "string") {
          root=allData[root];
        }  
        currentKey=root;
        forest.cophyl = cophyloLayout().width(size[0]).height(size[1]);
        forest.cophyl.build(d3.select("#svgWindow"), root);
      }

      function treeToJson(t) {
         x=t.replace(/,([^\(])/g,',  "name":"$1')
          .replace(/\(([^\(])/g,'("name":"$1')
          .replace(/([^\)]),/g,'$1",')
          .replace(/([^\)])\)/g,'$1")')
          .replace(/\)([^\)])/g,')"name" : "$1')
          .replace(/\(/g,'   "children":[ { \n ')
          .replace(/,/g,' }, \n { ')
          .replace(/\)/g,'} ], \n  ')
          .replace(/$/g,'"}\n ')
          .replace(/^/g,'{\n ');
          return x;
      }

      function mappingToJson(t) {
         x=t.replace(/, /g,'"],["')
           .replace(/\@/g,'","')
           .replace(/$/g,'"]]')
           .replace(/^(\s*)/g,'[["')
          return x;

     }
     
     
     forest.open = function (inputText) {
            
           var ht="";
           var pt="";
           var x;
           lines=inputText.split("\n")
           
           var matching="";
           var i=1;
           var keys=[];
           var jsonString="{";
           lines.forEach(function(l){
              if (/^\#Host tree\s*\=/.test(l)) {
                 ht=/[^=]*$/.exec(l)[0];
                 ht='"host": '+treeToJson(ht)+', ';
              }
              if (/^#Parasite tree\s*\=/.test(l)) {
                 pt=/[^=]*$/.exec(l)[0];
                 pt='"parasite": '+treeToJson(pt)+', ';
              }    
              if (/^[^\#\[]/.test(l)) {        
                 map='"mapping":'+mappingToJson(l);
              } 
              if (/^\[/.test(l)) {        
                 label=l;
                 if (i>1) 
                  jsonString+=',\n\n'; 
                 var key ='Mapping '+i+' '+label;
                 keys.push(key);
                 jsonString+='"'+key+'" : { "label":"'+label+'",'+ht + pt + map +'\n }';
                 i++;      
              }      
           })
           jsonString+="}";
           
           
           allData = JSON.parse(jsonString); 
           forest.allData=allData;
           currentKey=keys[0];
           forest.openTree(currentKey)
           if (keys.length>1) {
              links=
                d3.select(treeSelector).selectAll("div")
                 .data(keys)
                 .enter()
                 .append("div");
               if (keys.length>4) {
                links
                 .style("float", "left")
                 .style("width", "220px");
               }  
               links.append("a")
                 .text(function(k){return (keys.length>4 && k.length>43 ? k.substring(0,40)+"..." : k)})
                 .attr("href", "#")
                 .on("click", function(k){forest.openTree(k)});           
           }
         
         
      }
      return forest;
 }
