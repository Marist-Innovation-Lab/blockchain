# blockchain
The Marist Joint Study implementation of blockchain and its network viewing tools.

### Authors

**Tom Magnusson** - *Essence* - [tommagnusson](https://github.com/tommagnusson)

**Daniel Gisolfi** - *Network Viewer* - [dgisolfi](https://github.com/dgisolfi)

## Essence

Essence is an implementation of a blockchain peer in java. All source code for the peer is located in the `/src/edu/marist/jointstudy/essence` directory. To run an instance of the Essence API the jar file located in the `/out` directory can be used. As an example of chaining these peers into a network, located in the same directory is a bash script which will run nine peers on the host machine all connected together within a single network.

To run the sample network use the makefile located in the root of the directory. Run the following make target:

```
make create_sample_network
```

## Essence Network Viewer 

When a network of peers is running, a simple visualization of the peers can be built by running the Essence network Viewer. To run the visualiser, run the following make target:

```
make network_viewer
```

Once running visit [localhost:5000](http://localhost:5000/) to see the undirected graph of the network layout.
