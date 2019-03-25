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
from FuzzyKNN import FuzzyKNN

class postHandler:
    def start(self):
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
    def eval(self):
        return "Start Evaluating",200
