from flask import Flask,request, jsonify
from flask_restful import Api,Resource,reqparse
from FuzzyKNN import FuzzyKNN
import pprint
import operator
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.datasets import load_iris, load_breast_cancer
from sklearn.metrics import accuracy_score
from sklearn.neighbors import KNeighborsClassifier
from sklearn.base import BaseEstimator, ClassifierMixin
import operator
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.metrics import accuracy_score
from sklearn.base import BaseEstimator, ClassifierMixin

class Server(Resource):
    def post(self):
        if(request.json['command'] == "Start"):
            iris = load_iris()
            breast = load_breast_cancer()
            dataset = iris
            X = dataset.data
            y = dataset.target
            xTrain, xTest, yTrain, yTest = train_test_split(X,y)
            skModel = KNeighborsClassifier()
            custModel = FuzzyKNN()
            custModel.fit(xTrain, yTrain)
            return str(cross_val_score(cv=5, estimator=custModel, X=xTest, y=yTest)),200
        elif (request.json['command'] == "Eval"):
            return "Start Evaluating",200
        elif (request.json['command'] == "Obtain"):
            return "Returning Model",200
        else:
            return "Command not available",404

app = Flask(__name__)
api = Api(app)
api.add_resource(Server, '/server')
app.run(debug=True)
