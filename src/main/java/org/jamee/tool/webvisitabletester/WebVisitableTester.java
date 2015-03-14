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

public class WebVisitableTester {
    private int maxTestDepth;
    private Set<String> requestedLinks;
    private boolean testExternalLink;
    private LinkStatsEntry rootlinkStatsEntry;
    private LinksTraveller linksTranveller;
    private LinkStatsStreamRender linkStatsStreamRender;

    public WebVisitableTester(String rootLink, String rootLinkTitle, int maxRequests, int minRequests,
            int maxTestDepth, boolean testExternalLink) {
        super();
        this.requestedLinks = new HashSet<String>();
        this.maxTestDepth = maxTestDepth;
        this.testExternalLink = testExternalLink;
        this.rootlinkStatsEntry = new LinkStatsEntry(rootLink, rootLinkTitle, maxRequests, minRequests);
    }

    public void setLinksTranveller(LinksTraveller linksTranveller) {
        this.linksTranveller = linksTranveller;
    }

    public void setLinkStatsStreamRender(LinkStatsStreamRender linkStatsStreamRender) {
        this.linkStatsStreamRender = linkStatsStreamRender;
    }

    public LinkStatsEntry test() {
        return test(this.rootlinkStatsEntry, this.maxTestDepth);
    }

    public LinkStatsEntry test(final LinkStatsEntry linkStatsEntry, final int depth) {
        if (depth == 0) { return null; }
        testLinkVisitable(linkStatsEntry);
        linkStatsStreamRender.render(linkStatsEntry, this.maxTestDepth - depth);
        testResolvedLinks(linkStatsEntry, depth);
        return linkStatsEntry;
    }

    private void testLinkVisitable(final LinkStatsEntry linkStatsEntry) {
        int requestCount = linkStatsEntry.getMaxRequestCount();
        if (linkStatsEntry.getMaxRequestCount() != linkStatsEntry.getMinRequestCount()) {
            Random r = new Random();
            requestCount = r.nextInt(linkStatsEntry.getMaxRequestCount());
            while (requestCount < linkStatsEntry.getMinRequestCount()) {
                requestCount = r.nextInt(linkStatsEntry.getMaxRequestCount());
            }
        }
        long maxLoadTime = 0;
        long minLoadTime = 0;
        long totalLoadTime = 0;
        for (int i = 0; i < requestCount; ++i) {
            try {
                long startTime = System.currentTimeMillis();
                Connection conn = Jsoup.connect(linkStatsEntry.getLink());
                conn.execute();
                Response response = conn.response();
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
        requestedLinks.add(linkStatsEntry.getLink());
    }

    private void testResolvedLinks(final LinkStatsEntry linkStatsEntry, final int depth) {
        try {
            this.linksTranveller.accept(linkStatsEntry.getLink(), new LinkVisitor() {
                public void visite(URL url, String title) {
                    try {
                        URL rootLinkURL = new URL(rootlinkStatsEntry.getLink());
                        if (!testExternalLink && !url.getHost().equals(rootLinkURL.getHost())) {
                            return;
                        }
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    String normalURL = url.toExternalForm();
                    if (!requestedLinks.contains(normalURL)) {
                        LinkStatsEntry lee = test(
                                new LinkStatsEntry(normalURL, title, linkStatsEntry.getMaxRequestCount(),
                                        linkStatsEntry.getMinRequestCount()), depth - 1);
                        if (null != lee) {
                            linkStatsEntry.addlinkStatsEntry(lee);
                        }
                    } 
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out
                .println("Link\t\t\tTitle\tResponse Code\tRequested Count\tMax Response Time\tMin Response Time\tAvarage Response Time");
        WebVisitableTester wvtester = new WebVisitableTester("http://www.baidu.com/", "baidu", 5, 1,
                2, true);
        wvtester.setLinkStatsStreamRender(new LinkStatsStreamRender() {
            public void render(LinkStatsEntry linkStatsEntry, int depth) {
                for (int i = 0; i < depth; ++i)
                    System.out.print(" ");
                String link = linkStatsEntry.getLink().length() > 32 ? linkStatsEntry.getLink().substring(0, 29)
                        + "..." : linkStatsEntry.getLink();
                System.out.println(String.format("%s\t\t%s\t%d\t%d\t%d\t%d\t%d\t", link, linkStatsEntry.getTitle(),
                        linkStatsEntry.getResponseCode(), linkStatsEntry.getRequestedCount(),
                        linkStatsEntry.getMaxResponseTime(), linkStatsEntry.getMinResponseTime(),
                        linkStatsEntry.getAvarageResponseTime()));
            }
        });
        wvtester.setLinksTranveller(new JsoupLinkTraveller());
        wvtester.test();
    }
}
