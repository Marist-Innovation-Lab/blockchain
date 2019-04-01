var canvas = d3.select("#network"),
    width = canvas.attr("width"),
    height = canvas.attr("height"),
    r=16,
    color = d3.scaleOrdinal(d3.schemeCategory20),
    ctx = canvas.node().getContext("2d"),
    simulation =  d3.forceSimulation()
        .force("x", d3.forceX(width/2))
        .force("y", d3.forceY(height/2))
        .force("collide", d3.forceCollide(r+1))
        .force("charge", d3.forceManyBody()
            .strength(-300))
        .force("link", d3.forceLink()
            .id(function (d) { return d.port; }));


    // graph.nodes.forEach(function (d) {
    //     d.x = Math.random() * width;
    //     d.y = Math.random() * height;
    // });

d3.json("./static/graphData.json", function (err, graph) {
  if (err) throw err;

    simulation
        .nodes(graph.nodes)
        .on("tick", update)
        .force("link")
          .links(graph.links);

    canvas
        .call(d3.drag()
            .container(canvas.node())
            .subject(dragsubject)
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended));

    function update() {
      ctx.clearRect(0, 0, width, height);

      ctx.clearRect(0, 0, width, height);

      ctx.beginPath();
      ctx.globalAlpha = 1.0;
      ctx.strokeStyle = "#000";
      graph.links.forEach(drawLink);
      ctx.stroke();

      ctx.globalAlpha = 1.0;
      graph.nodes.forEach(drawNode);

    }

    function dragsubject() {
        return simulation.find(d3.event.x, d3.event.y);
    }

});

function dragstarted(d) {
  if (!d3.event.active) simulation.alphaTarget(0.3).restart();
  d3.event.subject.fx = d3.event.subject.x;
  d3.event.subject.fy = d3.event.subject.y;
  console.log(d3.event.subject);
  document.getElementById("port").innerHTML = d3.event.subject.port;
  // document.getElementById("entrypointPeer").innerHTML = getentrypointPort();
}
function dragged() {
  d3.event.subject.fx = d3.event.x;
  d3.event.subject.fy = d3.event.y;
}
function dragended() {
  if (!d3.event.active) simulation.alphaTarget(0);
  d3.event.subject.fx = null;
  d3.event.subject.fy = null;
}

function getNodeColor(entrypoint){
  var color = "#000";

  if (entrypoint == "true") {
    color = "#0d3478";
  }else {
    color = "#add8e6";
  }
  return color;
}

// function getentrypointPort(entrypoint){
//     if (degree == "BS"){
//         return "Bachelors Degree";
//     }else if(degree == "MS"){
//         return "Masters Degree";
//     }
// }

function drawNode(d) {
    ctx.beginPath();
    ctx.fillStyle = getNodeColor(d.entrypoint);
    ctx.moveTo(d.x,d.y);
    ctx.arc(d.x, d.y, r, 0, 2 * Math.PI);
    ctx.fill();
}

function drawLink(l) {
    ctx.moveTo(l.source.x,l.source.y);
    ctx.lineTo(l.target.x,l.target.y);
}
