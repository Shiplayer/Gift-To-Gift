package ru.gifttogift;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.gifttogift.db.GiftDB;
import ru.gifttogift.server.ResponseToRequestHandler;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static LogFile logFile;
    public String url = "https://www.redcube.ru/";

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, URISyntaxException, KeyManagementException, NoSuchAlgorithmException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        logFile = new LogFile();
        //new Test().run();
        GiftDB giftDB = new GiftDB();
        System.out.println(giftDB.getGifts(0, 10));
        new Main().run(args);
    }

    private void run(String[] args) throws IOException {
        System.out.println(args.length);
        if(args.length != 0 && args[0].equalsIgnoreCase("-check"))
            checkDatabase();
        try {
            System.out.println("SERVER START");
            ResponseToRequestHandler ss = new ResponseToRequestHandler();
        } catch (IOException | SQLException | ClassNotFoundException e) {
            logFile.write(e);
        }
    }

    private void query() throws SQLException, ClassNotFoundException {
        GiftDB giftDB = new GiftDB();
        giftDB.addGift(new DataGift("present", "https://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc", "category", "asdqwe",
                123123, "eqwrwq"));

    }

    /*
    $.ajax({
      url: "http://www.redcube.ru/sections/view/242",
      data: { page: 1
      },
      success: function(response) {
        console.log(response);
      },
      error: function(xhr) {
        //Do Something to handle error
      }
    });
     */
    private void checkDatabase() throws IOException {
        /*HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
        connection.connect();
        BufferedReader bf = new BufferedReader(new InputStreamReader((InputStream) connection.getContent()));*/
        ArrayList<String> catalog = new ArrayList<>();
        Document document = Jsoup.connect(url).get();
        Elements e = document.body().getElementsByClass("categories"); //берем ссылки на все категории
        if(e.size() == 1){
            Elements ul = e.get(0).children();
            for(int i = 0; i < ul.size(); i++){
                Element li = ul.get(i);
                catalog.add(li.children().get(0).attr("href").substring(1));
            }
        }
        // проходимся по всем полученным ссылками
        for(String page : catalog) {
            document = Jsoup.connect(URLDecoder.decode(url + page, "UTF-8")).get();
            Element jsGoodsMore = document.getElementsByClass("js-goods-more").first(); // кнопка, чтобы показать еще больше элементов
            if(jsGoodsMore == null) {// а есть ли она вообще?
                Elements blockList = document.getElementsByClass("b-cube_side-block-list italic").get(0).getElementsByTag("li");
                for(int i = 0; i < blockList.size(); i ++){
                    Element li = blockList.get(i);
                    if(li.className().equals("separator"))
                        continue;
                    System.out.println(url + li.children().get(0).attr("href").substring(1));
                    HandleContent(Jsoup.connect(URLDecoder.decode(url + li.children().get(0).attr("href"), "UTF-8")).get());
                }

                continue;
            }
            // количество подарков в данной категории, навзание категории и ссылка на эту категорию
            //HandleContent(document);
        }

        /*
            175 Хэллоуин /sections/view/273
            683 Подарки на свадьбу / Молодоженам /sections/view/275
            5150 Подарки /sections/view/242
            1182 Интерьер /sections/view/2
            2315 Посуда /sections/view/91
            195 Игрушки и куклы /sections/view/118
            331 Учеба и работа /sections/view/225
         */
    }

    private void HandleContent(Document document) throws IOException {
        String count;
        if(document.getElementsByClass("js-goods-more").size() == 0){
            count = String.valueOf(document.getElementsByClass("b-cube_goods-stuff-list ").size());
        } else {
            count = document.getElementsByClass("js-goods-more").get(0).attr("data-count");
        }
        String nameCategory = document.getElementsByClass("i-cube_active_breadcrumb").get(0).text();
        if(document.getElementsByClass("js-goods-more").size() == 0){
            System.err.println("returned");
            return;
        }
        String urlCategory = "/sections/view/" + document.getElementsByClass("i-flocktory").get(0).attr("data-fl-category-id");
        System.out.println("urlCategory= " + urlCategory);
        System.out.println(count + " " + nameCategory + " " + urlCategory);
        new DownloaderGifts(Integer.parseInt(count), nameCategory, urlCategory);
    }
}

//i-cube_goods-stuff-item no-touch text-center personalisation-good