package com.mitek.tree.util;

import com.mitek.tree.config.Constants;

/**
 * @author Sacumen(www.sacumen.com)
 * This class contains js script which will use to get image of selfie.
 */
public class SelfieScript {

    public static String getSelfieScript(String scriptURL, String verificationChoice, String styleFilePath) {
        String removeScript = RemoveElements.removeElements();
        return removeScript +
                "var loadJS = function(url, implementationCode, location){\r\n" +
                "var scriptTag = document.createElement('script');\r\n" +
                "scriptTag.id='mitekScript';\n" +
                "scriptTag.src = url;\r\n" +
                "var link = document.createElement('link');\r\n" +
                "link.rel = 'stylesheet';\r\n" +
                "link.type = 'text/css';\r\n" +
                "link.href = '" + styleFilePath + "';\r\n" +
                "document.getElementById('loginButton_0').style.display = 'none';\n" + "scriptTag.appendChild(link);\r\n" + "location.appendChild(scriptTag);\r\n" + "};\r\n" + "var input = document.createElement('input');\r\n" + "input.setAttribute('type', 'hidden');\r\n" + "input.setAttribute('id', 'integratorDocTypeInput');\r\n" + "input.setAttribute('value','" + verificationChoice + "');\r\n" + "document.body.appendChild(input);\r\n" +
                "var capturedTimeout = document.createElement('input');\n" + "capturedTimeout.id = 'capturedTimeout';\n" + "capturedTimeout.type = 'hidden';\n" + "capturedTimeout.value = '';\n" + "document.body.appendChild(capturedTimeout);\n" +
                "var interval = setInterval(function () {\n" + "var imageData = document.getElementById('capturedImage').src;\n" +
                "var result = imageData.startsWith('" + Constants.BASE64_STARTS_WITH + "');\n" +
                "if (result === true) " + "{\n" +
                "document.getElementById('captureSelfieResponse').value = imageData;\n" +
                "if (document.contains(document.getElementById('capturedImageContainer'))) {\n" +
                "var capturedImageContainer=document.getElementById('capturedImageContainer');\n" +
                "var selfieImage = document.createElement('input');\n" +
                "selfieImage.id = 'selfieImage';\n" +
                "selfieImage.type = 'hidden';\n" +
                "selfieImage.value = imageData;\n" +
                "capturedImageContainer.appendChild(selfieImage);\n" +
                "f2();\n" + "}\n" + "}\n" +
                "else if(document.getElementById('capturedTimeout').value=='timeout') {\n" +
                "document.getElementById('captureSelfieResponse').value = '';\n" +
                "f2();\n" + "}\n" + "}, 500);\n" +
                "function f2() {\n" + "clearInterval(interval);\n" + "document.getElementById('loginButton_0').click();\n" +
                "}\n" +
                "var yourCodeToBeCalled = function(){\r\n" + "}\r\n" + "loadJS(" + "\"" + scriptURL + "\"" + ", yourCodeToBeCalled, document.body);";

    }
}
