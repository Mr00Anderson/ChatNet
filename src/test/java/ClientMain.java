import Packets.*;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;


public class ClientMain extends Listener implements ActionListener{

    private static JFrame frame = new JFrame("Chat Client");
    private static JTextArea textArea = new JTextArea();
    private static JTextField textField = new JTextField(40);
    private static JButton sendButton = new JButton("Send");

    //
    private String name;
    private  Client client;

    public ClientMain() {
        client = new Client();
        while(name == null || name.length() == 0) {
            name = JOptionPane.showInputDialog(null, "Enter your name :D", "Entername", JOptionPane.QUESTION_MESSAGE);
        }
        Log.set(Log.LEVEL_ERROR);



        long current = System.currentTimeMillis();
        boolean connecting = true;

        client.start();
        while (connecting) {
            try {
                client.connect(5000, "localhost", 14300, 14300);
                connecting = false;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Unable to Connect .. Try again?");
                if(System.currentTimeMillis() - current >= 10000){
                    JOptionPane.showMessageDialog(null, "Time limit exceeded...Try again later");
                    System.exit(0);
                }
            }
        }

        client.addListener(this);

        client.getKryo().register(Packet.class);
        client.getKryo().register(PacketConnect.class);
        client.getKryo().register(PacketClientConnected.class);
        client.getKryo().register(PacketClientDisconnect.class);
        client.getKryo().register(PacketChat.class);


        PacketConnect connect = new PacketConnect();
        connect.username = name;
        client.sendTCP(connect);

        frame.setSize(600, 480);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Panel p = new Panel();

        sendButton.addActionListener(this);
        textField.addActionListener(this);


        Font font = new Font("consolas", Font.BOLD, 15);
        textArea.setFont(font);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.createToolTip();
        JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(580, 380));

        p.add(areaScrollPane);
        p.add(textField);
        p.add(sendButton);

        frame.add(p);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new ClientMain();

    }

    @Override
    public void received(Connection connection, Object object) {

        if(object instanceof Packet){
            if(object instanceof PacketClientConnected){
                PacketClientConnected p1 = (PacketClientConnected) object;
                textArea.append(p1.clientName+" connected!\n");
            }
            else if(object instanceof PacketClientDisconnect){
                PacketClientDisconnect p1 = (PacketClientDisconnect) object;
                textArea.append(p1.clientname+" disconnected!\n");
            } else if(object instanceof PacketChat){
                PacketChat p1 = (PacketChat) object;
                textArea.append(p1.clientname+": "+p1.message+"\n");
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent arg0) {

        String message = textField.getText();
        if(message.length() == 0){
            return;
        }else if(message.startsWith("Type")){
            textField.setText("");
            return;
        }


        PacketChat chat = new PacketChat();
        chat.clientname = name;
        chat.message = message;
        System.out.println("bytes sent : "+client.sendTCP(chat));


        textArea.setSelectedTextColor(Color.orange);
        textArea.append(name);
        textArea.setSelectedTextColor(Color.black);
        textArea.append(": "+ message + "\n");

        textField.setText("");
    }

}