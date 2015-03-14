package org.jamee.tool.webvisitabletester;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WebVisitableTester {
    private int connectionTimeout = 5000; // 5s
    private int readTimeout = 5000; // 5s
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
        long maxLoadTime = 0;
        long minLoadTime = 0;
        long totalLoadTime = 0;
        int requestCount = getRequestCount(linkStatsEntry);
        for (int i = 0; i < requestCount; ++i) {
            try {
                long startTime = System.currentTimeMillis();
                int statusCode = loadURL(linkStatsEntry.getLink());
                long loadTime = System.currentTimeMillis() - startTime;
                minLoadTime = (minLoadTime == 0) ? loadTime : Math.min(minLoadTime, loadTime);
                maxLoadTime = Math.max(loadTime, maxLoadTime);
                totalLoadTime += loadTime;
                linkStatsEntry.setResponseCode(statusCode);
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

    private int loadURL(final String url) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(false); // don't rely on native redirection support
            conn.setConnectTimeout(connectionTimeout);
            conn.setReadTimeout(readTimeout);
//            Host: www.baidu.com
//            User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:36.0) Gecko/20100101 Firefox/36.0
//            Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//            Accept-Language: zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3
//            Accept-Encoding: gzip, deflate
            // Simulate Firefox Browser
            conn.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:36.0) Gecko/20100101 Firefox/36.0");
            conn.addRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.addRequestProperty("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
            conn.addRequestProperty("Accept-Encoding","gzip, deflate");

            conn.connect();
            int contentLength = conn.getHeaderFieldInt("Content-Length", 1024* 1024);
            byte[] buffer = new byte[1024];
            int read = 0;
            int remaining = contentLength;

            InputStream inStream = conn.getInputStream();
            while (remaining > 0) {
                read = inStream.read(buffer);
                if (read == -1) { break; }
                remaining -= read;
            }

            return conn.getResponseCode();
        } finally {
            // per Java's documentation, this is not necessary, and precludes keepalives. However in practise,
            // connection errors will not be released quickly enough and can cause a too many open files error.
            if (null != conn) {
                conn.disconnect();
            }
        }

    }

    private int getRequestCount(final LinkStatsEntry linkStatsEntry) {
        int requestCount = linkStatsEntry.getMaxRequestCount();
        if (linkStatsEntry.getMaxRequestCount() != linkStatsEntry.getMinRequestCount()) {
            Random r = new Random();
            requestCount = r.nextInt(linkStatsEntry.getMaxRequestCount());
            while (requestCount < linkStatsEntry.getMinRequestCount()) {
                requestCount = r.nextInt(linkStatsEntry.getMaxRequestCount());
            }
        }
        return requestCount;
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
        WebVisitableTester wvtester = new WebVisitableTester("https://www.baidu.com/", "baidu", 1, 1,
                1, false);
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
