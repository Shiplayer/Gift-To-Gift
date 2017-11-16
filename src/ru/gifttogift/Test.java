package ru.gifttogift;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.gifttogift.server.ResponseToRequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, ClassNotFoundException {
        new Test().run();
    }

    public void run() throws IOException, URISyntaxException, SQLException, ClassNotFoundException {
        ResponseToRequestHandler ss = new ResponseToRequestHandler();
        /*String url = "http://www.redcube.ru/podarki-svadba/tarelka-dekorativnaya-s-vashim-tekstom-bonni-i-kla%C4%ADd-PP.SL.004.001675/";
        String decodeURL = URLDecoder.decode(url, "UTF-8");
        System.out.println(sendRequest(url));*/
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
