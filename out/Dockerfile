FROM openjdk:8-jdk
MAINTAINER Daniel Gisolfi
WORKDIR /
COPY ./EssenceAPI.jar EssenceAPI.jar
COPY ./bootPeers.sh bootPeers.sh
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjfx \
    && rm -rf /var/lib/apt/lists/*

CMD [ "./bootPeers.sh" ]