cd ../../../
call gradlew bootJar
REM call pscp -P 27018 -pw pFkVUq6urYFi build/libs/onw-wxs-0.0.1.jar root@97.64.126.87:/root/task03_onw/
call pscp -P 27018 -pw @welcome1017 build/libs/onw-wxs-0.0.1.jar root@106.12.13.244:/root/onw/
