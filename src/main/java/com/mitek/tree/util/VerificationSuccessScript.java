package com.mitek.tree.util;

/**
 * @author Sacumen(www.sacumen.com)
 * This class contains js script which will be used to get verification success script.
 */
public class VerificationSuccessScript {
    public static String getVerificationSuccessScript() {
        String removeScript = RemoveElements.removeElements();
        String footerScript = FooterScript.getFooterScript();
        return removeScript + footerScript;
    }
}
