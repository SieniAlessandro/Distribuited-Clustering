import pandas as pd
import skfuzzy as fuzz
import numpy as np
import json
from scipy.spatial.distance import cdist
import os

ERROR_THRESHOLD = 0.005
DISTANCE_THRESHOLD = 0
VALUES_THRESHOLD = 2
MAX_ITER = 1000
NEW_VALUES = -5
MAX_ACCEPTED_OUTLIERS = 1
MODEL_PATH = "../data/newModel.json"
DATA_PATH = "../data/readyData.txt"


class FCM:
    def merge(self):
        return "Models merged",200
    def train(self):
        #Retriving the dataframe related to the generated file
        df = pd.read_csv(DATA_PATH,names=["X","Y"],header=None,dtype={"X":float,"Y":float})
        print("CSV ORIGINALE: "+str(df.shape))
        #Checking if the model must be computed
        newValues = df[NEW_VALUES:]
        #Deleting from the original dataframe the new values
        df = df[:NEW_VALUES]
        print("[DEBUG] Old Dataframe shape :"+str(df.shape))
        [df,result] = self.isModelNeeded(df,newValues)
        print("[DEBUG] New Dataframe shape without outliers: "+str(df.shape))
        if(result):
            #Training the FCM with the array just obtained
            cntr,u_orig, _, _, _, _, _ = fuzz.cluster.cmeans(np.array(df).T,2,2,error=ERROR_THRESHOLD,maxiter = MAX_ITER)
            #Creating the JSON with the information of the created model
            model = {}
            model["centers"] = cntr.tolist()
            model["coefficentsMatrix"] = u_orig.tolist()
            #Saving the JSON in the file
            with open(MODEL_PATH,"w") as newModelFile:
                newModelFile.write(json.dumps(model))
            #Returning the OK code
            return "Model created",201
        else:
            return "",204
    def isModelNeeded(self,df,df2):
        if os.path.isfile(MODEL_PATH):
            with open(MODEL_PATH,"r") as modelFile:
                #Load the centers from the model saved in the file
                centers = np.array(json.load(modelFile)["centers"])
                #Compute the distance between the new point and each center and find
                #the minimum distance for each new value
                minDistances = np.amin(cdist(np.array(df2.values),centers,metric='euclidean'),axis=1)
                #Finding the correct points
                correct = minDistances <= DISTANCE_THRESHOLD
                #Creating a dataframe from that tuples
                df = pd.concat([df,df2[correct]])
                #Writing on file the new dataframe
                df.to_csv(DATA_PATH,index = None,header = None)
                #checking if the number of outliers is above the threshold
                if(df2[np.invert(correct)].shape[0] <= MAX_ACCEPTED_OUTLIERS ):
                    return df,True
                else:
                    return df,False
        else:
            return df,False
