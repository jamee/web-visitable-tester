package org.jamee.tool.webvisitabletester;

import java.net.URL;

public interface LinkVisitor {
    public void visit(URL url, String title);
}
