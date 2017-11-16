package ru.gifttogift;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.gifttogift.db.GiftDB;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class DownloaderGifts {
    private String content;
    private String giftsCategory;
    private GiftDB giftDB;
    private final String siteName = "https://www.redcube.ru";
    private final int maxProductInPage = 32;
    private final String request = "?page=";
    private final String DirWithImage = "images/";
    private File dirFile;
    private boolean notFound = false;

    private class Downloader implements Runnable {
        private String url;
        private String category;

        Downloader(String url, String category){
            this.url = url;
            this.category = category;
        }

        @Override
        public void run() {
            try {
                PagesHandlerWithGiftsInCategory(url, category);
            } catch (IOException e) {
                Main.logFile.write(e);
            }
        }
    }

    private class WriteImage implements Runnable {
        private String path, copyHref;
        public WriteImage(String path, String copyHref){
            this.path = path;
            this.copyHref = copyHref;
        }

        @Override
        public void run() {
            if (!new File(path).exists()) {
                try {
                    //System.out.println(copyHref);
                    //System.out.println(path);
                    byte[] bytes = DownloadImageFromURL(copyHref);
                    if (bytes != null) {
                        try (FileOutputStream fio = new FileOutputStream(new File(path))) {
                            fio.write(bytes);
                            fio.flush();
                        } catch (IOException e) {
                            Main.logFile.write(e);
                        }
                    }
                    //fio.close();
                } catch (IOException e) {
                    Main.logFile.write(e);

                }
            }
        }
    }


    // TODO (3) разбить на различные методы, так ведь проще?
    // TODO может попробовать забить на многопоточность? ThreadPool
    public DownloaderGifts(int count, String category, String url) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        Collection<Future<?>> futures = new LinkedList<>();
        System.out.println(category);
        giftsCategory = category;
        try {
            giftDB = new GiftDB();
        } catch (ClassNotFoundException | SQLException e) {
            Main.logFile.write(e);
        }
        // TODO (4) придумать что-нибудь получше
        if(category.contains("/")){
            category = category.replace(" / ", "\\");
        }

        dirFile = new File(DirWithImage + category);
        dirFile.mkdirs();

        for(int i = 0; i < count / maxProductInPage + 1; i++){
            futures.add(executorService.submit(new Downloader(url + request + (i + 1), category)));
            //PagesHandlerWithGiftsInCategory(url + request + (i + 1), category);
        }
        executorService.shutdown();
        try {
            /*for (Future<?> future : futures) {
                future.get();
            }*/
            executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Main.logFile.write(e);
        }
    }

    public boolean PagesHandlerWithGiftsInCategory(String url, String category) throws IOException {
        System.out.println("PagesHandlerWithGiftsInCategory: " + siteName + url + " Thread.currentThread().getName()=" + Thread.currentThread().getName());
        Document document = Jsoup.connect(URLDecoder.decode(siteName + url, "UTF-8")).get();
        Element ul = document.getElementById("b-cube_goods-stuff-list ");
        if(ul == null) {
            ul = document.getElementById("b-cube_goods-stuff-list without-best");
        }
        Elements li = ul.children();
        for(Element e : li) {
            if (!e.tagName().equals("li")) {
                continue;
            }
            Element link = e.getElementsByClass("i-cube_goods-stuff-item-link").first();
            if (link == null)
                continue;
            //System.out.println(link.hasAttr("href"));
            String sLink = link.attr("href");
            HandlerGift(sLink, category);
        }
        return true;
    }

    /*
    TODO добавить скачивание одного основного изображения
    <div class="zoomContainer" style="-webkit-transform: translateZ(0);position:absolute;left:334.5px;top:460px;height:412px;width:275px;">
        <div class="zoomLens" style="background-position: 0px 0px; float: right; overflow: hidden; z-index: 999; transform: translateZ(0px); opacity: 0.4; zoom: 1; width: 220px; height: 219.733px; background-color: white; cursor: default; border: 1px solid rgb(0, 0, 0); background-repeat: no-repeat; position: absolute; left: 46px; top: 190px; display: none;">&nbsp;</div>
        <div class="zoomWindowContainer" style="width: 400px;">
            <div style="overflow: hidden; background-position: -84.5455px -350px; text-align: center; background-color: rgb(255, 255, 255); width: 400px; height: 400px; float: left; background-size: 500px 750px; z-index: 100; border: 4px solid rgb(136, 136, 136); background-repeat: no-repeat; position: absolute; background-image: url(&quot;https://static.redcube.ru/fotos/fragment/60.9T.001.jpg&quot;); top: 0px; left: 275px; display: none;" class="zoomWindow">&nbsp;</div>
        </div>
    </div>
     */

    public boolean HandlerGift(String url, String category) throws IOException {
        List<String> pathImage = new ArrayList<>();
        // TODO что-то сделать с ссылкой, т.к. в ней могут присутствовать различные символы
        Document gifts = null;
        try {
            gifts = Jsoup.connect(URLDecoder.decode(siteName + url, "UTF-8")).get();
        } catch (HttpStatusException e){
            Main.logFile.write(e);
            //Main.logFile.writeInfo();
            return false;
        }
        Elements descLine = gifts.getElementsByClass("b-cube_good-desc-line cf");
        String describe = "";
        for(Element e : descLine){
            if(e.attr("itemprop").equals("description"))
                describe = e.text();
        }
        String codeGift = gifts.getElementById("i-cube_good-desc-artc").text();
        String name = gifts.getElementById("b-cube_good-desc-title").text();
        double price = Double.parseDouble(gifts.getElementsByClass("b-cube_price-info").first().getElementsByTag("span").first().text());
        //System.out.println(name + " " + codeGift + " " + price + " " + describe + " " + giftsCategory);
        //public DataGift(String name, String url, String category, String description, double price, String code){
        DataGift dataGift = new DataGift(name, url, giftsCategory, describe, price, codeGift);
        if(!giftDB.containsCurrGift(dataGift)) {
            // получание картинки
            Element bCubeGoodThumbList = gifts.getElementById("b-cube_good-thumb-list");
            if(bCubeGoodThumbList == null){
                Element zoomImage = gifts.getElementsByClass("zoomWindow").get(0);
                String attr = zoomImage.attr("style");
                String[] names = attr.split(";");
                for(String nameAttr : names){
                    if(nameAttr.trim().equalsIgnoreCase("background-image:")){
                        String strUrl = nameAttr.split(":")[1].trim();
                        strUrl = strUrl.substring(strUrl.indexOf("\"" + 1, strUrl.lastIndexOf("\"") - 1));
                        new Thread(new WriteImage(dirFile.getPath(), strUrl.substring(strUrl.lastIndexOf('\\') + 1))).start();
                    }
                }
                giftDB.addGift(dataGift);
                return true;
            }
            Elements imageLinks = bCubeGoodThumbList.children();
            String href;
            int arrCharBackslash[] = new int[10];
            //System.out.println(gifts);
            if (imageLinks == null) {
                return false;
            }
            for (Element ch : imageLinks) {
                int index = 0;
                if (ch.hasAttr("style"))
                    continue;
                Element imageE = ch.child(0);
                href = imageE.attr("href");
                for (int i = 0; i < href.length(); i++) {
                    if (href.charAt(i) == '/') {
                        arrCharBackslash[index++] = i;
                    }
                }

                String code = href.substring(arrCharBackslash[index - 1] + 1, href.lastIndexOf('.'));
                String size = "s" + href.substring(arrCharBackslash[index - 2] + 1, arrCharBackslash[index - 1]);
                String format = href.substring(href.lastIndexOf('.'));
                String path = dirFile.getPath() + "\\" + code + size + format;
                //System.out.println(path);
                final String copyHref = href;
                new Thread(new WriteImage(path, copyHref)).start();
                if (!notFound)
                    dataGift.AddImagePath(path);
            }

            giftDB.addGift(dataGift);
            return true;
        } else {
            System.err.println("gift exists... category=" + category + " url=" + url + " Thread.currentThread().getName()=" + Thread.currentThread().getName());
            return false;
        }
    }

    public Document GetNewProducts(String url){
        return null;
    }

    private byte[] DownloadImageFromURL(String url) throws IOException {
        ByteArrayOutputStream out = null;
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection)new URL(URLDecoder.decode(url, "UTF-8")).openConnection();
            urlConnection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            notFound = false;
            return out.toByteArray();
        } catch (FileNotFoundException e){
            System.err.println(url + " not found");
            notFound = true;
        }
        return null;
    }
}
