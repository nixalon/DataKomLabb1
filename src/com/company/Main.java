package com.company;

import java.io.IOException;
import java.net.*;

public class Main {

    public static void main(String[] args) throws IOException {
        String[] servers = {"gbg1.ntp.se", "gbg2.ntp.se", "mmo1.ntp.se", "mmo2.ntp.se", "sth1.ntp.se", "sth2.ntp.se", "svl1.ntp.se", "svl2.ntp.se"};
        int currentServer = 0;
        DatagramSocket socket = new DatagramSocket();

        while (true) {
            try {
                InetAddress address = InetAddress.getByName(servers[currentServer]);
                SNTPMessage message = new SNTPMessage();
                byte[] buf = message.toByteArray();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);

                socket.send(packet);
                System.out.println("Sent request");
                socket.receive(packet);

                SNTPMessage response = new SNTPMessage(packet.getData());
                System.out.println("Got reply");
                socket.close();
                System.out.println();
                System.out.println(response.toString());

                double result = ((SNTPMessage.receiveTimeStamp - SNTPMessage.originateTimeStamp) + (SNTPMessage.receiveTimeStamp - SNTPMessage.transmitTimeStamp)) / 2;
                System.out.println("Difference between clocks: " + result);

            } catch (SocketException | UnknownHostException e) {
                currentServer++;
                if(servers.length == currentServer){
                    break;
                }
                continue;
            }
            break;

        }

	        /*byte [] buf = {  36,   1,  0, -25,  0,  0,    0,   0,
                                0,   0,  0,   2, 80, 80,   83,   0,
                              -29, 116,  5,  61,  0,  0,    0,   0,
                              -29, 116,  5,  59, 14, 86,    0,   0,
                              -29, 116,  5,  62,  0, 47, -121, -38,
                              -29, 116,  5,  62,  0, 47, -113,  -1};

	        SNTPMessage msg = new SNTPMessage(buf);*/
    }
}
