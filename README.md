# zookeeper-bank-2018
Distributed application using Zookeeper

## Objectives
- Managements	the	informa5on	of	the	clients	in	a	bank
- Application	should	fault	tolerance	and	consistency

The aim of this assignment is to develop an application for managing the clients of a bank. A distributed application is developed, in order to provide properties, such as consistency, fault tolerance, replication, and availability. The functionality for the management of clients should be simple for concentrating on these distributed properties.

Clients interact with the server, which maintains the information in the back. The zookeeper ensemble provides the basic means for configuring a membership service, relying on fault detection, and allowing the servers to ensure consistency for the operations requested by the clients.

## Zookeeper

To be able to run this application in a distributed fashion a Zookeeper Ensemble needs to be started. To start a 3-server Zookeeper ensemble, use the following commands: zkServer.sh start ${ZK_HOME}/conf/zoo1.cfg, zkServer.sh start ${ZK_HOME}/conf/zoo2.cfg, zkServer.sh start ${ZK_HOME}/conf/zoo3.cfg.

The ensemble also provides a CLI (/zookeeper-3.4.10/bin/zkCli.sh) which can be used to manage the Zookeeper structure (see Znodes, read data, delete nodes, etc.). To start a client based shell, use the following command: zkCli.sh -server localhost:2181.

Data structure:

![Zookeeper Ensemble](images/znodes.png?raw=true)

## Application

The application servers are really simple Java applications. The database is made of a HashMap data structures, which stores information abour customers (account id, owner and current balance). The set of operations provided by the application are the standard CRUD (Creata, Read, Update, Delete). The application can be run from the command line, providing a basic UI which allows to execute the above-mentioned operations.

Each of the individual application process is tied to Zookeeper. All communication between the application processes is channeled through the Zookeeper ensemble.

## Leadership Election

The algorithm used for choosing the leader amongst all the application servers is very simple: the node with the lowest id will be elected as leader.

## Membership (fault detection)

The Members node is used to enforce Fault Detection. Everytime an application server joins the cluster, an EPHEMERAL_SEQUENTIAL (3) node is created under the members node (/members). The watch that is set on the members node will be triggered when a node goes down (either crashed, or gets stopped). The watcher process is responsible for starting a new application server, thus ensuring fault detection and automatic recovery.

## Consistency

The strategy for ensuring consistency is as follows:

1. Read operations: when a read operation is sent to any node (being it a leader node or a follower), it is processed by the same node;

2. Write operations: when a write operation is received by an application server, we distinguish two cases:
   1. the operation is received by the leader: the application server executes it and forwards it to all the other nodes;
   2. the operation is received by a non leader node: the operation is forwarded from this node to the leader, which will execute it and, then, forward it to all the other nodes.



