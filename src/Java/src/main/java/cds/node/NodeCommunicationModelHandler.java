package cds.node;

import cds.CommunicationModelHandler;
import cds.Model;
import cds.ModelReceiver;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class NodeCommunicationModelHandler extends CommunicationModelHandler {

    public NodeCommunicationModelHandler(String hostname) {
        super(hostname);
    }

    // Initialize connection and channel of Node-Sink communication
    @Override
    protected void initNodeToSink() {
        try {
            connectionNodeSink = factory.newConnection();
            channelNodeSink = connectionNodeSink.createChannel();
            channelNodeSink.queueDeclare(NODE_TO_SINK_QUEUE_NAME, false, false, false, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    // Initialize connection and channel of Sink-Node communication and start listening
    @Override
    public void initSinkToNode() {
        try {
            connectionSinkNode = factory.newConnection();
            channelSinkNode = connectionSinkNode.createChannel();
            channelSinkNode.exchangeDeclare(SINK_TO_NODE_EXCHANGE_NAME, "fanout");

            String queueName = channelSinkNode.queueDeclare().getQueue();
            channelNodeSink.queueBind(queueName,SINK_TO_NODE_EXCHANGE_NAME, "");

            receiver = new ModelReceiver(this);
            // Start listening to new models
            channelNodeSink.basicConsume(queueName, receiver);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Publish a model to the sink queue
    public void sendModelToSink(Model model) {
        try {
            channelNodeSink.basicPublish("", NODE_TO_SINK_QUEUE_NAME, null, model.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function called when a new model arrives
    @Override
    public void receiveModel(Model deliveredModel) {

        // Provide the new model to the ML Module
    }
}
