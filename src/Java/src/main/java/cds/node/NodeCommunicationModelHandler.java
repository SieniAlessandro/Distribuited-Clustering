package cds.node;

import cds.CommunicationModelHandler;
import cds.Model;
import cds.ModelReceiver;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class NodeCommunicationModelHandler extends CommunicationModelHandler {

    private int nodeID;

    public NodeCommunicationModelHandler(String hostname) {
        super(hostname);

    }

    @Override
    protected void initRPC() {
        try {
            Connection connectionRPC = factory.newConnection();
            channelRPC = connectionRPC.createChannel();
            System.out.println("[INFO] Creating RPC Client");

            nodeID = Integer.parseInt(callRegistration(this.toString()));
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    // Initialize connection and channel of Node-Sink communication
    @Override
    protected void initNodeToSink() {
        try {
            Connection connectionNodeSink = factory.newConnection();
            channelNodeSink = connectionNodeSink.createChannel();
            System.out.println("[INFO] Declaring NODE_TO_SINK_QUEUE");
            channelNodeSink.queueDeclare(NODE_TO_SINK_QUEUE_NAME, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    // Initialize connection and channel of Sink-Node communication and start listening
    @Override
    public void initSinkToNode() {
        try {
            Connection connectionSinkNode = factory.newConnection();
            channelSinkNode = connectionSinkNode.createChannel();
            System.out.println("[INFO] Declaring SINK_TO_NODE_EXCHANGE");
            channelSinkNode.exchangeDeclare(SINK_TO_NODE_EXCHANGE_NAME, "fanout");

            String queueName = channelSinkNode.queueDeclare().getQueue();
            System.out.println("[DEBUG] Temporary queueName: " + queueName);
            channelSinkNode.queueBind(queueName, SINK_TO_NODE_EXCHANGE_NAME, "");

            receiver = new ModelReceiver(this);
            // Start listening to new models
            System.out.println("[INFO] Starting consuming from " + queueName);
            channelSinkNode.basicConsume(queueName, receiver);
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }

    // Publish a model to the sink queue
    public void sendModelToSink(Model model) {
        try {
            File modelFile = new File("../Local/CurrentModel");
            System.out.println("[INFO] Publishing " + model.toString() + " on NODE_TO_SINK_QUEUE" );
            channelNodeSink.basicPublish("", NODE_TO_SINK_QUEUE_NAME, null, model.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String callRegistration(String message) throws IOException, InterruptedException {
        System.out.println("[INFO] Calling Sink's registration function" );

        final String corrId = UUID.randomUUID().toString();

        String replyQueueName = channelRPC.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channelRPC.basicPublish("", RPC_NODE_TO_SINK_QUEUE_NAME, props, message.getBytes("UTF-8"));

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        String ctag = channelRPC.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });

        String result = response.take();
        System.out.println("[INFO] Registration done! Received Node ID: " + result);
        channelRPC.basicCancel(ctag);
        return result;
    }

    // Function called when a new model arrives
    @Override
    public void receiveModel(Model deliveredModel) {
        // Provide the new model to the ML Module
    }


    public static void main(String[] args) {
        NodeCommunicationModelHandler node = new NodeCommunicationModelHandler("localhost");

        while(true) {

//            node.sendModelToSink();
            try {
                Thread.sleep((long)Math.floor(Math.random()*200));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
