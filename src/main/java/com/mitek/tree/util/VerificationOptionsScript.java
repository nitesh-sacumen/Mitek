package com.mitek.tree.util;

/**
 * @author Sacumen(www.sacumen.com)
 * This class contains js script which will be used to get verification options script.
 */
public class VerificationOptionsScript {
    public static String getVerificationOptionsScript() {
        String removeScript = RemoveElements.removeElements();
        String footerScript = FooterScript.getFooterScript();
        return removeScript + footerScript +
                "document.getElementById('loginButton_0').click();";
    }
}
