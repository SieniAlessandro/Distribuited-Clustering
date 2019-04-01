package cds;

public class Model {

    private int nodeID;

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public Model() {}

    public Model(byte[] rawData) {}

    public byte[] getBytes() {
        return null;
    }

}
