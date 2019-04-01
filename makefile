# Essence
# Author: Daniel Nicolas Gisolfi

JAR=EssenceAPI.jar

clean:
	@pkill -f $(JAR)
	@rm nohup.out
	@rm -R blockchain*

create_sample_network:
	@chmod +x ./out/bootPeers.sh
	@./out/bootPeers.sh
	@jps -lv

kill_sample_network:
	@pkill -f $(JAR)