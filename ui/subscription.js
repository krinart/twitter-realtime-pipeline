// Imports the Google Cloud client library
const {PubSub} = require('@google-cloud/pubsub');

function subscribeWithFlowControlSettings(
  subscriptionName,
  maxInProgress,
  timeout,
  messageHandler
) {

  // Creates a client
  const pubsub = new PubSub();

  const subscriberOptions = {
    flowControl: {
      maxMessages: maxInProgress,
    },
  };

  // References an existing subscription.
  // Note that flow control settings are not persistent across subscribers.
  const subscription = pubsub.subscription(subscriptionName, subscriberOptions);

  console.log(
    `Subscriber to subscription ${subscription.name} is ready to receive messages at a controlled volume of ${maxInProgress} messages.`
  );

  subscription.on(`message`, messageHandler);

  return function() {
    console.log("subscription.close()");
    subscription.close()
  }
}

exports.subscribeWithFlowControlSettings = subscribeWithFlowControlSettings;
