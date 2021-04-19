FROM adoptopenjdk/openjdk11:alpine
ARG JAR_FILE=build/libs/*all.jar
COPY ${JAR_FILE} credicard.jar
ENTRYPOINT ["java", "-Xmx512m","-jar","/credicard.jar"]