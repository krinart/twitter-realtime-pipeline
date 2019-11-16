function d3_draw(el) {
    let svg = el.append('svg')
                .attr({
                  'width': width,
                  'height': height,
                  'viewBox': `0 0 ${width} ${height}`
                });

    d3.json('us-map.json', function(err, topology) {
      svg.append('path')
         .datum(topojson.feature(topology, topology.objects.land))
         .attr('d', path)
         .attr('class', 'land-boundary');

      svg.append('path')
         .datum(topojson.mesh(topology, topology.objects.states, (a, b) => a !== b))
         .attr('d', path)
         .attr('class', 'state-boundary');
    });
};

function d3_ping(el, zipcodes) {
    console.log(zipcodes);
    let colorScale = d3.scale.linear()
                       .domain(d3.extent(zipcodes, c => c.ts))
                       .range([0, 0.8]);

    let zipSel = d3.select('svg').selectAll('circle')
                    .data(zipcodes, (d) => d.lat)
                    .attr('fill-opacity', c => colorScale(c.ts));

    zipSel.enter().append('circle')
                   .attr({
                     'cx': (d) => projection([d.lon, d.lat])[0],
                     'cy': (d) => projection([d.lon, d.lat])[1]
                   })

    zipSel.attr({
             'r': 1,
             'opacity': 1e-6,
             'fill-opacity': 0.3,
             'fill': '#c4e3f3',
             'stroke': '#fff',
             'stroke-opacity': 1
           })
          .transition()
             .delay((d) => Math.floor((Math.random() * 1000) + 0))
             .duration(500)
             .ease('cubic-in-out')
             .attr({
               'fill': '#c4e3f3',
               'opacity': 1,
               'r': 60,
               'stroke-opacity': 0.4,
               'stroke-width': '1px',
               'stroke': '#361'
             })
           .each('end', function() {
              let dot = d3.select(this);

              dot.transition()
                  .duration(800)
                  .attr({
                    'fill': '#f2dede',
                    'opacity': 0.9,
                    'fill-opacity': 0.9,
                    'stroke-width': '1px',
                    'stroke': '#361',
                    'r': 2.2
                   })
                  .each('end', function() {
                    let point = d3.select(this);

                    point.transition()
                          .duration(5000)
                          .attr({
                            'fill': 'white'
                          })
                  })
           })
};

function getSampleZips(filteredZips, pct) {
  return _.sampleSize(filteredZips, pct * filteredZips.length)
};