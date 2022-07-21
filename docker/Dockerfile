FROM tomcat:jdk11-corretto

COPY ./build/libs /usr/local/tomcat/webapps

EXPOSE 8080
CMD ["catalina.sh", "run"]
