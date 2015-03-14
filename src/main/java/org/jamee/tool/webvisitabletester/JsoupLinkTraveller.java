package org.jamee.tool.webvisitabletester;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupLinkTraveller implements LinksTraveller {

    public void accept(String link, LinkVisitor visitor) throws IOException {
        Document doc = Jsoup.connect(link).get();
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
