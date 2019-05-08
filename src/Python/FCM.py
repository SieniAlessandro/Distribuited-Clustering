import pandas as pd
import skfuzzy as fuzz
import numpy as np
import json
from scipy.spatial.distance import cdist
import os
ERROR_THRESHOLD = 0.005
DISTANCE_THRESHOLD = 4
VALUES_THRESHOLD = 2
MAX_ITER = 1000
NEW_VALUES = -5
MODEL_PATH = "../data/newModel.json"
class FCM:
    def merge(self):
        return "Models merged",200
    def train(self):
        #Retriving the dataframe related to the generated file
        df = pd.read_csv("../data/readyData.txt",names=["X","Y"],header=None,dtype={"X":float,"Y":float})
        #Generating the numpy array from the dataframe in order to use it in the FCM
        values = np.array(df.values)
        #Checking if the model must be computed
        new_values = values[NEW_VALUES:]
        #Training the FCM with the array just obtained
        cntr,u_orig, _, _, _, _, _ = fuzz.cluster.cmeans(values.T,2,2,error=ERROR_THRESHOLD,maxiter = MAX_ITER)
        #Creating the JSON with the information of the created model
        model = {}
        model["centers"] = cntr.tolist()
        model["coefficentsMatrix"] = u_orig.tolist()
        #Saving the JSON in the file
        with open(MODEL_PATH,"w") as newModelFile:
            newModelFile.write(json.dumps(model))
        #Returning the OK code
        return "Model created",200
    def isModelNeeded(self,values):
        fileExists = os.path.isfile(MODEL_PATH)
        if fileExists:
            with open(MODEL_PATH,"r") as modelFile:
                centers = np.array(json.load(modelFile)["centers"])
                minDistances = np.amin(cdist(values,centers,metric='euclidean'),axis=1)
                print(minDistances)
                print(np.count_nonzero(minDistances[minDistances > DISTANCE_THRESHOLD]))
            return False
        else:
            return True


if __name__ == "__main__":
    print("Prova train")
    FCM().isModelNeeded(np.array([[2,3],[1,2],[2,4]]))
