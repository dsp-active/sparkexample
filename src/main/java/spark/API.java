package spark;

import static spark.Spark.get;
import static spark.Spark.stop;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class API {

    public static void main(String[] args) {

        // Default ohne Pfad
        get("/", (req, res) -> "Hi there! This is an internal testserver. Use /help to list all availabe API calls.");

        // Help - Alle verfügbaren Befehle
        get("/help", (req, res) -> {
            return "/hello/$name -- Simple test command with a variable.<br>" + "/stop -- Stops the server.<br>"
                    + "/status -- Outputs the current status code, which should be '200' if the server is running correctly.<br>"
                    + "/ek/$searchterms/$minPrice/$maxPrice -- Simple webscraping example utilizing search terms and pricing variables.<br>";
        });

        // helloworld, simpler Testbefehl
        get("/hello", (req, res) -> "Hello who? (hello/$name)");

        // Erweiterter Testbefehl mit Variable
        get("/hello/:name", (req, res) -> {
            return "Hello world! And hello " + req.params(":name") + "!<br>Did you know that my IP adress is " + req.ip() + "? Now you do!";
        });

        // Server stoppen
        get("/stop", (req, res) -> {
            stop();
            return "Server stopped. Great.";
        });

        // Ausgabe Statuscode (idR: 200)
        get("/status", (req, res) -> res.status());

        // Webscrape Ebay Kleinanzeigen, Suchbefehl + Preisbereich per API Aufruf
        get("/ek/:search/:min/:max", (req, res) -> {
            String url = "https://www.ebay-kleinanzeigen.de/s-";
            if (!req.params(":min").equals("0") || !req.params(":max").equals("0")) {
                url += "preis:" + req.params(":min") + ":" + req.params(":max") + "/";
            }
            url += req.params(":search") + "/k0";
            return webScrape(url);
        });
    }

    protected static String webScrape(String url) {
        try {
            // Scraping der Adresse mit direkter Such-Query
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:97.0) Gecko/20100101 Firefox/97.0";
            final Document doc = Jsoup.connect(url).proxy("proxyclu.active-logistics.com", 8080).userAgent(userAgent).timeout(60000).get();

            // Filtern des HTML Dokuments nach Inseratliste & einzelnen Angaben (Titel, Preis, Standort...)
            Elements body = doc.select("ul.itemlist.ad-list.lazyload.it3"); // Immer als Gattung + Class, hier das "Ober-Element"
            String scrape = "<br>" + doc.select("div.breadcrump span.breadcrump-summary").text() + ".<br>"; // Ergebnis der Suche
            // Es wurde(n) " + body.select("article.aditem").size() + " Inserat(e) gefunden. -- Oben ist besser tho.
            for (Element e : body.select("article.aditem")) {
                // Sehr hilfreich: String html = e.html(); ---> Genauere Angabe von Klassennamen & Strukturen! Besser als im Browser!
                scrape += "<br>-------------------------<br>";
                scrape += "<b>" + e.select("div.aditem-main div.aditem-main--middle h2.text-module-begin").text() + "</b><br>"; // Titel
                scrape += e.select("div.aditem-main div.aditem-main--middle p.aditem-main--middle--description").text() + "<br>"; // Desc
                scrape += e.select("div.aditem-main div.aditem-main--middle p.aditem-main--middle--price").text() + "<br>"; // Preis
                scrape += e.select("div.aditem-main div.aditem-main--top div.aditem-main--top--left").text() + " - " + e.select(
                        "div.aditem-main div.aditem-main--top div.aditem-main--top--right").text() + "<br>"; // Ort & Einstelldatum
                scrape += e.select("div.aditem-main div.aditem-main--bottom p.text-module-end").text() + "<br>"; // Suche oder Versand
                scrape += "<a href=\"https://www.ebay-kleinanzeigen.de" + e.select("article.aditem").attr("data-href")
                        + "\">Link zum Inserat</a>"; // Verlinkung zum Inserat mit HTML Hyperlink
                scrape += "<br>-------------------------<br>";
            }
            scrape += "<br>Alle verfügbaren Objekte der ersten Seite ausgegeben. Vielen Dank, dass Sie mit Active Airlines geflogen sind.<br>";
            return scrape;
        } catch (IOException e) {
            System.out.println("<br>Da ist etwas schiefgelaufen. Mein aufrichtiges Beileid~.. aber Mitgefühl ist leider gerade aus.<br>");
            e.printStackTrace();
        }
        return "";
    }
}

