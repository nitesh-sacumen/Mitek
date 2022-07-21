package com.mitek.tree.util;

/**
 * @author Sacumen(www.sacumen.com)
 * This class contains js script which will be used to get verification failure script.
 */
public class VerificationFailureScript {

    public static String getVerificationFailureScript() {
        String removeScript = RemoveElements.removeElements();
        String footerScript = FooterScript.getFooterScript();
        return removeScript + footerScript;
    }
}
