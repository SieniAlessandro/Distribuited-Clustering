package cds;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public abstract class CommunicationModelHandler {

    protected final String NODE_TO_SINK_QUEUE_NAME = "MODELS_QUEUE";
    protected final String SINK_TO_NODE_EXCHANGE_NAME = "NEW_MODEL_QUEUE";

    protected ConnectionFactory factory;
    protected Connection connectionNodeSink;
    protected Connection connectionSinkNode;
    protected Channel channelNodeSink;
    protected Channel channelSinkNode;
    protected ModelReceiver receiver;

    public CommunicationModelHandler(String hostname) {
        this.factory = new ConnectionFactory();
        factory.setHost(hostname);

        initNodeToSink();
        initSinkToNode();
    }

    protected abstract void initNodeToSink();

    protected abstract void initSinkToNode();

    public abstract void receiveModel(Model deliveredModel);
}
