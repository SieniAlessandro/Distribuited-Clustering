package cds;

import java.io.*;

public class Model {

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

    public Model(byte[] rawData) {
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

    public void toFile() {
        String pathname = "ModelNode" + nodeID + ".json";
        try (FileOutputStream fileOut = new FileOutputStream(pathname)) {
            fileOut.write(json.getBytes(), 0, json.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
