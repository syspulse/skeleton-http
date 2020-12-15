#!/bin/bash

docker ps -a|grep auth-otp|awk '{print $1}'|xargs docker rm
docker images|grep auth-otp|awk '{print $1}'|xargs docker rmi
