const { Kafka } = require('kafkajs');

const kafka = new Kafka({
  clientId: 'ui',
  brokers: ['my-kafka-headless:9092']
})

function createKafkaSubscription() {
    var consumer = kafka.consumer({groupId: 'ui-group'})
    consumer.connect()
    consumer.subscribe({topic: 'twitter-geo-app', fromBeginning: true})

    var subscribers = {}

    consumer.run({
        eachMessage: async ({ topic, partition, message }) => {
            for (let [key, subscriber] of Object.entries(subscribers)) {
              console.log(`${key}: ${subscriber}`);
              subscriber(message);
            }
        },
    })

    function addSubscriber(subscriber) {
        var index = Object.entries(subscribers).length;
        console.log(`Add subscriber with index: ${index}`)
        subscribers[index] = subscriber;
        return function(){
            console.log(`Delete subscriber with index: ${index}`)
            delete subscribers[index]
        }
    }

    return addSubscriber;
}

exports.createKafkaSubscription = createKafkaSubscription;
