FROM ubuntu:16.04

LABEL "Maintainer"="Shubham Gupta"

WORKDIR /app

RUN apt-get -y update && apt-get -y upgrade
RUN apt-get -y install software-properties-common

RUN apt-get install -y curl wget vim apt-utils net-tools

RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update -y
RUN apt-get install -y openjdk-8-jdk
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64

RUN mkdir -p /home/ubuntu/setup/jars

COPY ./build/libs/coronavirus-tracker-0.0.1-SNAPSHOT.jar /home/ubuntu/setup/jars

RUN cd /home/ubuntu/setup/jars

CMD ["java", "-jar", "/home/ubuntu/setup/jars/coronavirus-tracker-0.0.1-SNAPSHOT.jar"]