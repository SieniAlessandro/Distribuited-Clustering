package cds;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

public abstract class CommunicationModelHandler {

    protected final String RPC_NODE_TO_SINK_QUEUE_NAME = "RPC_QUEUE";
    protected final String NODE_TO_SINK_QUEUE_NAME = "MODELS_QUEUE";
    protected final String SINK_TO_NODE_EXCHANGE_NAME = "NEW_MODEL_QUEUE";

    protected ConnectionFactory factory;
    protected Channel channelNodeSink;
    protected Channel channelSinkNode;
    protected Channel channelRPC;
    protected ModelReceiver receiver;

    public CommunicationModelHandler(String hostname) {
        this.factory = new ConnectionFactory();
        factory.setHost(hostname);

        initRPC();
        initSinkToNode();
        initNodeToSink();
    }

    protected abstract void initNodeToSink();

    protected abstract void initSinkToNode();

    protected abstract void initRPC();

    public abstract void receiveModel(Model deliveredModel);

    public abstract void sendModel();
}
