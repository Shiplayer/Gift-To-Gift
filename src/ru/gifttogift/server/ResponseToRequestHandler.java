package ru.gifttogift.server;

import ru.gifttogift.DataGift;
import ru.gifttogift.Main;
import ru.gifttogift.db.GiftDB;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Exchanger;

public class ResponseToRequestHandler {
    private final GiftDB giftDB;
    private final ServerSocket serverSocket;
    public String request;

    public ResponseToRequestHandler() throws IOException, SQLException, ClassNotFoundException {
        serverSocket = new ServerSocket(44579);
        giftDB = new GiftDB("admin", "Anton4447138");
        new Thread(() -> {
            while(true){
                try {
                    Socket s = serverSocket.accept();
                    new Client(s, s.getInetAddress().getHostName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public class Message {
        private String text;
        private int first;
        private int count;
        private String path;

        public Message(String txt, int first, int count){
            text = txt;
            this.first = first;
            this.count = count;
        }

        public Message(int first, int count){
            this.first = first;
            this.count = count;
        }

        public Message(String text){
            this.text = text;
        }

        public Message(String methods, String path) {
            this.text = methods;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public boolean isEmpty(){
            return text.isEmpty();
        }

        public String getText(){
            return text;
        }

        public void setFirst(int first) {
            this.first = first;
        }

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public class RequestHandler{
        private String content;
        private StringTokenizer stringTokenizer;

        public RequestHandler(String s){
            content = s;
            stringTokenizer = new StringTokenizer(s);
        }

        public boolean hasNext(){
            return stringTokenizer.hasMoreTokens();
        }

        public String nextString() {
            if (hasNext())
                return stringTokenizer.nextToken();
            else
                return null;
        }

        public Integer nextInt() {
            if (hasNext())
                return Integer.parseInt(stringTokenizer.nextToken());
            else
                return null;

        }

        public String nextAllString() {
            StringBuilder stringBuilder = new StringBuilder();
            String buf;
            while((buf = nextString()) != null){
                if(hasNext())
                    stringBuilder = stringBuilder.append(buf).append(" ");
                else
                    stringBuilder = stringBuilder.append(buf);
            }
            return stringBuilder.toString();
        }
    }


    public class Client{
        private final Socket socket;
        private final String name;
        private String messageSend;
        private String messageRecive;
        private final int id;
        private int count = 0;
        private final Exchanger<Message> exchanger;

        public Client(Socket addr, String name) throws FileNotFoundException {
            socket = addr;
            this.name = name;
            id = count++;
            exchanger = new Exchanger<>();
            new Thread(new HandleEventsClientReceive()).start();
            //new Thread(new HandleEventsClientSend()).start();
        }

        public class HandleEventsClientReceive implements Runnable{

            Message message;

            public HandleEventsClientReceive() {

            }

            @Override
            public void run() {
                try (BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {
                    String buf;

                    while ((buf = bf.readLine()) != null) {
                        //message = new Message(buf);
                        RequestHandler requestHandler = new RequestHandler(buf);
                        String methods;
                        System.out.println(buf);
                        if ((methods = requestHandler.nextString()).equals("get"))
                            message = new Message(methods, requestHandler.nextInt(), requestHandler.nextInt());
                        else if(methods.equals("getImage"))
                            message = new Message(methods, requestHandler.nextAllString());
                        else if(methods.equals("getCategory"))
                            message = new Message(methods);

                        //exchanger.exchange(message);
                        System.err.println("message is receiving");
                        if (message.getText().equalsIgnoreCase("get")) {
                            List<DataGift> list = giftDB.getGifts(message.first, message.count);
                            System.out.println(Arrays.toString(list.toArray()));
                            pw.println(list.size());
                            if (list.isEmpty()) {
                                pw.println("null");
                            } else {
                                for (DataGift e : list) {
                                    pw.println(e);
                                }
                            }
                            System.err.println("message is sending");
                        } else if(message.getText().equalsIgnoreCase("getImage")){
                            File file = new File(message.getPath());
                            try {
                                System.out.println(message.getPath());
                                System.out.println("file.length() = " + file.length());
                                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                                //outputStream.write(ByteBuffer.allocate(Long.BYTES).putLong(file.length()).array());
                                BufferedImage image = ImageIO.read(file);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                baos.reset();
                                ImageIO.write(image, "jpg", baos);
                                System.out.println(baos.size());
                                outputStream.writeInt(baos.size());
                                baos.writeTo(outputStream);

                            } catch (IOException e){
                                Main.logFile.writeInfo(file.getPath());
                                Main.logFile.write(e);
                            }
                            //ImageIO.write(image, "jpg", outputStream);
                            /*byte[] bytes = new byte[fileInputStream.available()];
                            System.out.println(fileInputStream.available());
                            int count = fileInputStream.read(bytes);
                            System.out.println("count = " + count);

                            objectOutputStream.write(ByteBuffer.allocate(4).putInt(count).array());
                            objectOutputStream.write(bytes);

                            objectOutputStream.close();
                            fileInputStream.close();
                            */

                            //TODO отправлять в виде JSON
                        } else if(message.getText().equals("getCategory")){
                            File file = new File("images");
                            if(file.exists()){
                                File[] dirs = file.listFiles();
                                assert dirs != null;
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int n = 0; n < dirs.length; n++){
                                    stringBuilder = stringBuilder.append(dirs[n].getName()).append(";");
                                }
                                System.out.println(stringBuilder.toString());
                                pw.println(stringBuilder.toString());
                            }
                        }
                    }

                } catch (IOException e) {
                    System.out.println("exit");
                    Main.logFile.write(e);
                    //e.printStackTrace();
                }
            }
        }

        /*public class HandleEventsClientSend implements Runnable {
            Message message;

            public HandleEventsClientSend() {

            }

            @Override
            public void run() {
                try (PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {
                    System.err.println("send: " + socket.getInetAddress().getHostName());
                    while (true) {
                        message = exchanger.exchange(message);
                        if (!message.isEmpty()) {
                            if (message.getText().equals("get")) {
                                List<DataGift> list = giftDB.getGifts(message.first, message.count);
                                System.err.println(list.size());
                                pw.println(list.size());
                                if (list == null) {
                                    pw.println("null");
                                } else {
                                    for (DataGift e : list) {
                                        pw.println(e);
                                    }
                                }
                                System.err.println("message is sending");
                            } else {
                                pw.println("error request");
                            }
                            System.out.println("flush");
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("error");
                    e.printStackTrace();
                }
            }
        }*/
    }
}
