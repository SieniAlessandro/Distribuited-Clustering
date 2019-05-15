package it.unipi.cds.federatedLearning;

/**
 * Constant Repository
 */
public class Config {
    public static final String PATH_SINK_RECEIVED_MODELS = "dataSink/ModelNode";
    public static final String PATH_SINK_MERGED_MODEL = "dataSink/MergedModel.json";
    public static final String HOSTNAME_SINK = "10.102.5.54";

    public static final String PATH_NODE_BASEDIR = "dataNodes/";
    public static final String PATH_NODE_UPDATED_MODEL = PATH_NODE_BASEDIR + "NewUpdatedModel.json";
    public static final String PATH_NODE_NEW_MODEL = PATH_NODE_BASEDIR + "newModel";
    public static final String PATH_NODE_COLLECTED_DATA = PATH_NODE_BASEDIR + "collectedData";
    public static final String PATH_NODE_READY_DATA = PATH_NODE_BASEDIR + "readyData";
    public static final String HOSTNAME_NODE = "10.102.5.54";

}
