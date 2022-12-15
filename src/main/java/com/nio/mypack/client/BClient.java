package com.nio.mypack.client;

public class BClient {
    public static void main(String[] args) {
        ChatClient client = new ChatClient("127.0.0.1", 7777);
        client.start();
    }
}
