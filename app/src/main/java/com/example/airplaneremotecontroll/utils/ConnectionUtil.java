package com.example.airplaneremotecontroll.utils;

import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;

import com.example.airplaneremotecontroll.activities.MainActivity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectionUtil {

    public static Socket socket;

    public static void connect() {

        final WifiManager manager = (WifiManager) MainActivity.getInstance().getApplicationContext().getSystemService(MainActivity.getInstance().WIFI_SERVICE);
        final DhcpInfo dhcp = manager.getDhcpInfo();
        final String address = Formatter.formatIpAddress(dhcp.gateway);
        int port = 1337;

        try {

            System.out.println("IP Address found: " + address);
            System.out.println("Connecting to " + address + " on port " + port);
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), 3000);
            System.out.println("Successfully connected");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    OtherUtils.popupMessage("Connection successful", "Successfully connected to " + address + ":" + port);
                }
            });
            socket.setSoTimeout(5000);

            socket.getOutputStream();

            startPacketSending();

        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);


            ex.printStackTrace();

            System.out.println("An error occurred while connecting to ESP-Module.");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    OtherUtils.popupAction("Connection failed", "An error occurred while connecting to ESP-Module. (" + address + ":" + port + ")" +
                            "\n----------------------------------------------------------------\n" + sw.toString(), "Reconnect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Thread thread = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try  {
                                        connect();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            thread.start();


                        }
                    });
                }
            });

        }
    }

    public static void startPacketSending() throws InterruptedException {
        while (ConnectionUtil.socket.isConnected()) {
            if (!MainActivity.stopped) {
                String message = MovementUtil.speed + "," + MovementUtil.site + "," + MovementUtil.height + "\n";
                ConnectionUtil.sendMessage(message);
                //System.out.println(message);
                Thread.sleep(100);
            }

        }
    }

    public static void disconnect() {
        if (!socket.isClosed()) {
            try {
                socket.close();
                OtherUtils.popupMessage("Disconnected", "The application client successfully disconnected from the server.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            OtherUtils.popupMessage("Not connected", "The application client is not connected to the server.");
        }
    }

    public static void sendMessage(String message) {
        try {
            socket.getOutputStream().write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
