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
                    //fin.read(buffer)????????????????????????????????????
                    while (fin.read(buffer) != -1) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????0??????limit
                        //???????????????????????????????????? position  limit
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            //??????write??????????????????????????????
                            fout.write(buffer);
                        }
                        //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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



        File smallFile = new File("F:\\43 ??????git?????????\\01???????????? ??????????????? www.zxit8.com???.mp4");
        File smallFileCopy = new File("D:\\7fgame\\ccc.mp4");

        System.out.println("-----------copy---------------");
//        benchmark(noBufferStreamCopy,smallFile,smallFileCopy);
        benchmark(bufferStreamCopy,smallFile,smallFileCopy);
        benchmark(nioBufferStreamCopy,smallFile,smallFileCopy);
        benchmark(noTransferStreamCopy,smallFile,smallFileCopy);
    }
}
