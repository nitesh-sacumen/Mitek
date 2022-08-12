package com.mitek.tree.util;

/**
 * @author Sacumen(www.sacumen.com)
 * This class contains js script which will use in consent page.
 */
public class ConsentScript {
    public static String getConsentScript() {
        String footerScript = FooterScript.getFooterScript();
        String removeScript = RemoveElements.removeElements();
        return removeScript + footerScript;
    }
}
