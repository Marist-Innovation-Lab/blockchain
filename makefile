# Essence
# Author: Daniel Nicolas Gisolfi

JAR=EssenceAPI.jar
VERSION=1.0
IMAGE=essence-network-map
CONTAINER=network_viewer

all: intro clean create_sample_network docker_network_viewer

intro:
	@echo "\n					ESSENCE v$(VERSION)"

clean:
	# Cleanup Peer network
	-pkill -f $(JAR)
	-rm nohup.out
	-rm -R blockchain*
	# Cleanup docker/network viewer
	-rm *.log
	-docker rm $(CONTAINER)
	-docker rmi $(IMAGE)

create_sample_network: intro
	@chmod +x ./out/bootPeers.sh
	@./out/bootPeers.sh
	@jps -lv

kill_sample_network:
	@pkill -f $(JAR)

init:
	@python3 -m pip install -r  ./src/EssenceNetworkViewer/requirements.txt

network_viewer: intro init
	@python3 ./src/EssenceNetworkViewer/src/server.py
	
network_viewer_image:
	@echo "\n				Creating Essence Viewer Docker image"
	@docker build -t essence-network-map ./src/EssenceNetworkViewer/.

docker_network_viewer: intro network_viewer_image
	@docker run --rm --name $(CONTAINER) -p5000:5000 $(IMAGE)