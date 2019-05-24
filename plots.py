import matplotlib.pyplot as plt
import os
from os import path,listdir
from os.path import isfile, join,isdir
import json
import numpy as np
#Plotting the training data
if os.path.exists("dataNodes"):
    #Scrolling all the folders
    dirnames = [f for f in listdir("dataNodes") if isdir(join("dataNodes", f))]
    for dirname in dirnames:
        completeName = "dataNodes/"+dirname
        filenames = [f for f in listdir(completeName) if isfile(join(completeName, f))]
        for filename in filenames:
            if "Plot" not in filename:
                with open(completeName+"/"+filename,"r") as file:
                    dict = json.load(file)
            if "trainResult" in filename and "Plot" not in filename:
                #This is a trining result
                points = np.array(dict["points"])
                centers = np.array(dict["centers"])
                plt.scatter(points[:,0],points[:,1],color="blue",label="Training points")
                plt.scatter(centers[:,0],centers[:,1],color="red",label="Centroids")
                plt.legend(loc='upper left')
                plt.savefig(completeName+"/Plot_"+filename+".png")
                plt.clf()
            elif "updatedPoint" in filename and "Plot" not in filename:
                #This is a trining result
                oldModel = np.array(dict["oldModel"])
                mergedModel = np.array(dict["mergedModel"])
                updatedModel = np.array(dict["updatedPoint"])

                plt.scatter(oldModel[:,0],oldModel[:,1],color="blue",label="Old centroids")
                plt.scatter(mergedModel[:,0],mergedModel[:,1],color="red",label="Merged centroids")
                plt.scatter(updatedModel[:,0],updatedModel[:,1],color="red",label="Updated centroids")
                plt.legend(loc='upper left')
                plt.savefig(completeName+"/Plot_"+filename+".png")
                plt.clf()
if os.path.exists("dataSink"):
    dirnames = [f for f in listdir("dataSink") if isdir(join("dataSink", f))]
    for dirname in dirnames:
        completeName = "dataSink/"+dirname
        filenames = [f for f in listdir(completeName) if isfile(join(completeName, f))]
        for filename in filenames:

            if "Plot" not in filename:
                with open(completeName+"/"+filename,"r") as file:
                    dict = json.load(file)
            if "MergedModel" in filename and "Plot" not in filename:
                #This is a trining result
                newcenters = np.array(dict["newcenters"])
                oldcenters = np.array(dict["oldcenters"])
                plt.scatter(oldcenters[:,0],oldcenters[:,1],color="blue",label="Training centers")
                plt.scatter(newcenters[:,0],newcenters[:,1],color="red",label="Merged centers")
                plt.legend(loc='upper left')
                plt.savefig(completeName+"/Plot_"+filename+".png")
                plt.clf()
