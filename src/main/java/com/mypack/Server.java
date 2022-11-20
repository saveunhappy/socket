package com.mypack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        final String QUIT = "quit";

        final int DEFAULT_PORT = 8888;
//        ServerSocket serverSocket = null;
        try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
            //绑定监听端口
            System.out.println("启动服务器，监听端口" + DEFAULT_PORT);
            while (true) {
                //等待客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("客户端【" + socket.getPort() + "】已连接");
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                //读取客户端发过来的消息
                String msg = null;
                while ((msg = reader.readLine()) != null) {


                    System.out.println("客户端【" + socket.getPort() + "]:" + msg);
                    //回复客户发送的消息
                    writer.write("服务器" + msg + "\r\n");
                    writer.flush();
                    if (QUIT.equals(msg)) {
                        System.out.println("客户端【" + socket.getPort() + "]已断开连接");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
