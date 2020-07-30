FROM 172.16.101.214:5000/openjdk:8u181-jre-stretch
ENV TZ Asia/Shanghai
VOLUME /tmp
EXPOSE 8080
COPY ./target/*.jar /app.jar
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app.jar"]
