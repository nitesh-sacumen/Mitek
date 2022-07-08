package com.mitek.tree.util;

public class VerificationRetryScript {
    public static String getVerificationRetryScript() {
        String removeScript = RemoveElements.removeElements();
        String footerScript = FooterScript.getFooterScript();
        return removeScript + footerScript;
    }
}
