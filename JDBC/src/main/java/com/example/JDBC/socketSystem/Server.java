package com.example.JDBC.socketSystem;


import com.example.JDBC.controller.ServerThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

@Slf4j
public class Server {


    private static final int listenPort = 1010;


    public static void main(String[] args) {
        log.info("Server\n");
        try {
            ServerSocket server = new ServerSocket(listenPort);
            log.info("started: {}", server);

            while (true) {
                // listen request
                Socket socket = server.accept();
                // new thread execution services
                new ServerThread(socket).start();
            }

        } catch (IOException e) {
            log.info(Arrays.toString(e.getStackTrace()));
        }
    }

}