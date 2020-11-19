package io.github.sparkastic.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.sun.tools.jdi.Packet;
import io.github.sparkastic.packets.PacketChat;
import io.github.sparkastic.packets.PacketClientConnected;
import io.github.sparkastic.packets.PacketClientDisconnect;
import io.github.sparkastic.packets.PacketConnect;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerMain extends Listener {
    private HashMap<String, Connection> clients = new HashMap<>();
    public Server server;

    public static void main(String[] args) {
        Log.set(Log.LEVEL_ERROR);
        new ServerMain().init();

    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof PacketConnect) {
            PacketConnect p1 = (PacketConnect) object;
            clients.put(p1.username, connection);
            PacketClientConnected p = new PacketClientConnected();
            p.clientName = p1.username;
            server.sendToAllExceptTCP(connection.getID(), p);
            System.out.println(p1.username + " connected");
        } else if (object instanceof PacketClientDisconnect) {
            PacketClientDisconnect p2 = (PacketClientDisconnect) object;
            server.sendToAllExceptTCP(clients.get(p2.clientname).getID(), p2);

            clients.remove(p2.clientname);
        } else if (object instanceof PacketChat) {
            PacketChat chat = (PacketChat) object;
            server.sendToAllExceptTCP(connection.getID(), chat);
        }
    }

    @Override
    public void disconnected(Connection connection) {
        PacketClientDisconnect p2 = new PacketClientDisconnect();
        String username = "";

        for (Map.Entry<String, Connection> pairs : clients.entrySet()) {
            if (pairs.getValue() == connection) {
                username = pairs.getKey();
                break;
            }
        }

        if (!username.equalsIgnoreCase("")) {
            p2.clientname = username;
            server.sendToAllExceptTCP(clients.get(p2.clientname).getID(), p2);
        }
    }


    public void init() {
        server = new Server();

        server.start();
        try {
            server.bind(14300, 14300);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.addListener(this);

        server.getKryo().register(Packet.class);
        server.getKryo().register(PacketConnect.class);
        server.getKryo().register(PacketClientConnected.class);
        server.getKryo().register(PacketClientDisconnect.class);
        server.getKryo().register(PacketChat.class);
    }
}
