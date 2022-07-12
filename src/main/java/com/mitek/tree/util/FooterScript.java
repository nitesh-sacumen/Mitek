package com.mitek.tree.util;

/**
 * @author Sacumen(www.sacumen.com)
 * This class contains js script which will use to set footer margin of the document.
 */
public class FooterScript {

    public static String getFooterScript() {
        return "if (document.contains(document.getElementById('footer'))) {\n" +
                "document.getElementById('footer').style.marginBottom='0px';\n" +
                "}\n";
    }
}
