from flask import Flask,request, jsonify
from flask_restful import Api,Resource,reqparse
from FCM import FCM
import skfuzzy as fuzz


class Server(Resource):
    def post(self):
        if(request.json['command'] == "Train"):
            return FCM().train()
        elif (request.json['command'] == "Debug"):
            return postHandler().debug()
        else:
            return "Command not available",404
app = Flask(__name__)
api = Api(app)
api.add_resource(Server, '/server')
app.run(debug=True)
