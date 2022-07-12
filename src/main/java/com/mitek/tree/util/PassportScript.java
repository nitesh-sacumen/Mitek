package com.mitek.tree.util;

import com.mitek.tree.config.Constants;

/**
 * @author Sacumen(www.sacumen.com)
 * This class contains js script which will use to get image of passport.
 */
public class PassportScript {
    public static String getScript(Boolean isVerificationRefresh) {
        String removeScript = RemoveElements.removeElements();
        String footerScript = FooterScript.getFooterScript();
        return removeScript +
                "if(" + isVerificationRefresh + "){\n" +
                footerScript +
                "}\n";
    }

    public static String getPassportScript(String scriptURL, String verificationChoice) {
        return "var loadJS = function(url, implementationCode, location){\r\n" +
                "var scriptTag = document.createElement('script');\r\n" +
                "scriptTag.id='mitekScript';\n" +
                "scriptTag.src = url;\r\n" +
                "var link = document.createElement('link');\r\n" +
                "link.rel = 'stylesheet';\r\n" +
                "link.type = 'text/css';\r\n" +
                "link.href = '/mitek/style.css';\r\n" +
                "document.getElementById('loginButton_0').style.display = 'none';\n" +
                "scriptTag.appendChild(link);\r\n" +
                "location.appendChild(scriptTag);\r\n" + "};\r\n" +
                "var input = document.createElement('input');\r\n" +
                "input.setAttribute('type', 'hidden');\r\n" + "input.setAttribute('id', 'integratorDocTypeInput');\r\n" + "input.setAttribute('value','" + verificationChoice + "');\r\n" + "document.body.appendChild(input);\r\n" +

                "var capturedTimeout = document.createElement('input');\n" + "capturedTimeout.id = 'capturedTimeout';\n" +
                "capturedTimeout.type = 'hidden';\n" + "capturedTimeout.value = '';\n" +
                "document.body.appendChild(capturedTimeout);\n" +
                "var interval = setInterval(function () {\n" +
                "var imageData = document.getElementById('capturedImage').src;\n" +
                "var result = imageData.startsWith('" + Constants.BASE64_STARTS_WITH + "');\n" +
                "if (result === true) " +
                "{\n" +
                "var capturedImageContainer = document.createElement('div');\n" +
                "capturedImageContainer.id='capturedImageContainer';\n" +
                "document.getElementById('capturePassportResponse').value = imageData;\n" +
                "var passportImage = document.createElement('input');\n" +
                "passportImage.id = 'passportImage';\n" +
                "passportImage.type = 'hidden';\n" +
                "passportImage.value = imageData;\n" +
                "capturedImageContainer.appendChild(passportImage);\n" +
                "document.body.appendChild(capturedImageContainer);\n" +
                "f2();\n" + "}\n" +
                "else if(document.getElementById('capturedTimeout').value=='timeout') {\n" +
                "document.getElementById('capturePassportResponse').value = '';\n" +
                "f2();\n" + "}\n" + "}, 500);\n" +
                "function f2() {\n" + "clearInterval(interval);\n" +
                "document.getElementById('loginButton_0').click();\n" + "}\n" + "var yourCodeToBeCalled = function(){\r\n" + "}\r\n" + "loadJS(" + "\"" + scriptURL + "\"" + ", yourCodeToBeCalled, document.body);";
    }
}
