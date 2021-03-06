package com.remcokortekaas.app.watchers;

import com.remcokortekaas.app.Bank;
import com.remcokortekaas.app.Client;
import com.remcokortekaas.app.OperationBank;
import com.remcokortekaas.app.OperationEnum;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.UnsupportedEncodingException;

public class NewNodeWatcher implements Watcher {

    private ZooKeeper zk;
    private Bank bank;

    public NewNodeWatcher(ZooKeeper zkInstance, Bank bank){
        this.zk = zkInstance;
        this.bank = bank;
    }
    @Override
    public void process(WatchedEvent event) {
        System.out.println("New node: " + event.getPath());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Stat stat;
        String operationNodeName = null;
        try {
            stat = zk.exists(event.getPath(), false);
            operationNodeName = new String(zk.getData(event.getPath(), false, stat), "UTF-8");
        } catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // send current state (db) to the new node
        for (java.util.HashMap.Entry <Integer, Client>  entry : bank.getClientDB().clientDB.entrySet()) {
            Client c = entry.getValue();

            System.out.println("Customer: " + c);

            bank.sendMessages.forwardOperationToNode(new OperationBank(
                        OperationEnum.CREATE_CLIENT,
                        new Client(c.getAccountNumber(), c.getName(), c.getBalance())
                    ), operationNodeName);
        }

        NodeDownWatcher nodeDownWatcher = new NodeDownWatcher();
        String nodeId = event.getPath();
        try {
            zk.exists(nodeId, nodeDownWatcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
