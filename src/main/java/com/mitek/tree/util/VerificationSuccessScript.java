package com.mitek.tree.util;

public class VerificationSuccessScript {
    public static String getVerificationSuccessScript() {
        String removeScript = RemoveElements.removeElements();
        String footerScript = FooterScript.getFooterScript();
        return removeScript + footerScript;
    }
}
