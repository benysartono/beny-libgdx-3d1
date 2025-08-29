package com.beny.libgdx3d1;

import com.beny.libgdx3d1.server.Network;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkClient {

    private Client client;
    private ConcurrentHashMap<Integer, Network.PlayerUpdate> otherPlayers = new ConcurrentHashMap<>();
    private int myId = -1;

    public void connect(String host) throws IOException {
        client = new Client();
        Network.register(client.getKryo());

        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("Connected to server with ID: " + connection.getID());
                myId = connection.getID();
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Network.PlayerUpdate) {
                    Network.PlayerUpdate update = (Network.PlayerUpdate) object;
                    if (update.id != myId) {
                        otherPlayers.put(update.id, update);
                    }
                }
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("Disconnected from server");
                otherPlayers.remove(connection.getID());
            }
        });

        client.start();
        client.connect(5000, host, Network.TCP_PORT, Network.UDP_PORT);
    }

    public void sendMyUpdate(float x, float y, float z, float rotation) {
        if (client != null && client.isConnected()) {
            Network.PlayerUpdate update = new Network.PlayerUpdate();
            update.id = myId;
            update.x = x;
            update.y = y;
            update.z = z;
            update.rotation = rotation;
            client.sendUDP(update);
        }
    }

    public ConcurrentHashMap<Integer, Network.PlayerUpdate> getOtherPlayers() {
        return otherPlayers;
    }
}
