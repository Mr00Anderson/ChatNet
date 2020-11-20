package io.github.sparkastic.packets;

public class PacketChat implements Packet {
    public String clientname;
    public String message;
    public boolean isChat = true;
}
