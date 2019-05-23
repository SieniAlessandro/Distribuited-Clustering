from flask import Flask,request, jsonify
from flask_restful import Api,Resource,reqparse
from FCM import FCM
from Utils import *

class Server(Resource):
    def post(self):
        if(request.json['command'] == "Train"):
            return FCM().train(request.json['ID'],request.json["Coeff"],request.json["Window"],request.json['values'])
        elif (request.json['command'] == "Merge"):
            return FCM().merge(int(request.json['nodes']))
        elif (request.json["command"] == "Update"):
            return FCM().update(int(request.json["ID"]))
        else:
            return "Command not available",200
removeOldFiles()
app = Flask(__name__)
api = Api(app)
api.add_resource(Server, '/server')
app.run(debug=True)
