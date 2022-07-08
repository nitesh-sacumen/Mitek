package com.mitek.tree.util;

public class VerificationOptionsScript {
    public static String getVerificationOptionsScript() {
        String removeScript = RemoveElements.removeElements();
        String footerScript = FooterScript.getFooterScript();
        return removeScript + footerScript +
                "document.getElementById('loginButton_0').click();";
    }
}
