FROM openjdk:11

COPY ./build/libs /opt

WORKDIR /opt
CMD ["java", "-jar", "sender.jar", "org.example.sender.SenderApplication"]
