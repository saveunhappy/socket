package com.mypack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        final String QUIT = "quit";
        final String DEFAULT_SERVER_HOST = "127.0.0.1";
        final int DEFAULT_SERVER_PORT = 8888;

        try (Socket socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //等待用户输入信息
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = consoleReader.readLine();
                //发送给服务器
                writer.write(input + "\r\n");
                writer.flush();
                //读取服务器返回的消息
                String msg = reader.readLine();
                System.out.println("读取服务端返回回来的消息:" + msg);
                //查看用户是否退出
                if (QUIT.equals(input)){
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }


    }
}
