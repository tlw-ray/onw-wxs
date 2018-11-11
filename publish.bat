call gradlew bootJar
call pscp -pw 111111Sese build/libs/onw-wxs-0.0.1.jar root@47.100.37.30:/root/
call pscp -pw 111111Sese keystore.p12 root@47.100.37.30:/root/
