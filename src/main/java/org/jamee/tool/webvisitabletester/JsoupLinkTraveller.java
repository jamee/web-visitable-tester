package org.jamee.tool.webvisitabletester;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupLinkTraveller implements LinksTraveller {

    public void accept(String link, LinkVisitor visitor) throws IOException {
        Connection conn = Jsoup.connect(link);
        conn.header("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:36.0) Gecko/20100101 Firefox/36.0");
        conn.header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.header("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        conn.header("Accept-Encoding","gzip, deflate");
        Response resp = conn.execute();
        Document doc = resp.parse();

        if (doc != null) {
            Elements linkElements = doc.select("a[href]");
            for (Element le : linkElements) {
                try {
                    URL url = new URL(le.attr("abs:href"));
                    visitor.visit(url, le.text());
                } catch (java.net.MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
