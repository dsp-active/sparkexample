FROM java:8

WORKDIR /code
ADD pom.xml /code/pom.xml
ADD src /code/src

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/* && ["mvn", "dependency:resolve"] && ["mvn", "verify"] && ["mvn", "package"]

EXPOSE 4567
CMD ["/usr/lib/jvm/java-1.11.0-openjdk-amd64/bin/java", "-jar", "target/spark-jar-with-dependencies.jar"]
