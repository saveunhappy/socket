package com.bio.client;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private final String QUIT = "quit";
    private final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private final int DEFAULT_SERVER_PORT = 8888;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    //发送消息给服务器
    public void send(String msg) throws IOException {
        if (!socket.isOutputShutdown()) {
            //因为服务器读取是readLine函数，所以要读取到一个回车
            writer.write(msg + "\n");
            writer.flush();
        }
    }


    //从服务器接收消息
    public String receive() throws IOException {
        String msg = null;
        if (!socket.isInputShutdown()) {
            msg = reader.readLine();
        }
        return msg;
    }

    //检查用户是否准备推出

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public void close() {
        if (writer != null) {
            try {
                System.out.println("关闭");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            //创建socket
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
            //创建IO流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // 处理用户的输入,这个里面就是个死循环，一直等着输入的，不会说你说一句话就创建一个线程
            //而是你第一次进来就去创建那一个线程，在那一个线程中一直等着你输入，如果你输入了quit退出
            //那么才会退出这个线程，在UserInputHandler中的run方法中去看那个方法把
            /*
            *  if(chatClient.readyToQuit(input)){
                    break;
               }
            * */

            new Thread(new UserInputHandler(this)).start();
            //退出了，然后来到下面
            //读取服务器转发的消息
            String msg = null;
            while ((msg = receive()) != null) {
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public static void main(String[] args) {

        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }

}
