package com.mitek.tree.util;

public class VerificationFailureScript {

    public static String getVerificationFailureScript() {
        String removeScript = RemoveElements.removeElements();
        String footerScript = FooterScript.getFooterScript();
        return removeScript + footerScript;
    }
}
