package it.unipi.cds.federatedLearning;

/**
 * Constant Repository
 */
public class Config {
    public static final String PATH_SINK_RECEIVED_MODELS = "dataSink/ModelNode";
    public static final String PATH_SINK_MERGED_MODEL = "dataSink/MergedModel.json";
    public static final String PATH_SINK_HISTORY = "dataSink/history/";
    public static final String PATH_SINK_POOL_SIZE = "dataSink/";

    public static final String PATH_NODE_BASEDIR = "dataNodes/";
    public static final String PATH_NODE_UPDATED_MODEL = PATH_NODE_BASEDIR + "NewUpdatedModel.json";
    public static final String PATH_NODE_NEW_MODEL = PATH_NODE_BASEDIR + "newModel";
    public static final String PATH_NODE_COLLECTED_DATA = PATH_NODE_BASEDIR + "collectedData";
    public static final String PATH_NODE_READY_DATA = PATH_NODE_BASEDIR + "readyData";

    public static final Double PERCENTAGE_OLD_VALUES = 0.5;
    public static final int SIZE_WINDOW = 200;

    /*
     * Generating parameters
     */
    public static final int MEAN = 30;
    public static final int ST_DEV = 5;
}
