import argparse
import os
import shutil
import subprocess
from time import sleep

parser = argparse.ArgumentParser(description='Run a pool of nodes for testing purpose')
parser.add_argument("poolSize", help='number of nodes to run')
parser.add_argument("ipRabbit",help="IP address of the rabbit mq server")
args = parser.parse_args()

pidList = []

for node in range(0,int(args.poolSize)):
    pidList.append(subprocess.Popen(["java", "-jar", "DataCollector.jar",args.ipRabbit], shell=True))
