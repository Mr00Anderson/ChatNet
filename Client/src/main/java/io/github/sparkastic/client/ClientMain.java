package io.github.sparkastic.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.sun.tools.jdi.Packet;
import io.github.sparkastic.packets.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;
import javax.swing.text.DefaultCaret;


public class ClientMain extends Listener implements ActionListener {
    private static final JFrame frame = new JFrame("--Sparkext--");
    private static final JTextArea textArea = new JTextArea();
    private static final JTextField textField = new JTextField(40);
    private static final JButton sendButton = new JButton("Send");

    private final Client client;

    private String name;
    public static final String IP = "localhost";
    public ClientMain() {
        client = new Client();
        init();

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
        textArea.setAutoscrolls(true);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        textArea.setCaret(caret);

        JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(580, 380));
        areaScrollPane.setAutoscrolls(true);

        p.add(areaScrollPane);
        p.add(textField);
        p.add(sendButton);

        frame.add(p);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new ClientMain();
    }

    public void init(){
        while (name == null || name.length() == 0) {
            name = JOptionPane.showInputDialog(null, "Enter your name :D", "Entername", JOptionPane.QUESTION_MESSAGE);
        }
        Log.set(Log.LEVEL_ERROR);

        long current = System.currentTimeMillis();
        boolean connecting = true;

        client.start();
        while (connecting) {
            try {
                client.connect(5000, IP, 14300, 14300);
                connecting = false;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Unable to Connect .. Try again?");
                if (System.currentTimeMillis() - current >= 10000) {
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
        client.getKryo().register(PacketServerInfo.class);


        PacketConnect connect = new PacketConnect();
        connect.username = name;
        client.sendTCP(connect);
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof PacketClientConnected) {
            PacketClientConnected p1 = (PacketClientConnected) object;
            textArea.append(p1.clientName + " connected!\n");
        } else if (object instanceof PacketClientDisconnect) {
            PacketClientDisconnect p1 = (PacketClientDisconnect) object;
            textArea.append(p1.clientname + " disconnected!\n");
        } else if (object instanceof PacketChat) {
            PacketChat p1 = (PacketChat) object;
            textArea.append(p1.clientname + ": " + p1.message + "\n");
        }else if(object instanceof PacketServerInfo){
            PacketServerInfo info = (PacketServerInfo) (object);
            frame.setTitle("--Sparkext--"+name+"                                 Total Online: "+info.totalOnline+"                            ");
        }
    }


    @Override
    public void actionPerformed(ActionEvent arg0) {

        String message = textField.getText();
        if (message.length() == 0) {
            return;
        } else if (message.startsWith("Type")) {
            textField.setText("");
            return;
        }


        PacketChat chat = new PacketChat();
        chat.clientname = name;
        chat.message = message;


        int bytes = client.sendTCP(chat);
        int i = 0;
        while(bytes == 0){
            if(i == 3){
                try{
                    client.connect(1000, IP, 14300, 14300);
                }catch (IOException e){
                    JOptionPane.showMessageDialog(null, "Connection error");
                    return;
                }
            }

            client.sendTCP(message);
            i++;
        }


        textArea.setSelectedTextColor(Color.orange);
        textArea.append(name);
        textArea.setSelectedTextColor(Color.black);
        textArea.append(": " + message + "\n");

        textField.setText("");
    }

}