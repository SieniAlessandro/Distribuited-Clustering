package cds.sink;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RPCServer extends Thread {

    private final String RPC_NODE_TO_SINK_QUEUE_NAME = "RPC_QUEUE";

    private Channel channelRPC;
    private SinkCommunicationModelHandler sink;
    private ConnectionFactory factory;

    public RPCServer(SinkCommunicationModelHandler sink, Channel channelRPC, String hostname) {
        this.sink = sink;
        this.channelRPC = channelRPC;

        this.factory = new ConnectionFactory();
        factory.setHost(hostname);
    }

    public void run() {
        try (Connection connectionRPC = factory.newConnection()) {
            channelRPC = connectionRPC.createChannel();

            System.out.println("[INFO] Starting RPC Server");
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

                    sink.increasePoolSize();

                    response += sink.getPoolSize();
                    System.out.println("[INFO] New node requesting registration. New node id: " + response);


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
            System.out.println("[INFO] Starting consuming from RPC_QUEUE");
            channelRPC.basicConsume(RPC_NODE_TO_SINK_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
            }));

            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
