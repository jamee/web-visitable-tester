package org.jamee.tool.webvisitabletester;

import java.io.IOException;

public interface LinksTraveller {
    public void accept(String link, LinkVisitor visitor) throws IOException;
}
