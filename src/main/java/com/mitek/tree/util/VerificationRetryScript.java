package com.mitek.tree.util;

public class VerificationRetryScript {
    public String getVerificationRetryScript() {

        return "if (document.contains(document.getElementById('footer'))) {\n" +
                "document.getElementById('footer').style.marginBottom='0px';\n" +
                "}\n" +

                "if (document.contains(document.getElementById('parentDiv'))) {\n" +
                "document.getElementById('parentDiv').remove();\n" +
                "}\n" +


                "if (document.contains(document.getElementById('mitekMediaContainer'))) {\n" +
                "document.getElementById('mitekMediaContainer').remove();\n" +
                "}\n" +


                "if (document.contains(document.getElementById('uiContainer'))) {\n" +
                "document.getElementById('uiContainer').remove();\n" +
                "}\n" +

                "if (document.contains(document.getElementById('mitekScript'))) {\n" +
                "document.getElementById('mitekScript').remove();\n" +
                "}\n" +

                "if (document.contains(document.getElementById('capturedTimeout'))) {\n" +
                "document.getElementById('capturedTimeout').remove();\n" +
                "}\n" +

                "if (document.contains(document.getElementById('hidden'))) {\n" +
                "document.getElementById('hidden').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('capturedImageContainer'))) {\n" +
                "document.getElementById('capturedImageContainer').remove();\n" +
                "}\n";
    }
}
