package com.beny.libgdx3d1;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class GameClient extends WebSocketClient {

    private volatile String latestMessage;  // only keep the newest message
    
	public GameClient(String serverUri) {
        super(URI.create(serverUri)); // no Exception handling needed
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        // just keep the latest JSON string
        latestMessage = message;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Error: " + ex.getMessage());
    }
    
    public String getLatestMessage() {
        String msg = latestMessage;
        latestMessage = null; // clear after reading
        return msg;
    }
}
