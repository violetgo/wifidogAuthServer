#!/bin/bash

FLAGEID=$(ps aux | grep authServer.jar |grep -v grep | grep jar| awk '{print $2}')

if [ "$1" == "start" ];then
        if [ "$FLAGEID" == "" ];then
                nohup java -jar authServer.jar 8000 &
        fi
elif [ "$1" == "stop" ];then
        if [ "$FLAGEID" != "" ];then
                kill -9 "$FLAGEID"
        fi
elif [ "$1" == "restart" ];then
        kill -9 "$FLAGEID"
        nohup java -jar authServer.jar 8000 &
fi