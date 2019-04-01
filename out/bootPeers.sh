#!/bin/bash
# Essence Startup
# Author: Daniel Nicolas Gisolfi


APP="./out/EssenceAPI.jar"

#####################
# Boot up all peers	#
#####################

# Example Network
nohup java -jar ${APP} 9090 http://127.0.0.1:9096 http://127.0.0.1:9092 &
nohup java -jar ${APP} 9091 http://127.0.0.1:9096 http://127.0.0.1:9098 &
nohup java -jar ${APP} 9092 http://127.0.0.1:9090 http://127.0.0.1:9096 &
nohup java -jar ${APP} 9093 http://127.0.0.1:9096 http://127.0.0.1:9094 &
nohup java -jar ${APP} 9094 http://127.0.0.1:9093 http://127.0.0.1:9096 http://127.0.0.1:9095 &
nohup java -jar ${APP} 9095 http://127.0.0.1:9097 http://127.0.0.1:9094 &
nohup java -jar ${APP} 9096 http://127.0.0.1:9091 http://127.0.0.1:9090 http://127.0.0.1:9092 http://127.0.0.1:9093 http://127.0.0.1:9094 http://127.0.0.1:9097 &
nohup java -jar ${APP} 9097 http://127.0.0.1:9098 http://127.0.0.1:9095 http://127.0.0.1:9096 &
nohup java -jar ${APP} 9098 http://127.0.0.1:9091 http://127.0.0.1:9097 &
