package com.github.io.bio;


import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于BIO的服务端
 * @Author: Zer01ne
 * @Date: 2019/3/1 10:14
 * @Version 1.0
 */
public class BioServer {

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(ServerProperties.HOST_PORT);
            System.out.println("服务端已启动，监听端口：" + ServerProperties.HOST_PORT);
            //服务开关标志
            ExecutorService service = Executors.newFixedThreadPool(5);
            while (true){
                Socket client = server.accept();
                service.submit(new BioHandler(client));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 服务端对客户端的处理线程
     */
    public static class BioHandler implements Runnable{

        private Socket client;
        private Scanner reader;
        private PrintStream writer;
        private boolean flag;
        public BioHandler(Socket client){
            this.client = client;
            flag = true;
            try {
                this.reader = new Scanner(client.getInputStream());
                reader.useDelimiter("\n");
                this.writer = new PrintStream(client.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (this.flag){
                if (reader.hasNext()){
                    System.out.println("客户端写事件");
                    String str = reader.next().trim();
                    if ("quit".equalsIgnoreCase(str)){
                        System.out.println("客户端退出事件");
                        writer.println("bye......");
                        flag = false;
                    }else {
                        writer.println("【from server】：" + str);
                    }
                    //writer.flush();
                }
            }

            try {
                reader.close();
                writer.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
