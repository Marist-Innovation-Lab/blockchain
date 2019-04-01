#FileName: essence.py
#Author:   Daniel N. Gisolfi
#Purpose:  To make calls and interact with the Essence API
#Date:     2018.7.5

import requests, json, settings, os

def callEssenceAPI(method, command, Peer):
    url = settings.ipAddress +':'+ str(Peer) + '/'
    try:
        if method == 'GET':
            # GET with params in URL
             url += command
             r = requests.get(url)
        else:
            r = requests.get(url)
            essenceLog('ESSENCE: Connection Successful')
            return 'Connection Successful'
        return r.json()
    except requests.exceptions.RequestException as err:
        print(err)
        return err



def updateMap(Peer):
    essenceLog('Connecting to Peer on Port:' + str(Peer))
    examinedPeers = []
    unExaminedPeers = [Peer]
    nodes = []
    links = []

    try:
        for peer in unExaminedPeers:
            #Get the known friends of a peer
            resp = requests.get(settings.ipAddress +':'+ str(peer) + '/friends')
            #Add all known friends of the peer to the "not read" list
            for friend in findFriends(resp.json()):
                if friend in unExaminedPeers:
                    links.append('{"source":' +  str(peer) + ', "target":' + str(friend) + '}')
                    continue
                else:
                    #Add links to list
                    unExaminedPeers.append(friend)
                    links.append('{"source":' +  str(peer) + ', "target":' + str(friend) + '}')

            # if peer not in examinedPeers:
            examinedPeers.append(peer)

            #Add nodes to list
            nodes.append(peer)
    except:
        essenceLog('ERROR Connection to at least one peer within the network has Failed. Please ensure all peers are running.')
        return 'ERROR Connection to at least one peer within the network has Failed. Please ensure all peers are running.'


    fileWrite = createD3file(nodes, links)
    if fileWrite == True:
        essenceLog('MAP Status: Map Rendered')
        return 'Map Rendered'
    elif fileWrite == False:
        essenceLog('MAP Status: ERROR File writing failure')
        return 'ERROR File writing failure'


def findFriends(friendData):
    friends = []
    index = 0
    for each in friendData:
        friends.append(friendData[index]['port'])
        index += 1
    return friends

def createD3file(nodes, links):
    try:
        file = open('./src/EssenceNetworkViewer/src/static/' + settings.graphData, 'w')
    except:
        file = open('./src/EssenceNetworkViewer/src/static/' + settings.graphData, 'a+')
  

    # try:
    nodeIndex = 0
    file.write('{\n"nodes":[\n')
    for node in nodes:
        if nodeIndex == (len(nodes) - 1):
            if node == settings.default_peer:
                file.write('{"port": ' + str(nodes[nodeIndex])
                + ', "entrypoint": "true"}')
            else:
                file.write('{"port": ' + str(nodes[nodeIndex])
                + ', "entrypoint": "false"}')
        else:
            if node == settings.default_peer:
                file.write('{"port": ' + str(nodes[nodeIndex])
                + ', "entrypoint": "true"},\n')
            else:
                file.write('{"port": ' + str(nodes[nodeIndex])
                + ', "entrypoint": "false"},\n')
        nodeIndex += 1

    file.write('\n],\n"links": [\n')

    linkIndex = 0
    for link in links:
        if linkIndex == (len(links) - 1):
            file.write(str(links[linkIndex]) + '\n')
        else:
            file.write(str(links[linkIndex]) + ',\n')
        linkIndex += 1

    file.write(']\n}')
    # except:
    #     return False

    file.close()
    return True

def essenceLog(msg):
    try:
        fileName = 'Essence.log'
        if os.path.exists(fileName):
            file = open(fileName, 'a')
            msg += '\n'
        else:
            file = open(fileName, 'w')
        file.write(msg)
        file.close()
    except:
        print('ERR LOGGING FAILED')