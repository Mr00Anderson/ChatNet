import Packets.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerMain extends Listener {

    private HashMap<String, Connection> clients = new HashMap<String, Connection>();
   public  Server server ;

    public static void main(String[] args){
        Log.set(Log.LEVEL_ERROR);
        new ServerMain().init();

    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof Packet){

            if(object instanceof PacketConnect){

                PacketConnect p1 = (PacketConnect)object;
                clients.put(p1.username, connection);
                PacketClientConnected p = new PacketClientConnected();
                p.clientName = p1.username;
                server.sendToAllExceptTCP(connection.getID(), p);
                System.out.println(p1.username + " connected");
            }else if(object instanceof PacketClientDisconnect){
                PacketClientDisconnect p2 = (PacketClientDisconnect)object;
                server.sendToAllExceptTCP(clients.get(p2.clientname).getID(), p2);

                clients.remove(p2.clientname);
            }else if(object instanceof PacketChat){

                PacketChat chat = (PacketChat)object;
                server.sendToAllExceptTCP(connection.getID(), chat);
            }

        }

    }

    @Override
    public void disconnected(Connection connection) {
        PacketClientDisconnect p2 = new PacketClientDisconnect();
        Iterator it = clients.entrySet().iterator();
        String username = "";
        while(it.hasNext()){
            Map.Entry pairs = (Map.Entry)it.next();
            if(pairs.getValue() == connection){
                username = (String)pairs.getKey();
                break;
            }
        }
        if(!username.equalsIgnoreCase("")) {
            p2.clientname = username;
            server.sendToAllExceptTCP(clients.get(p2.clientname).getID(), p2);
        }
    }


    public void init(){
        server = new Server();

        server.start();
        try {
            server.bind(14300,14300);
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
