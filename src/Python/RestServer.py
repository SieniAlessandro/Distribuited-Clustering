from flask import Flask,request, jsonify
from flask_restful import Api,Resource,reqparse
from FCM import FCM
import skfuzzy as fuzz


class Server(Resource):
    def post(self):
        if(request.json['command'] == "Train"):
            return FCM().train()
        # elif (request.json['command'] == "Update"):
            # with open("../data/sink/MergedModel.json","r") as f:
        elif (request.json['command'] == "Merge"):
            with open("../data/sink/MergedModel.json","w") as f:
                f.write("ciao")
            return FCM().merge()
        else:
            return "Command not available",404
app = Flask(__name__)
api = Api(app)
api.add_resource(Server, '/server')
app.run(debug=True)
