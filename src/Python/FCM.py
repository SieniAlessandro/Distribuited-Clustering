import pandas as pd
import skfuzzy as fuzz
import numpy as np
import json
ERROR_THRESHOLD = 0.005
MAX_ITER = 1000
class FCM:
    def merge(self):
        return "Models merged",200
    def train(self):
        #Retriving the dataframe related to the generated file
        df = pd.read_csv("../data/readyData.txt",names=["X","Y"],header=None,dtype={"X":float,"Y":float})
        #Generating the numpy array from the dataframe in order to use it in the FCM
        values = np.array(df.values)
        print("-----------------------------------------------------------------")
        print(values)
        print("-----------------------------------------------------------------")
        #Training the FCM with the array just obtained
        cntr,u_orig, _, _, _, _, _ = fuzz.cluster.cmeans(values.T,2,2,error=ERROR_THRESHOLD,maxiter = MAX_ITER)
        #Creating the JSON with the information of the created model
        model = {}
        model["centers"] = cntr.tolist()
        model["coefficentsMatrix"] = u_orig.tolist()
        #Saving the JSON in the file
        with open("../data/newModel.json","w") as newModelFile:
            newModelFile.write(json.dumps(model))
        #Returning the OK code
        return "Model created",200
