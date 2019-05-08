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
        System.out.println("[INFO] Starting consuming");
    }

    public void handleCancelOk(String s) {
        System.out.println("[INFO] Consumer cancelled in the correct way: " + s );
    }

    public void handleCancel(String s) throws IOException {
        System.out.println("[INFO] Consumer cancelled in an incorrect way: " + s );
    }

    public void handleShutdownSignal(String s, ShutdownSignalException e) {
        System.out.println("[INFO] Either the channel or the underlying connection has been shut down: " + s );
        System.out.println(e.toString());
    }

    public void handleRecoverOk(String s) {
        System.out.println("[INFO] Recovery ok: " + s );
    }

    public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
        System.out.println("[INFO] New model delivered: " + s );

        System.out.println("[INFO] Envelope information: " + envelope.toString());

        Model deliveredModel = new Model(bytes);
        handler.receiveModel(deliveredModel);
    }
}
