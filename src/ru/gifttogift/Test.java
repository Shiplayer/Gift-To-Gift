package ru.gifttogift;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.gifttogift.server.ResponseToRequestHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, ClassNotFoundException {
        new Test().run();
    }

    public void run() throws IOException, URISyntaxException, SQLException, ClassNotFoundException {
        String text = "getImage images\\Символы года 2018\\PP.DG.011.001978s340x508.jpg";
        Socket socket = new Socket("192.168.1.13", 44579);
        try(DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)){
            pw.println(text);
            byte[] bytes = new byte[4];
            int size = inputStream.readInt();
            //int size = (int) getLongFromBytes(bytes);
            System.out.println(size);
            try (InputStream imageData = new SubStream(inputStream, size)) {
                ImageIO.write(ImageIO.read(imageData), "jpg", new File("test.jpg"));
            }
            /*BufferedImage bufferedImage = ImageIO.read(inputStream);
            System.out.println(bufferedImage.getHeight() + " " + bufferedImage.getWidth());

            */
        }
        //ResponseToRequestHandler ss = new ResponseToRequestHandler();
        /*String url = "http://www.redcube.ru/podarki-svadba/tarelka-dekorativnaya-s-vashim-tekstom-bonni-i-kla%C4%ADd-PP.SL.004.001675/";
        String decodeURL = URLDecoder.decode(url, "UTF-8");
        System.out.println(sendRequest(url));*/
    }

    private static final class SubStream extends FilterInputStream {
        private final long length;
        private long pos;

        public SubStream(final InputStream stream, final long length) {
            super(stream);

            this.length = length;
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int available() throws IOException {
            return (int) Math.min(super.available(), length - pos);
        }

        @Override
        public int read() throws IOException {
            if (pos++ >= length) {
                return -1;
            }

            return super.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (pos >= length) {
                return -1;
            }

            int count = super.read(b, off, (int) Math.min(len, length - pos));

            if (count < 0) {
                return -1;
            }

            pos += count;

            return count;
        }

        @Override
        public long skip(long n) throws IOException {
            if (pos >= length) {
                return -1;
            }

            long skipped = super.skip(Math.min(n, length - pos));

            if (skipped < 0) {
                return -1;
            }

            pos += skipped;

            return skipped;
        }

        @Override
        public void close() throws IOException {
            // Don't close wrapped stream, just consume any bytes left
            while (pos < length) {
                skip(length - pos);
            }
        }
    }

    public long getLongFromBytes(byte[] bytes){
        long value = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            value = (value << 8) + (bytes[i] & 0xff);
        }
        return value;
    }

    private static Document sendRequest(String url) throws IOException {
        Connection connection = Jsoup.connect(url);
        try{
            Document document = connection.get();

            return document;

        } catch (HttpStatusException e){
            System.out.println(connection.response().statusCode());

                URLConnection urlConnection = new URL(URLEncoder.encode(url, "UTF-8")).openConnection();
                BufferedReader bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();
                while (bf.ready()) {
                    stringBuilder.append(bf.readLine());
                }
                System.out.println(stringBuilder.toString());
                return Jsoup.parse(stringBuilder.toString());

        }

    }
}
