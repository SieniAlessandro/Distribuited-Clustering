import os
from os import path,listdir
from os.path import isfile, join,isdir
import shutil
import json
DATA_NODES_FOLDER = "../../dataNodes"
DATA_SINK_FOLDER = "../../dataSink"
NODES_LOG_BASE_FOLDER = "../../dataNodes/Log"
SINK_LOG_BASE_FOLDER = "../../dataSink/Log"
def removeOldFiles():
    #Removing the old file in the folder dataNodes:
    if os.path.exists(DATA_NODES_FOLDER) :
        #Getting all the files
        filenames = [f for f in listdir(DATA_NODES_FOLDER) if isfile(join(DATA_NODES_FOLDER, f))]
        dirnames = [f for f in listdir(DATA_NODES_FOLDER) if isdir(join(DATA_NODES_FOLDER, f))]
        #Deleting the files
        for filename in filenames:
            os.remove(DATA_NODES_FOLDER+"/"+filename)
        #Deleting the directory
        for dirname in dirnames:
            shutil.rmtree(DATA_NODES_FOLDER+"/"+dirname)
    #Removing the old file in the folder dataSink:
    '''
    if os.path.exists(DATA_SINK_FOLDER):
        #Getting all the files
        filenames = [f for f in listdir(DATA_SINK_FOLDER) if isfile(join(DATA_SINK_FOLDER, f))]
        dirnames = [f for f in listdir(DATA_NODES_FOLDER) if isdir(join(DATA_NODES_FOLDER, f))]
        #Deleting the files
        for filename in filenames:
            os.remove(DATA_SINK_FOLDER+"/"+filename)
        #Deleting the directory
        for dirname in dirnames:
            shutil.rmtree(DATA_NODES_FOLDER+"/"+dirname)
    '''
def save(type,id,fileName,jsonToSave):
    #Type 0 is for the Node, Type 1 for the Sink
    if type == 0:
        LOG_DIR_PATH =  NODES_LOG_BASE_FOLDER+str(id)
        if not os.path.exists(LOG_DIR_PATH):
            os.makedirs(LOG_DIR_PATH)
        with open(LOG_DIR_PATH+"/"+filename,"w") as fileToSave:
            json.dump(jsonToSave,fileToSave)
    else:
        LOG_DIR_PATH =  SINK_LOG_BASE_FOLDER+str(id)
        if not os.path.exists(LOG_DIR_PATH):
            os.makedirs(LOG_DIR_PATH)
        with open(LOG_DIR_PATH+"/"+filename,"w") as fileToSave:
            json.dump(jsonToSave,fileToSave)
