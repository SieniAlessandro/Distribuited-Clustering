package cds;

import java.io.*;

public class Model implements Serializable {

    private int nodeID;
    private String json;

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
            e.printStackTrace();
        }
        Model m = (Model) obj;
        this.nodeID = m.getNodeID();
        this.json = m.toString();
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
