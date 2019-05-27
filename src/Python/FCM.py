import pandas as pd
import skfuzzy as fuzz
import numpy as np
import json
from scipy.spatial.distance import cdist
import os
import time
import matplotlib.pyplot as plt
from Utils import save

ERROR_THRESHOLD = 0.005
DISTANCE_THRESHOLD = 30
VALUES_THRESHOLD = 2
MAX_ITER = 1000
NEW_VALUES = -5
MAX_ACCEPTED_OUTLIERS = 1
BASE_MODEL_PATH = "../../dataNodes/newModel"
BASE_DATA_PATH = "../../dataNodes/readyData"
MERGED_MODE_NODE_PATH =  "../../dataNodes/NewUpdatedModel.json"
MERGED_MODEL_PATH = "../../dataSink/mergedModel.json"
BASE_MODEL_SINK_PATH = "../../dataSink/ModelNode"
CLUSTERS = 2

class FCM:
    def associate(self,distance):
        if distance <= 3:
            return 1
        elif 3 < distance <= 5 :
            return 0.75
        elif 5 < distance <= 7 :
            return 0.5
        else:
            return 0
    def merge(self,nodes):
        #Obtaining the centers
        with open(BASE_MODEL_SINK_PATH+"1.json","r") as model:
            centers = np.array(json.load(model)["centers"],dtype=float)
        for i in range(2,nodes+1):
            #Opening the file and concatenating the centers
            with open(BASE_MODEL_SINK_PATH+str(i)+".json","r") as model:
                nodeCntrs = np.array(json.load(model)["centers"],dtype=float)
                centers = np.vstack((centers,nodeCntrs))

        cntr,u_orig, _, _, _, _, _ = fuzz.cluster.cmeans(centers.T,CLUSTERS,2,error=ERROR_THRESHOLD,maxiter = MAX_ITER)
        mergedModel = {}
        mergedModel["centers"] = cntr.tolist()
        #Computing the mean Minumum distance for the new centers from the old centers
        for i in range(1,nodes+1):
            with open(BASE_MODEL_SINK_PATH+str(i)+".json","r") as model:
                oldcntrs = np.array(json.load(model)["centers"])

            indexes = np.argmin(cdist(cntr,oldcntrs,metric='euclidean'),axis=1)
            #Calculating the minimum value along the row
            minDistances = np.amin(cdist(cntr,oldcntrs,metric='euclidean'),axis=1)
            minDistancesIndex =  np.argmin(cdist(cntr,oldcntrs,metric='euclidean'),axis=1)
            #Check if there are at least a repetition
            if(np.unique(minDistancesIndex).shape[0] == minDistancesIndex.shape[0]):
                #Compute the mean of the distances
                meanDistance = np.mean(minDistances)
                mergedModel[str(i)] = self.associate(meanDistance)
            else:
                mergedModel[str(i)] = 0

        jsonToSave = {}
        jsonToSave["newcenters"] = cntr.tolist()
        jsonToSave["oldcenters"] = centers.tolist()
        save(1,0,"MergedModel_"+str(time.time()),jsonToSave)


        #Saving the new Model
        with open(MERGED_MODEL_PATH,"w") as mergedModelFile:
            json.dump(mergedModel,mergedModelFile)
        return "Models merged",201

    def train(self, id,coeff,window,values):
        #Retriving the dataframe related to the generated file
        df = pd.read_csv(BASE_DATA_PATH+id+".txt",names=["X","Y"],header=None,dtype={"X":float,"Y":float})
        #Computing the effective dimension of the window
        dim = int(int(window)*(1+float(coeff)))
        START_WINDOW = dim * (-1);
        if df.shape[0] == int(values):
            result = True
        else:
            #Checking if the model must be computed
            NEW_VALUES = int(values) * -1
            newValues = df[NEW_VALUES:]
            #Deleting from the original dataframe the new values and the previous window
            df = df[:NEW_VALUES]
            [df,result] = self.isModelNeeded(id,df,newValues)
        if(result):
            #Selecting only the desired window
            if (START_WINDOW * (-1)) < df.shape[0]:
                df = df[START_WINDOW:]
            #Training the FCM with the array just obtained
            points = np.array(df)
            cntr,u_orig, _, _, _, _, _ = fuzz.cluster.cmeans(points.T,CLUSTERS,2,error=ERROR_THRESHOLD,maxiter = MAX_ITER)
            #Creating the JSON with the information of the created model
            model = {}
            model["centers"] = cntr.tolist()

            jsonToSave = {}
            jsonToSave["points"] = points.tolist()
            jsonToSave["centers"] = cntr.tolist()
            save(0,int(id),"trainResult"+str(id)+"_"+str(time.time()),jsonToSave)


            #Saving the JSON in the file
            with open(BASE_MODEL_PATH+id+".json","w") as newModelFile:
                newModelFile.write(json.dumps(model))

            #Returning the OK code
            return "Model created",201
        else:
            return "",204
    def isModelNeeded(self,id,df,df2):
        if os.path.isfile(BASE_MODEL_PATH+id+".json"):
            with open(BASE_MODEL_PATH+id+".json","r") as modelFile:
                #Load the centers from the model saved in the file
                centers = np.array(json.load(modelFile)["centers"])
                #Compute the distance between the new point and each center and find
                #the minimum distance for each new value
                minDistances = np.amin(cdist(np.array(df2.values),centers,metric='euclidean'),axis=1)
                #Finding the correct points and the outliers
                correct = (minDistances <= DISTANCE_THRESHOLD)
                outliers = np.invert(correct)
                #Creating a dataframe from that tuples
                df = pd.concat([df,df2.loc[correct]])
                #Writing on file the new dataframe
                df.to_csv(BASE_DATA_PATH+id+".txt",index = None,header = None)
                #checking if the number of outliers is above the threshold
                if df2.loc[outliers].shape[0] <= int(df2.shape[0] * 0.5):
                    return df,True
                else:
                    return df,False
        else:
            return df,True
    def update(self,id):
        with open(BASE_MODEL_PATH+str(id)+".json","r") as oldModelFile:
            oldModel = np.array(json.load(oldModelFile)['centers'])
        with open(MERGED_MODE_NODE_PATH,"r") as mergedModelFile:
            dict = json.load(mergedModelFile)
            mergedModel = np.array(dict['centers'])
            weight = float(dict[str(id)])
        distances = cdist(mergedModel,oldModel,metric="euclidean")
        minDistancesIndex = np.argmin(distances,axis=1)
        updatedPoint = []
        for i in range (0,mergedModel.shape[0]):
            IncrementX = float(mergedModel[i,0])*weight + float(oldModel[minDistancesIndex[i],0])*(1-weight)
            IncrementY = float(mergedModel[i,1])*weight + float(oldModel[minDistancesIndex[i],1])*(1-weight)
            updatedPoint.append([IncrementX,IncrementY])

        jsonToSave = {}
        jsonToSave["oldModel"] = oldModel.tolist()
        jsonToSave["mergedModel"] = mergedModel.tolist()
        jsonToSave["updatedPoint"] = updatedPoint
        save(0,int(id),"updatedPoint"+str(id)+"_"+str(time.time()),jsonToSave)

        dict = {}
        dict["centers"] = updatedPoint
        with open(BASE_MODEL_PATH+str(id)+".json","w") as updatedModelFile:
            json.dump(dict,updatedModelFile)
        return "Model updated",200
