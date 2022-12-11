package com.nio.fileoperator;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

interface FileCopyRunner {
    void copyFile(File source, File target);
}


public class FileCopyDemo {
    private static final int ROUNDS = 5;

    private static void benchmark(FileCopyRunner fileCopyRunner,File source,File target){
        long elapsed = 0L;
        for (int i = 0; i < ROUNDS; i++) {
            long startTime = System.currentTimeMillis();
            fileCopyRunner.copyFile(source,target);
            elapsed += System.currentTimeMillis() - startTime;
            target.delete();
        }
        System.out.println(fileCopyRunner + ":" + elapsed / ROUNDS);


    }

    public static void main(String[] args) {
        FileCopyRunner noBufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                try (
                        InputStream fin = new FileInputStream(source);
                        OutputStream fout = new FileOutputStream(target);
                ) {

                    int result;
                    while ((result = fin.read()) != -1) {
                        fout.write(result);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public String toString() {

                return "noBufferStreamCopy";
            }
        };
        FileCopyRunner bufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                try (
                        InputStream fin = new BufferedInputStream(new FileInputStream(source));
                        OutputStream fout = new BufferedOutputStream(new FileOutputStream(target));
                ) {
                    byte[] buffer = new byte[1024];

                    int result;
                    while ((result = fin.read(buffer)) != -1) {
                        fout.write(buffer, 0, result);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public String toString() {
                return "bufferStreamCopy";
            }
        };
        FileCopyRunner nioBufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                try (
                        FileChannel fin = new FileInputStream(source).getChannel();
                        FileChannel fout = new FileOutputStream(target).getChannel();
                ) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    //fin.read(buffer)其实是在向缓冲区中写数据
                    while (fin.read(buffer) != -1) {
                        //写了好多之后就该读取了，然后调用这个方法就会把当前写到的地方指向0，把limit
                        //指向刚才那个写到的地方。 position  limit
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            //这个write反而是从缓冲区读取。
                            fout.write(buffer);
                        }
                        //恢复到最开始的指针指向的位置，但是没有清除之前的东西，如果再使用，相当于是覆盖
                        buffer.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public String toString() {
                return "nioBufferStreamCopy";
            }
        };
        FileCopyRunner noTransferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                try (
                        FileChannel fin = new FileInputStream(source).getChannel();
                        FileChannel fout = new FileOutputStream(target).getChannel();
                ) {
                    long transfer = 0L;
                    long size = fin.size();
                    while (transfer != size) {
                        transfer += fin.transferTo(0, size, fout);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public String toString() {
                return "noTransferStreamCopy";
            }
        };



        File smallFile = new File("F:\\43 玩转git三剑客\\01课程综述 【更多教程 www.zxit8.com】.mp4");
        File smallFileCopy = new File("D:\\7fgame\\ccc.mp4");

        System.out.println("-----------copy---------------");
//        benchmark(noBufferStreamCopy,smallFile,smallFileCopy);
        benchmark(bufferStreamCopy,smallFile,smallFileCopy);
        benchmark(nioBufferStreamCopy,smallFile,smallFileCopy);
        benchmark(noTransferStreamCopy,smallFile,smallFileCopy);
    }
}
