from flask import Flask,request, jsonify
from flask_restful import Api,Resource,reqparse
from postHandler import postHandler

class Server(Resource):
    def post(self):
        if(request.json['command'] == "Start"):
            return postHandler().start()
        elif (request.json['command'] == "Eval"):
            return postHandler().eval()
        elif (request.json['command'] == "Debug"):
            return postHandler().debug()
        else:
            return "Command not available",404

app = Flask(__name__)
api = Api(app)
api.add_resource(Server, '/server')
app.run(debug=True)
