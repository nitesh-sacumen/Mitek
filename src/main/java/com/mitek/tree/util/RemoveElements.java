package com.mitek.tree.util;

public class RemoveElements {

    public static String removeElements() {

        return "if (document.contains(document.getElementById('parentDiv'))) {\n" +
                "document.getElementById('parentDiv').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('integratorDocTypeInput'))) {\n" +
                "document.getElementById('integratorDocTypeInput').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('capturedTimeout'))) {\n" +
                "document.getElementById('capturedTimeout').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('mitekScript'))) {\n" +
                "document.getElementById('mitekScript').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('integratorAutoCaptureButton'))) {\n" +
                "document.getElementById('integratorAutoCaptureButton').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('integratorManualCaptureButton'))) {\n" +
                "document.getElementById('integratorManualCaptureButton').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('uiContainer'))) {\n" +
                "document.getElementById('uiContainer').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('mitekMediaContainer'))) {\n" +
                "document.getElementById('mitekMediaContainer').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('hidden'))) {\n" +
                "document.getElementById('hidden').remove();\n" +
                "}\n";

    }
}
