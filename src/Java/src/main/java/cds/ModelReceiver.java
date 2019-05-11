package cds;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;

public class ModelReceiver implements Consumer {

    private CommunicationModelHandler handler;

    public ModelReceiver(CommunicationModelHandler handler) {
        this.handler = handler;
    }

    public void handleConsumeOk(String s) {
        Log.info("MordelReceiver", "Starting consuming");
    }

    public void handleCancelOk(String s) {
        Log.info("MordelReceiver", "Consumer cancelled in the correct way: " + s );
    }

    public void handleCancel(String s) throws IOException {
        Log.info("MordelReceiver", "Consumer cancelled in an incorrect way: " + s );
    }

    public void handleShutdownSignal(String s, ShutdownSignalException e) {
        Log.info("MordelReceiver", "Either the channel or the underlying connection has been shut down: " + s );
        System.out.println(e.toString());
    }

    public void handleRecoverOk(String s) {
        Log.info("MordelReceiver", "Recovery ok: " + s );
    }

    public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
        Model deliveredModel = new Model(bytes);
        int nodeID = deliveredModel.getNodeID();
        Log.info("MordelReceiver", "New model delivered from : " + (nodeID==-1? "Sink": nodeID) );
        handler.receiveModel(deliveredModel);
    }
}
