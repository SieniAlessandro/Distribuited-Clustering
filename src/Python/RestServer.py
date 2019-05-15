from flask import Flask,request, jsonify
from flask_restful import Api,Resource,reqparse
from FCM import FCM
import skfuzzy as fuzz


class Server(Resource):
    def post(self):
        print("Gestione Post")
        if(request.json['command'] == "Train"):
            return FCM().train(request.json['ID'],request.json['values'])
        elif (request.json['command'] == "Merge"):
            with open("../../dataSink/MergedModel.json","w") as f:
                print(request.json['nodes'])
            return FCM().merge()
        else:
            return "Command not available",200
app = Flask(__name__)
api = Api(app)
api.add_resource(Server, '/server')
app.run(debug=True)
