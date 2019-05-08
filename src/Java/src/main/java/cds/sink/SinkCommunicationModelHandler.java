package cds.sink;

import cds.CommunicationModelHandler;
import cds.Model;
import cds.ModelReceiver;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class SinkCommunicationModelHandler extends CommunicationModelHandler {

    private int currentPoolSize;
    private ArrayList<Boolean> isNew;

    public SinkCommunicationModelHandler(String hostname) {
        super(hostname);
        currentPoolSize = 0;
        isNew = new ArrayList<Boolean>();
    }

    @Override
    protected void initNodeToSink() {
        try {
            Connection connectionNodeSink = factory.newConnection();
            channelNodeSink = connectionNodeSink.createChannel();
            channelNodeSink.queueDeclare(NODE_TO_SINK_QUEUE_NAME, false, false, false, null);
            System.out.println("[INFO] Waiting for models");

            receiver = new ModelReceiver(this);
            channelNodeSink.basicConsume(NODE_TO_SINK_QUEUE_NAME, true, receiver);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initSinkToNode() {
        try {
            Connection connectionSinkNode = factory.newConnection();
            channelSinkNode = connectionSinkNode.createChannel();
            channelSinkNode.exchangeDeclare(SINK_TO_NODE_EXCHANGE_NAME, "fanout", true);
            System.out.println("[INFO] Declaring SINK_TO_NODE_EXCHANGE, open? " + channelSinkNode.isOpen());
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initRPC() {
        Thread rpcServer = new RPCServer(this, channelRPC, factory.getHost());
        rpcServer.start();
    }

    @Override
    public void receiveModel(Model deliveredModel) {
        deliveredModel.toFile("src/data/sink/ModelNode" + deliveredModel.getNodeID() + ".json");
        isNew.set(deliveredModel.getNodeID() - 1, true);

        if (areAllNew()) {
            // Start the merging of the models
            ModelMerger mm = new ModelMerger(isNew.size(), this);
            mm.start();
        }
    }

    @Override
    public void sendModel() {
        try {
            Model model = new Model(-1, new String ( Files.readAllBytes( Paths.get("src/data/sink/MergedModel.json"))));
//            Model model = new Model(1, "CIAO");
            System.out.println("[INFO] Publishing " + model.toString() + " on SINK_TO_NODE_EXCHANGE, open? " + channelSinkNode.isOpen());
            channelSinkNode.basicPublish(SINK_TO_NODE_EXCHANGE_NAME, "", null, model.getBytes());
            isNew.forEach((e) -> {
                e = false;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean areAllNew() {
        boolean and = true;
        for (Boolean b : isNew)
            and = and && b;
        return and;
    }

    void increasePoolSize() {
        isNew.add(false);
        currentPoolSize++;
        System.out.println("[INFO] Increasing pool size, current size: " + currentPoolSize);
    }

    String getPoolSize() {
        return String.valueOf(currentPoolSize);
    }

    public static void main(String[] args) throws InterruptedException {
        SinkCommunicationModelHandler sink = new SinkCommunicationModelHandler("localhost");

        // Uncomment for testing
//        Thread.sleep(10000);
//        while(true) {
//            sink.sendModel();
//        }
    }
}
