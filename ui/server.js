var fs = require('fs'),
    http = require('http'),
    buildWsServer = require('./ws').buildWsServer,
    FakeEvents = require('./fake_events').FakeEvents,
    subscribeWithFlowControlSettings = require('./subscription').subscribeWithFlowControlSettings,
    createKafkaSubscription = require('./kafka').createKafkaSubscription;

// Http server for static files.
http.createServer(function (req, res) {
  var path;
  if (req.url == "/")
    path = __dirname + "/static/index.html";
  else
    path = __dirname + "/static" + req.url;

  fs.readFile(path, function (err,data) {
    if (err) {
      res.writeHead(404);
      res.end(JSON.stringify(err));
      return;
    }
    res.writeHead(200);
    res.end(data);
  });
}).listen(8080);

const addSubscriber = createKafkaSubscription();
var closeSubscriberByClientId = {};

// Websocket server.
buildWsServer(
    (connection, clientId) => {
        var closeSubscriber = addSubscriber(
            message => {
                console.log(message.data);
                connection.sendUTF(message.value);
              })

        closeSubscriberByClientId[clientId] = closeSubscriber;
    //    FakeEvents.map(event => connection.sendUTF(JSON.stringify(event)))
    },
    clientId => {
        var closeSubscriber = closeSubscriberByClientId[clientId];
        closeSubscriber();
    }
);

