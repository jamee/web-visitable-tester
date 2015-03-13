package org.jamee.tool.webvisitabletester;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebVisitableTester {
    private int maxTestDepth;
    private Set<String> requestedLinks;
    private boolean testExternalLink;
    private LinkStatsEntry rootlinkStatsEntry;

    public WebVisitableTester(String rootLink, String rootLinkTitle, int maxRequests, int minRequests,
            int maxTestDepth, boolean testExternalLink) {
        super();
        this.requestedLinks = new HashSet<String>();
        this.maxTestDepth = maxTestDepth;
        this.testExternalLink = testExternalLink;
        this.rootlinkStatsEntry = new LinkStatsEntry(rootLink, rootLinkTitle, maxRequests, minRequests);
    }

    public LinkStatsEntry test() {
        return test(this.rootlinkStatsEntry, this.maxTestDepth);
    }

    public LinkStatsEntry test(LinkStatsEntry linkStatsEntry, int depth) {
        if (depth == 0) {
            return null;
        }

        int requestCount = linkStatsEntry.getMaxRequestCount();
        if (linkStatsEntry.getMaxRequestCount() != linkStatsEntry.getMinRequestCount()) {
            Random r = new Random();
            requestCount = r.nextInt(linkStatsEntry.getMaxRequestCount());
            while (requestCount < linkStatsEntry.getMinRequestCount()) {
                requestCount = r.nextInt(linkStatsEntry.getMaxRequestCount());
            }
        }
        Document doc = null;
        long maxLoadTime = 0;
        long minLoadTime = 0;
        long totalLoadTime = 0;
        for (int i = 0; i < requestCount; ++i) {
            try {
                long startTime = System.currentTimeMillis();
                Connection conn = Jsoup.connect(linkStatsEntry.getLink());
                conn.execute();
                Response response = conn.response();
                if (null != response.body() && !response.body().isEmpty()) {
                    doc = response.parse();
                }
                long loadTime = System.currentTimeMillis() - startTime;
                minLoadTime = (minLoadTime == 0) ? loadTime : Math.min(minLoadTime, loadTime);
                maxLoadTime = Math.max(loadTime, maxLoadTime);
                totalLoadTime += loadTime;
                linkStatsEntry.setResponseCode(response.statusCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        linkStatsEntry.setRequestedCount(requestCount);
        linkStatsEntry.setAvarageLoadTime(totalLoadTime / requestCount);
        linkStatsEntry.setMaxLoadTime(maxLoadTime);
        linkStatsEntry.setMinLoadTime(minLoadTime);

        print(linkStatsEntry, this.maxTestDepth - depth);
        if (doc != null) {
            Elements linkElements = doc.select("a[href]");
            for (Element le : linkElements) {
                String link = le.attr("abs:href");
                try {
                    URL url = new URL(link);
                    URL rootLinkURL = new URL(rootlinkStatsEntry.getLink());
                    if (!this.testExternalLink && !url.getHost().equals(rootLinkURL.getHost())) {
                        continue;
                    }
                    if (!requestedLinks.contains(url.toExternalForm())) {
                        requestedLinks.add(link);
                        LinkStatsEntry lee = test(
                                new LinkStatsEntry(link, le.text(), linkStatsEntry.getMaxRequestCount(),
                                        linkStatsEntry.getMinRequestCount()), depth - 1);
                        if (null != lee) {
                            linkStatsEntry.addlinkStatsEntry(lee);
                        }
                    }
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return linkStatsEntry;
    }

    public void print(LinkStatsEntry linkStatsEntry, int deepth) {
        if (null != linkStatsEntry) {
            for (int i = 0; i < deepth; ++i)
                System.out.print(" ");
            String link = linkStatsEntry.getLink().length() > 32 ? linkStatsEntry.getLink().substring(0, 29) + "..."
                    : linkStatsEntry.getLink();
            System.out.println(String.format("%s\t\t%s\t%d\t%d\t%d\t%d\t%d\t", link, linkStatsEntry.getTitle(),
                    linkStatsEntry.getResponseCode(), linkStatsEntry.getRequestedCount(),
                    linkStatsEntry.getMaxResponseTime(), linkStatsEntry.getMinResponseTime(),
                    linkStatsEntry.getAvarageResponseTime()));
        }
    }

    public static void main(String[] args) throws IOException {
        System.out
                .println("Link\t\t\tTitle\tResponse Code\tRequested Count\tMax Response Time\tMin Response Time\tAvarage Response Time");
        new WebVisitableTester("http://10.111.131.68:9080/jpetstore", "jpetstore", 10, 1, 30, false).test();
    }
}
