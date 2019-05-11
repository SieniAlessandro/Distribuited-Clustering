package cds.sink;

import cds.*;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

public class SinkCommunicationModelHandler extends CommunicationModelHandler {

    private int currentPoolSize;
    private ArrayList<Boolean> isNew;
    private boolean merging;

    public SinkCommunicationModelHandler(String hostname) {
        super(hostname);
        this.currentPoolSize = 0;
        this.isNew = new ArrayList<Boolean>();
        this.merging = false;
    }

    @Override
    protected void initNodeToSink() {
        try {
            Connection connectionNodeSink = factory.newConnection();
            channelNodeSink = connectionNodeSink.createChannel();
            channelNodeSink.queueDeclare(NODE_TO_SINK_QUEUE_NAME, false, false, false, null);
            System.out.println("Waiting for models");

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
            Log.info("SinkCommunicationModelHandler","Declaring SINK_TO_NODE_EXCHANGE");
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
        deliveredModel.toFile(Config.PATH_SINK_RECEIVED_MODELS + deliveredModel.getNodeID() + ".json");
        isNew.set(deliveredModel.getNodeID() - 1, true);

        if (areAllNew() && !merging) {
            // Start the merging of the models
            merging = true;
            ModelMerger mm = new ModelMerger(isNew.size(), this);
            mm.start();
        }
    }

    @Override
    public void sendModel() {
        try {
            Model model = new Model(-1, new String ( Files.readAllBytes( Paths.get(Config.PATH_SINK_MERGED_MODEL))));
//            Model model = new Model(1, "CIAO");
            Log.info("Sink","Publishing " + model.toString() + " on SINK_TO_NODE_EXCHANGE");
            channelSinkNode.basicPublish(SINK_TO_NODE_EXCHANGE_NAME, "", null, model.getBytes());
            Collections.fill(isNew, Boolean.FALSE);
            Log.debug("Sink", isNew.toString());
            merging = false;
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
        Log.info("Sink","Increasing pool size, current size: " + currentPoolSize);
    }

    String getPoolSize() {
        return String.valueOf(currentPoolSize);
    }

    public static void main(String[] args) throws InterruptedException {
    	new SinkCommunicationModelHandler(Config.HOSTNAME_SINK);

        // Uncomment for testing
//        Thread.sleep(10000);
//        while(true) {
//            sink.sendModel();
//        }
    }
}
