package com.github.dougefr;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/chat/{username}")
@ApplicationScoped
public class ChatSocket {

    private static final Logger LOG = Logger.getLogger(ChatSocket.class);
    private final Map<String, Session> sessions = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        sessions.put(username, session);
        broadcast(String.format("User %s joined", username));
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(username);
        broadcast(String.format("User %s left", username));
    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        sessions.remove(username);
        broadcast(String.format("User %s left on error: %s", username, throwable));
    }

    @OnMessage
    public void onMessage(String message, @PathParam("username") String username) {
        broadcast(String.format(">> %s: %s", username, message));
    }

    private void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message, result -> {
                if(result.getException() != null) {
                    LOG.error("Unable to send message", result.getException());
                }
            });
        });
    }
}
