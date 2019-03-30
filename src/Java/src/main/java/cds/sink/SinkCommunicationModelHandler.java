package cds.sink;

import cds.CommunicationModelHandler;
import cds.Model;
import cds.ModelReceiver;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SinkCommunicationModelHandler extends CommunicationModelHandler {
    private final String NODE_TO_SINK_QUEUE_NAME = "MODELS_QUEUE";
    private final String SINK_TO_NODE_EXCHANGE_NAME = "NEW_MODEL_QUEUE";

    public SinkCommunicationModelHandler(String hostname) {
        super(hostname);
    }

    @Override
    protected void initNodeToSink() {
        try {
            connectionNodeSink = factory.newConnection();
            channelNodeSink = connectionNodeSink.createChannel();
            channelNodeSink.queueDeclare(NODE_TO_SINK_QUEUE_NAME, false, false, false, null);
            System.out.println("[INFO] Waiting for models...");

            receiver = new ModelReceiver(this);
            channelNodeSink.basicConsume(NODE_TO_SINK_QUEUE_NAME, receiver);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initSinkToNode() {
        try {
            connectionSinkNode = factory.newConnection();
            channelSinkNode = connectionSinkNode.createChannel();

            channelSinkNode.exchangeDeclare(SINK_TO_NODE_EXCHANGE_NAME, "fanout");
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveModel(Model deliveredModel) {
        // Start the merging of the models
    }

    public void publishNewModel(Model model) {
        try {
            channelSinkNode.basicPublish(SINK_TO_NODE_EXCHANGE_NAME, "", null, model.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
