package com.example.JDBC.socketSystem;


import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class SocketClient extends Thread {
    public static final int port = 1010;

    public static void main(String[] args) {
        log.info("Client");
        new SocketClient().start();
    }

//    public void start() {
//        try {
//            //  connect
//            Socket socket = new Socket();
//            InetAddress address = InetAddress.getByName("127.0.0.1");
//            log.info("connecting to server...\n");
//            socket.connect(new InetSocketAddress(address, port), 30000);
//            new SendThread(socket).start();
////            new ReceiveThread(socket).start();
//        } catch (Exception e) {
//            log.info(Arrays.toString(e.getStackTrace()));
//        }
//    }

//    static class SendThread extends Thread {
//        private final Socket socket;
//
//        public SendThread(Socket socket) {
//            this.socket = socket;
//        }
//
//        @Override
//        public void run() {
//            ReceiveThread receiveThread =new ReceiveThread(socket);
//            String requestStr = "";
//            while (!requestStr.equals("end")) {
//                try {
//                    BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
//                    BufferedWriter requestWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//
//                    // input request string
//                    log.info("{} Enter the request:", socket);
//                    requestStr = systemIn.readLine();
//
//                    log.info("in {} request : \n", socket);
//
//                    // send request
//                    requestWriter.write(requestStr);
//                    requestWriter.newLine();
//                    requestWriter.flush();
//                    log.info("request send...");
//                    receiveThread.start();
//                } catch (Exception e) {
//                    log.info("ReceiveThread"+e.getMessage());
//                    log.info(Arrays.toString(e.getStackTrace()));
//                } finally {
//                }
//            }
//        }
//    }

//    static class ReceiveThread extends Thread {
//        private final Socket socket;
//
//        public ReceiveThread(Socket socket) {
//            this.socket = socket;
//        }
//
//        @Override
//        public void run() {
////            while (true) {
//            try {
//                // response get in
//                BufferedReader response = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                log.info("wait for response get in...");
//
//                String temp;
//                StringBuilder result = new StringBuilder();
//                while (null != (temp = response.readLine())) {
//                    result.append(temp).append("\n");
//                }
//
//                log.info(String.valueOf(result));
//                log.info("in {} response over...\n", socket);
//                log.info("{} connection is closed : " + socket.isClosed(), socket);
//
//            } catch (Exception e) {
//                log.info("ReceiveThread"+e.getMessage());
//                log.info(Arrays.toString(e.getStackTrace()));
//            }
////            }
//        }
//    }

    @Override
    public void run() {
        while (true) {
            try {
                BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
                Socket socket = new Socket();
                InetAddress address = InetAddress.getByName("127.0.0.1");
                log.info("{} Enter the request:", socket);

                // input request string
                String requestStr = systemIn.readLine();

                //  connect
                log.info("connecting to server...\n");
                socket.connect(new InetSocketAddress(address, port), 30000);
                log.info("in {} request : \n", socket);

                // send request
                BufferedWriter requestWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                requestWriter.write(requestStr);
                requestWriter.newLine();
                requestWriter.flush();
                log.info("request send...");

                // response get in
                BufferedReader response = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String temp;
                StringBuilder result = new StringBuilder();

                log.info("wait for response get in...");

                while (null != (temp = response.readLine())) {
                    result.append(temp).append("\n");
                }
                log.info(String.valueOf(result));
                socket.close();
                log.info("in {} response over...\n", socket);
                log.info("{} connection is closed : " + socket.isClosed(), socket);
            } catch (IOException e) {
                log.info("in IOException: " + e);
            }
        }
    }
}