package it.unipi.cds.federatedLearning;

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

    /**
     * Constructor of a CommunicationModelHandler class
     * @param hostname RabbitMQ Server IP Address
     */
    public CommunicationModelHandler(String hostname) {
        this.factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setVirtualHost("cds/");
        factory.setUsername("cdsAdmin");
        factory.setPassword("cds");

        initRPC();
        initSinkToNode();
        initNodeToSink();
    }

    /**
     * Initialize a queue for the communication from Node to Sink.
     * <ul>
     *     <li>Sink Side: start to consume arriving Models in the queue</li>
     *     <li>Node Side: just declare the queue that will be used to send Model to the Sink  </li>
     * </ul>
     */
    protected abstract void initNodeToSink();

    /**
     * Initialize an exchange for the communication from Sink to Node.
     * <ul>
     *     <li>Sink Side: just declare the exchange on which updated Models will be published</li>
     *     <li>Node Side: get a temporary queue and bind it to the exchange, then start consuming the arriving Models</li>
     * </ul>
     */
    protected abstract void initSinkToNode();

    /**
     * Initialize a Remote Procedure Call mechanism to add a new node to the pool
     * <ul>
     *     <li>Sink Side: run a RPCServer thread that listens to nodes' registration requests</li>
     *     <li>Node Side: call the Remote Procedure and assign the response to the nodeID</li>
     * </ul>
     */
    protected abstract void initRPC();

    /**
     * Receive a model
     * <ul>
     *     <li>Sink Side: a new model arrived on the NodeToSink queue</li>
     *     <li>Node Side: a new updated model has been published on the SinkToNode exchange</li>
     * </ul>
     * @param deliveredModel received Model
     */
    public abstract void receiveModel(Model deliveredModel);

    /**
     * Send a model
     * <ul>
     *     <li>Sink Side: publish an updated Model on the SinkToNode exchange</li>
     *     <li>Node Side: put the new model in the NodeToSink queue</li>
     * </ul>
     */
    public abstract void sendModel();

}
