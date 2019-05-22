package it.unipi.cds.federatedLearning.node;

import it.unipi.cds.federatedLearning.CommunicationModelHandler;
import it.unipi.cds.federatedLearning.Config;
import it.unipi.cds.federatedLearning.Log;
import it.unipi.cds.federatedLearning.Model;
import it.unipi.cds.federatedLearning.ModelReceiver;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * This class handles the Node side communication between Sink and Node
 */
public class NodeCommunicationModelHandler extends CommunicationModelHandler {

    private static int nodeID;
    private static String queueName;
    /**
     * {@inheritDoc}
     */
    NodeCommunicationModelHandler(String hostname) {
        super(hostname);
    }
    /**
     * Get the Identification number of this node
     */
    public int getNodeID() {
        return nodeID;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initRPC() {
        try {
            Connection connectionRPC = factory.newConnection();
            channelRPC = connectionRPC.createChannel();
            Log.info("NodeCommunicationHandler", "Creating RPC Client");

            nodeID = Integer.parseInt(callFunction("Registration"));
        } catch (IOException | TimeoutException | InterruptedException e) {
            Log.error("Node", e.toString());
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initNodeToSink() {
        try {
            Connection connectionNodeSink = factory.newConnection();
            channelNodeSink = connectionNodeSink.createChannel();
            Log.info("Node-" + nodeID, "Declaring NODE_TO_SINK_QUEUE");
            channelNodeSink.queueDeclare(NODE_TO_SINK_QUEUE_NAME, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            Log.error("Node-" + nodeID, e.toString());
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void initSinkToNode() {
        try {
            Connection connectionSinkNode = factory.newConnection();
            channelSinkNode = connectionSinkNode.createChannel();

            Log.info("Node-" + nodeID, "Declaring SINK_TO_NODE_EXCHANGE");
            channelSinkNode.exchangeDeclare(SINK_TO_NODE_EXCHANGE_NAME, "fanout", true);

            queueName = channelSinkNode.queueDeclare().getQueue();
            System.out.println("[DEBUG] Temporary queueName: " + queueName);
            channelSinkNode.queueBind(queueName, SINK_TO_NODE_EXCHANGE_NAME, "");

            receiver = new ModelReceiver(this);
            // Start listening to updated models
            Log.info("Node-" + nodeID, "Starting consuming from " + queueName);
            channelSinkNode.basicConsume(queueName, true, receiver);
        } catch (TimeoutException | IOException e) {
            Log.error("Node-" + nodeID, e.toString());
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void receiveModel(Model deliveredModel) {
        // Notify the new model to the ML Module
        Log.info("Node-" + nodeID, "Updated model received");
        deliveredModel.toFile(Config.PATH_NODE_UPDATED_MODEL);
        Runnable notifier = () -> {
            try {
                Unirest.post("http://127.0.0.1:5000/server")
                        .header("content-type", "application/json")
                        .body("{\n\t\"command\":\"Update\",\n\t\"ID\":\""+ nodeID +"\"\n}")
                        .asString();
            } catch (UnirestException e) {
                Log.error("Node-" + nodeID, e.toString());
            }
        };
        notifier.run();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendModel() {
        try {
            Model model = new Model(nodeID, new String(Files.readAllBytes(Paths.get(Config.PATH_NODE_NEW_MODEL+nodeID+".json"))));
            Log.info("Node-" + nodeID, "Publishing " + model.toString() + " on NODE_TO_SINK_QUEUE");
            channelNodeSink.basicPublish("", NODE_TO_SINK_QUEUE_NAME, null, model.getBytes());
        } catch (IOException e) {
            Log.error("Node-" + nodeID, e.toString());
        }
    }
    /**
     * Call a Remote Procedure implemented by the Sink
     * @return function's response
     */
    public String callFunction(String function) throws IOException, InterruptedException {
        Log.info("Node", "Calling Sink's function: " + function);

        final String corrId = UUID.randomUUID().toString();

        String replyQueueName = channelRPC.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        if ( function.equals("Leave"))
            function = function.concat(":" + nodeID);

        channelRPC.basicPublish("", RPC_NODE_TO_SINK_QUEUE_NAME, props, function.getBytes(StandardCharsets.UTF_8));

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        String ctag = channelRPC.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        }, consumerTag -> {
        });

        String result = response.take();
        Log.info("Node", "Function " + function + " executed. Response: " + result);
        channelRPC.basicCancel(ctag);
        if ( function.startsWith("Leave") && result.equals("OK")) {
            try {
                channelNodeSink.close();
                channelSinkNode.close();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    /**
     * Run a NodeCommunicationHandler for testing purposes, the main class of the node is DataCollector.java
     */
    public static void main(String[] args) {
        NodeCommunicationModelHandler node = new NodeCommunicationModelHandler("localhost");
        while (true) {
            node.sendModel();
            try {
                Thread.sleep((long) Math.floor(Math.random() * 200));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
