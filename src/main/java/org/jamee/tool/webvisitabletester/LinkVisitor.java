package org.jamee.tool.webvisitabletester;

import java.net.URL;

public interface LinkVisitor {
    public void visite(URL url, String title);
}
