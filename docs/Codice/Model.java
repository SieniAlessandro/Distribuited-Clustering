package it.unipi.cds.federatedLearning;

import java.io.*;


public class Model implements Serializable {

    private int nodeID;
    private String json;

    public Model(int id, String json) {
        this.nodeID = id;
        this.json = json;
    }
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
        }
    }
    public String getJson() {
        return json;
    }
    public void setJson(String json) {
        this.json = json;
    }
    public int getNodeID() {
        return nodeID;
    }
    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }
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
    public void toFile(String filename) {
        File file = new File(filename);
        file.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(json);
        } catch (IOException e) {
            Log.error("Model", e.toString());
        }
    }
    @Override
    public String toString() {
        if ( this.nodeID == -1 )
            return "UpdatedModel";
        return "Model-" + nodeID;
    }
}
