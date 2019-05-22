import pandas as pd
import skfuzzy as fuzz
import numpy as np
import json
from scipy.spatial.distance import cdist
import os
import time
import matplotlib.pyplot as plt
import itertools
ERROR_THRESHOLD = 0.005
DISTANCE_THRESHOLD = 100
VALUES_THRESHOLD = 2
MAX_ITER = 1000
NEW_VALUES = -5
MAX_ACCEPTED_OUTLIERS = 1
BASE_MODEL_PATH = "../../dataNodes/newModel"
BASE_DATA_PATH = "../../dataNodes/readyData"
MERGED_MODE_NODE_PATH =  "../../dataNodes/NewUpdatedModel.json"
PREPARED_MODEL = "../../DataSink/preparedModel.json"
MERGED_MODEL_PATH = "../../DataSink/mergedModel.json"
BASE_MODEL_SINK_PATH = "../../DataSink/ModelNode"


class FCM:
    def associate(self,distance):
        if distance <= 0.5:
            return 1
        elif 0.5 < distance <= 1 :
            return 0.75
        elif 1 < distance <= 2 :
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
        print(centers)
        print(centers.shape)

        cntr,u_orig, _, _, _, _, _ = fuzz.cluster.cmeans(centers.T,2,2,error=ERROR_THRESHOLD,maxiter = MAX_ITER)
        mergedModel = {}
        mergedModel["centers"] = cntr.tolist()
        #Computing the mean Minumum distance for the new centers from the old centers
        for i in range(1,nodes+1):
            print("------------- NODO "+str(i)+"--------------------------")
            with open(BASE_MODEL_SINK_PATH+str(i)+".json","r") as model:
                oldcntrs = np.array(json.load(model)["centers"])
            indexes = np.argmin(cdist(cntr,oldcntrs,metric='euclidean'),axis=1)
            print(cdist(cntr,oldcntrs,metric='euclidean'))
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

        with open(MERGED_MODEL_PATH,"w") as mergedModelFile:
            json.dump(mergedModel,mergedModelFile)
        return "Models merged",201

    def train(self, id,coeff,window,values):
        #Retriving the dataframe related to the generated file
        df = pd.read_csv(BASE_DATA_PATH+id+".txt",names=["X","Y"],header=None,dtype={"X":float,"Y":float})
        print("CSV ORIGINALE: "+str(df.shape))
        dim = int(int(window)*(1+float(coeff)))
        #Checking if the model must be computed
        NEW_VALUES = int(values) * -1
        newValues = df[NEW_VALUES:]
        #Deleting from the original dataframe the new values and the previous window
        START_WINDOW = dim * (-1);
        df = df[:NEW_VALUES]
        print("Dimensione Dataset senza nuovi valori con finestra: "+ str(df.shape))
        print("[DEBUG] Old Dataframe shape :"+str(df.shape))
        [df,result] = self.isModelNeeded(id,df,newValues)
        print("[DEBUG] New Dataframe shape without outliers: "+str(df.shape))
        if(result):
            #Selecting only the desired window
            df = df[START_WINDOW:]
            print("DIMENSION OF THE DATASET ANALYZED: "+ str(df.shape))
            #Training the FCM with the array just obtained
            cntr,u_orig, _, _, _, _, _ = fuzz.cluster.cmeans(np.array(df).T,2,2,error=ERROR_THRESHOLD,maxiter = MAX_ITER)
            #Creating the JSON with the information of the created model
            model = {}
            model["centers"] = cntr.tolist()
            model["timestamp"] = time.time()
            #model["coefficentsMatrix"] = u_orig.tolist()
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
                correct = minDistances <= DISTANCE_THRESHOLD
                outliers = np.invert(correct)
                #Creating a dataframe from that tuples
                df = pd.concat([df,df2[correct]])
                #Writing on file the new dataframe
                df.to_csv(BASE_DATA_PATH+id+".txt",index = None,header = None)
                #checking if the number of outliers is above the threshold
                if(df2[outliers].shape[0] <= MAX_ACCEPTED_OUTLIERS ):
                    return df,True
                else:
                    return df,False
        else:
            return df,True
    def update(self,id):
        with open(BASE_MODEL_PATH+str(id)+".json","r") as oldModelFile:
            oldModel = np.array(json.load(oldModelFile)['centers'])
        print(oldModel)
        with open(MERGED_MODE_NODE_PATH,"r") as mergedModelFile:
            dict = json.load(mergedModelFile)
            mergedModel = np.array(dict['centers'])
            weight = float(dict[str(id)])
        distances = cdist(mergedModel,oldModel,metric="euclidean")
        minDistancesIndex = np.argmin(distances,axis=1)
        print(minDistancesIndex)
        updatedPoint = []
        print(mergedModel.shape)
        for i in range (0,mergedModel.shape[0]):
            print("Step: " +str(weight))
            IncrementX = float(mergedModel[i,0])*weight + float(oldModel[minDistancesIndex[i],0])*(1-weight)
            IncrementY = float(mergedModel[i,1])*weight + float(oldModel[minDistancesIndex[i],1])*(1-weight)
            updatedPoint.append([IncrementX,IncrementY])
        a = np.array(updatedPoint)

        #print("Point nearest 2:"+str(mergedModel[1])+"-"+str(oldModel[minDistancesIndex[1]]))
        plt.scatter(oldModel[:,0],oldModel[:,1], color="red")
        plt.scatter(mergedModel[:,0],mergedModel[:,1],color="blue")
        plt.scatter(a[:,0],a[:,1], color="green")
        plt.savefig("plot"+str(id)+".png")
        plt.clf()
        #print(distances)
        dict = {}
        dict["centers"] = updatedPoint
        with open(BASE_MODEL_PATH+str(id)+".json","r") as updatedModelFile:
            json.dump(dict,updatedModelFile)
        return "Model updated",200
