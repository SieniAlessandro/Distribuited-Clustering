package it.unipi.cds.federatedLearning;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class represents a Model produced by a Node or a Sink. It consists of the nodeID of the node that
 * has instantiated the object and a String that is a a json representation of the Model.
 */
public class Model implements Serializable {

    private int nodeID;
    private String json;

    public Model(int id, String json) {
        this.nodeID = id;
        this.json = json;
    }

    /**
     * Construct a Model by de-serializing bytes
     * @param rawData bytes to be de-serialized
     */
    Model(byte[] rawData) {
        Object obj = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(rawData);
             ObjectInputStream ois = new ObjectInputStream(bis)){
            obj = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.error("Model", e.toString());
        }
        Model m = (Model) obj;
        if (m != null) {
            this.nodeID = m.getNodeID();
            this.json = m.getJson();
            //Log.debug("Model", json);
        }
    }

    /**
     * @return Get json String
     */
    public String getJson() {
        return json;
    }

    /**
     * Set json String
     * @param json json representation of Model
     */
    public void setJson(String json) {
        this.json = json;
    }
    /**
     * @return Get nodeID
     */
    public int getNodeID() {
        return nodeID;
    }

    /**
     * Set nodeID
     * @param nodeID Node Identification number
     */
    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    /**
     * Serialize this object
     * @return Serialization of Model Object
     */
    public byte[] getBytes() {
        byte[] data = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)){
            oos.writeObject(this);
            oos.flush();
            data = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Write json String in a file .json
     * @param filename Path to the filename
     */
    public void toFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(json);
        } catch (IOException e) {
            Log.error("Model", e.toString());
        }
    }

    /**
     * @return model name
     */
    @Override
    public String toString() {
        if ( this.nodeID == -1 )
            return "UpdatedModel";
        return "Model-" + nodeID;
    }


}
