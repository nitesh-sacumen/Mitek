package com.mitek.tree.util;

/**
 * @author Sacumen(www.sacumen.com)
 * This class contains js script which will be used to get verification retry script.
 */
public class VerificationRetryScript {
    public static String getVerificationRetryScript() {
        String removeScript = RemoveElements.removeElements();
        String footerScript = FooterScript.getFooterScript();
        return removeScript + footerScript;
    }
}
