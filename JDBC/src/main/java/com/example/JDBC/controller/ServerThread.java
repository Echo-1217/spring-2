package com.example.JDBC.controller;

import com.example.JDBC.model.CRUD;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;

@Slf4j
public class ServerThread extends Thread {
    private final CRUD crud = new CRUD();


    private final Socket currClient;

    public ServerThread(Socket currClient) {
        this.currClient = currClient;
    }

    @Override
    public void run() {
        try {
            // 本地服務器控制台顯示客戶端連接的用戶信息
            log.info("connection accept: {} \t", currClient);
            InputStream inputStream = currClient.getInputStream();
            // InputStreamReader == ascii --> char
            BufferedReader requestReader = new BufferedReader(new InputStreamReader(inputStream));
            // OutputStreamWriter == char --> ascii
            BufferedWriter responseWriter = new BufferedWriter(new OutputStreamWriter(currClient.getOutputStream()));

            //----request get in----
            String strRequest = requestReader.readLine();
            log.info(" client request :  " + strRequest);

            // ---response send back----
            JSONObject jsonRequest = new JSONObject(strRequest);
            switch (jsonRequest.getString("crud")) {
                case "read":
                    responseWriter.write(crud.getResponse(jsonRequest.getString("id")));
                    break;
                case "delete":
                    responseWriter.write(crud.deleteTransfer(jsonRequest.getString("id")));
                    break;
                default:
                    break;
                case "create":
                case "update":
                    responseWriter.write(crud.transferInsertAndUpdate(jsonRequest.getJSONObject("requestList")));
                    break;
            }

            responseWriter.newLine();
            responseWriter.flush();
            //client close---------------
//            currClient.close();
//            log.info("client is closed: " + currClient.isClosed());

        } catch (IOException | JSONException | SQLException e) {
            try {
                log.info("exception message in try catch\n: IOException | JSONException : ", e);

                BufferedWriter responseWriter = new BufferedWriter(new OutputStreamWriter(currClient.getOutputStream()));
                responseWriter.write(e.getMessage());
                responseWriter.newLine();
                responseWriter.flush();
                log.info("send error message\n");

//                currClient.close();
            } catch (IOException ex) {
                log.info(Arrays.toString(ex.getStackTrace()));
            }

        } finally {
            if (currClient != null) {
                if (!currClient.isClosed()) {
                    try {
                        currClient.close();
                    } catch (Exception e) {
                        log.info(Arrays.toString(e.getStackTrace()));
                    }
                }
                log.info("in finally : current client is closed : "+ currClient.isClosed());
            }
        }
    }
}
