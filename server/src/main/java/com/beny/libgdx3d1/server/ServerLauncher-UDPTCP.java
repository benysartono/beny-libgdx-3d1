package com.beny.libgdx3d1.server;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ServerLauncher {

    private Server server;
    private ConcurrentHashMap<Integer, Network.PlayerUpdate> players = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        new ServerLauncher().start();
    }

    public void start() throws IOException {
        server = new Server();
        Network.register(server.getKryo());

        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                System.out.println("Player connected: " + connection.getID());
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Network.PlayerUpdate) {
                    Network.PlayerUpdate update = (Network.PlayerUpdate) object;
                    update.id = connection.getID();

                    // Save state
                    players.put(connection.getID(), update);

                    // Broadcast to all clients
                    server.sendToAllExceptTCP(connection.getID(), update);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("Player disconnected: " + connection.getID());
                players.remove(connection.getID());
            }
        });

        server.bind(Network.TCP_PORT, Network.UDP_PORT);
        server.start();
        System.out.println("Server started on TCP " + Network.TCP_PORT + ", UDP " + Network.UDP_PORT);
    }
}
