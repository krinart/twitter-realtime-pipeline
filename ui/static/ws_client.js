function doWS(onMessage) {
  // if user is running mozilla then use it's built-in WebSocket
  window.WebSocket = window.WebSocket || window.MozWebSocket;

  var url = 'ws://' + window.location.hostname + ':1337';
  console.log(url);

  var connection = new WebSocket(url);

  connection.onopen = function () {
    // connection is opened and ready to use
  };

  connection.onerror = function (error) {
    // an error occurred when sending/receiving data
  };

  connection.onmessage = onMessage;
}