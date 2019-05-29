package it.unipi.cds.federatedLearning;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;

/**
 * Each method of a Consumer implementation is called when an event on the RabbitMQ queue occurs.
 */
public class ModelReceiver implements Consumer {

    private CommunicationModelHandler handler;
    /**
     * {@inheritDoc}
     */
    public ModelReceiver(CommunicationModelHandler handler) {
        this.handler = handler;
    }
    /**
     * {@inheritDoc}
     */
    public void handleConsumeOk(String s) {
        Log.info("ModelReceiver", "Starting consuming");
    }
    /**
     * {@inheritDoc}
     */
    public void handleCancelOk(String s) {
        Log.info("ModelReceiver", "Consumer cancelled in the correct way: " + s );
    }
    /**
     * {@inheritDoc}
     */
    public void handleCancel(String s) throws IOException {
        Log.info("ModelReceiver", "Consumer cancelled in an incorrect way: " + s );
    }

    public void handleShutdownSignal(String s, ShutdownSignalException e) {
        Log.error("ModelReceiver", "Either the channel or the underlying connection has been shut down: " + s );
    }
    /**
     * {@inheritDoc}
     */
    public void handleRecoverOk(String s) {
        Log.info("ModelReceiver", "Recovery ok: " + s );
    }
    /**
     * {@inheritDoc}
     */
    public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
        Model deliveredModel = new Model(bytes);
        int nodeID = deliveredModel.getNodeID();
        Log.info("ModelReceiver", "New model delivered from : " + (nodeID==-1? "Sink": nodeID) );
        handler.receiveModel(deliveredModel);
    }
}
