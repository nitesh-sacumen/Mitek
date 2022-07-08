package com.mitek.tree.util;

public class FooterScript {

    public static String getFooterScript()
    {
        return "if (document.contains(document.getElementById('footer'))) {\n" +
                "document.getElementById('footer').style.marginBottom='0px';\n" +
                "}\n" ;
    }
}
