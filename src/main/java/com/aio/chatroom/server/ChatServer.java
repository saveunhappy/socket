package com.aio.chatroom.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final String LOCALHOST = "localhost";
    private static final int DEFAULT_PORT = 8888;

    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;
    private static final int THREADPOOL_SIZE = 8;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverChannel;
    private List<ClientHandler> connectedClients;
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public ChatServer(int port) {
        this.port = port;
        this.connectedClients = new ArrayList<>();
    }

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    private boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void start() {
        try {
            // 建立包含固定数量的线程池
            ExecutorService executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE);
            // 自定义 AsynchronousChannelGroup
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
            // 绑定端口
            serverChannel.bind(new InetSocketAddress(LOCALHOST, port));
            System.out.println("启动服务器，监听端口:" + port);

            // 为了持续监听连接的请求
            while (true) {
                serverChannel.accept(null, new AcceptHandler());
                // 阻塞式的调用
                System.in.read();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverChannel);
        }

    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
            // 持续监听新的客户端的连接
            if (serverChannel.isOpen()) {
                serverChannel.accept(null, this);
            }
            if (clientChannel != null && clientChannel.isOpen()) {
                ClientHandler handler = new ClientHandler(clientChannel);
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
                //  TODO 将新用户添加到在线用户列表中去
                addClient(handler);
                clientChannel.read(buffer, buffer, handler);
            }


        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("连接失败:" + exc);
        }
    }

    private class ClientHandler implements CompletionHandler<Integer, Object> {
        private AsynchronousSocketChannel clientChannel;

        public ClientHandler(AsynchronousSocketChannel channel) {
            this.clientChannel = channel;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            ByteBuffer buffer = (ByteBuffer) attachment;
            if (buffer != null) {
                if (result <= 0) {
                    // 客户端发生异常
                    // TODO 将客户移除在线客户列表
                    removeClient(this);
                } else {
                    buffer.flip();
                    String fwdMsg = receive(buffer);
                    System.out.println(getClientName(clientChannel) + ":" + fwdMsg);
                    forwardMessage(clientChannel, fwdMsg);
                    buffer.clear();

                    // 检查用户是否决定退出
                    if (readyToQuit(fwdMsg)) {
                        // TODO 将客户移除在线客户列表
                        removeClient(this);
                    } else {
                        // 继续监听从客户端发送的消息
                        clientChannel.read(buffer, buffer, this);
                    }
                }
            }
        }


        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("读写失败" + exc);
        }
    }

    private synchronized void addClient(ClientHandler handler) {
        connectedClients.add(handler);
        System.out.println(getClientName(handler.clientChannel) + "已经连接到服务器！");
    }

    private synchronized void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
        System.out.println(getClientName(handler.clientChannel) + "已断开连接！");
        close(handler.clientChannel);
    }

    private String receive(ByteBuffer buffer) {
        CharBuffer charBuffer = charset.decode(buffer);
        return String.valueOf(charBuffer);
    }

    private String getClientName(AsynchronousSocketChannel clientChannel) {

        int clientPort = -1;
        try {
            InetSocketAddress address = (InetSocketAddress) clientChannel.getRemoteAddress();
            clientPort = address.getPort();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "客户端[" + clientPort + "]";
    }


    private synchronized void forwardMessage(AsynchronousSocketChannel clientChannel, String fwdMsg) {
        for (ClientHandler handler : connectedClients) {
            if(!clientChannel.equals(handler.clientChannel)){
                try {
                    ByteBuffer buffer = charset.encode(getClientName(handler.clientChannel) + ":" + fwdMsg);
                    handler.clientChannel.write(buffer, null, handler);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) {
        ChatServer server= new ChatServer();
        server.start();
    }

}