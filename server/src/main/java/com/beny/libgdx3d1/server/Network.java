package com.beny.libgdx3d1.server;

public class Network {
    public static final int TCP_PORT = 54555;
    public static final int UDP_PORT = 54777;

    // Register all packet/message classes
    public static void register(com.esotericsoftware.kryo.Kryo kryo) {
        kryo.register(PlayerUpdate.class);
        kryo.register(java.util.ArrayList.class);
    }

    // Example packet: player position update
    public static class PlayerUpdate {
        public int id;
        public float x, y, z;
        public float rotation;

        public PlayerUpdate() {} // Required for Kryo
    }
}
