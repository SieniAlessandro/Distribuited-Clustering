package cds.node;

import cds.CommunicationModelHandler;
import cds.Config;
import cds.Log;
import cds.Model;
import cds.ModelReceiver;
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

public class NodeCommunicationModelHandler extends CommunicationModelHandler {

    private static int nodeID;

    NodeCommunicationModelHandler(String hostname) {
        super(hostname);
    }

    public int getNodeID() {
        return nodeID;
    }

    @Override
    protected void initRPC() {
        try {
            Connection connectionRPC = factory.newConnection();
            channelRPC = connectionRPC.createChannel();
            Log.info("NodeCommunicationHandler", "Creating RPC Client");

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
            Log.info("Node-" + nodeID, "Declaring NODE_TO_SINK_QUEUE");
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

            Log.info("Node-" + nodeID, "Declaring SINK_TO_NODE_EXCHANGE");
            channelSinkNode.exchangeDeclare(SINK_TO_NODE_EXCHANGE_NAME, "fanout", true);

            String queueName = channelSinkNode.queueDeclare().getQueue();
            System.out.println("[DEBUG] Temporary queueName: " + queueName);
            channelSinkNode.queueBind(queueName, SINK_TO_NODE_EXCHANGE_NAME, "");

            receiver = new ModelReceiver(this);
            // Start listening to updated models
            Log.info("Node-" + nodeID, "Starting consuming from " + queueName);
            channelSinkNode.basicConsume(queueName, true, receiver);
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }

    // Function called when a new model arrives
    @Override
    public void receiveModel(Model deliveredModel) {
        // Notify the new model to the ML Module
        Log.info("Node-" + nodeID, "Updated model received");
        deliveredModel.toFile(Config.PATH_NODE_UPDATED_MODEL);
        Runnable notifier = () -> {
            try {
                Unirest.post("http://127.0.0.1:5000/server")
                        .header("content-type", "application/json")
                        .body("{\n\t\"command\":\"Update\"\n")
                        .asString();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        };
        notifier.run();
    }

    // Publish a model to the sink queue
    @Override
    public void sendModel() {
        try {
            Model model = new Model(nodeID, new String(Files.readAllBytes(Paths.get(Config.PATH_NODE_NEW_MODEL))));
            Log.info("Node-" + nodeID, "Publishing " + model.toString() + " on NODE_TO_SINK_QUEUE");
            channelNodeSink.basicPublish("", NODE_TO_SINK_QUEUE_NAME, null, model.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String callRegistration(String message) throws IOException, InterruptedException {
        Log.info("Node-" + nodeID, "Calling Sink's registration function");

        final String corrId = UUID.randomUUID().toString();

        String replyQueueName = channelRPC.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channelRPC.basicPublish("", RPC_NODE_TO_SINK_QUEUE_NAME, props, message.getBytes(StandardCharsets.UTF_8));

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        String ctag = channelRPC.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        }, consumerTag -> {
        });

        String result = response.take();
        Log.info("Node-" + nodeID, "Registration done! Received Node ID: " + result);
        channelRPC.basicCancel(ctag);
        return result;
    }

    // JUST FOR TESTING
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
