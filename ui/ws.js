var WebSocketServer = require('websocket').server;
var http = require('http');

var clients = [];

function buildWsServer(onInit, onClose, onMessage = message => {}) {

    console.log(onClose);

    var server = http.createServer(function(request, response) {
      // process HTTP request. Since we're writing just WebSockets
      // server we don't have to implement anything.
    });
    server.listen(1337, function() { });

    // create the server
    wsServer = new WebSocketServer({
      httpServer: server
    });

    // WebSocket server
    wsServer.on('request', function(request) {
      var connection = request.accept(null, request.origin);
      var clientId = clients.push(connection) - 1;

      console.log((new Date()) + ' Connection accepted.');
      onInit(connection, clientId);

      // This is the most important callback for us, we'll handle
      // all messages from users here.
      connection.on('message', function(message) {
        if (message.type === 'utf8') {
          // process WebSocket message
          onMessage(message)
        }
      });

      connection.on('close', function(connection) {
        // close user connection
        onClose(clientId);
      });
    });
}

exports.buildWsServer = buildWsServer;
