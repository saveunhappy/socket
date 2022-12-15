package com.nio.mypack.client;

public class CClient {
    public static void main(String[] args) {
        ChatClient client = new ChatClient("127.0.0.1", 7777);
        client.start();
    }
}
