package it.unipi.cds.federatedLearning.sink;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import it.unipi.cds.federatedLearning.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Remote Procedure Call Server that exposes Sink's functions callable by Nodes
 */
public class RPCServer extends Thread {

    private Channel channelRPC;
    private SinkCommunicationModelHandler sink;
    private ConnectionFactory factory;


    /**
     * Check if every node has sent its new model
     * @param sink instance of a SinkCommunicationModelHandler
     * @param channelRPC Channel for the Remote Procedure Call
     * @param hostname IP Address of RabbitMQ Server
     */
    RPCServer(SinkCommunicationModelHandler sink, Channel channelRPC, String hostname) {
        this.sink = sink;
        this.channelRPC = channelRPC;

        this.factory = new ConnectionFactory();
        factory.setHost(hostname);
    }

    /**
     * Run a Remore Procedure Call Server that listens to node's call
     *  <ul>
     *      <li>Registration: A Node wants to join the node pool, so this server provides it a nodeID</li>
     *      <li>Leave: A Node wants to leave the node pool. It has to include also its nodeID in the request</li>
     *  </ul>
     */
    public void run() {
        try (Connection connectionRPC = factory.newConnection()) {
            channelRPC = connectionRPC.createChannel();

            Log.info("RPCServer", "Starting RPC Server");
            String RPC_NODE_TO_SINK_QUEUE_NAME = "RPC_QUEUE";
            channelRPC.queueDeclare(RPC_NODE_TO_SINK_QUEUE_NAME, false, false, false, null);
            channelRPC.queuePurge(RPC_NODE_TO_SINK_QUEUE_NAME);
            channelRPC.basicQos(1);
            Object monitor = new Object();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "";

                try {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    if (message.equals("Registration")) {
                        response += sink.registration();
                    } else if (message.startsWith("Leave")) {
                        int nodeID = Integer.parseInt(message.split(":")[1]);
                        response += sink.removeNode(nodeID);
                    }


                } catch (RuntimeException e) {
                    System.out.println(" [ERROR] " + e.toString());
                } finally {
                    channelRPC.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes(StandardCharsets.UTF_8));
                    channelRPC.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };
            Log.info("RPCServer", "Starting consuming from RPC_QUEUE");
            channelRPC.basicConsume(RPC_NODE_TO_SINK_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
            }));

            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        Log.error("RPCServer", e.toString());
                    }
                }
            }
        } catch (TimeoutException | IOException e) {
            Log.error("RPCServer", e.toString());
        }
    }
}
