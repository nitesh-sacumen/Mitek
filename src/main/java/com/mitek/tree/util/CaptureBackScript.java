package com.mitek.tree.util;

/**
 * @author Sacumen(www.sacumen.com)
 * This class contains js script which will use to get back side image of document.
 */
public class CaptureBackScript {
    public static String getCaptureBackScript(String scriptURL, String verificationChoice) {
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
                "var input = document.createElement('input');\r\n" + "input.setAttribute('type', 'hidden');\r\n" +
                "input.setAttribute('id', 'integratorDocTypeInput');\r\n" +
                "input.setAttribute('value','" + verificationChoice + "');\r\n" +
                "document.body.appendChild(input);\r\n" +

                "var capturedTimeout = document.createElement('input');\n" +
                "capturedTimeout.id = 'capturedTimeout';\n" +
                "capturedTimeout.type = 'hidden';\n" +
                "capturedTimeout.value = '';\n" +
                "document.body.appendChild(capturedTimeout);\n" +

                "var interval = setInterval(function () {\n" +
                "var codeData = document.getElementById('capturedBackImageCode').value;\n" +
                "var result = codeData.includes('*');\n" +
                "if (result === true) {\n" +
                "document.getElementById('captureBackResponse').value = codeData;\n" +
                "var backData = document.getElementById('capturedBackImage').src;\n" +
                "document.getElementById('captureBack').value = backData;\n" +

                "if (document.contains(document.getElementById('capturedImageContainer'))) {\n" +
                "var capturedImageContainer=document.getElementById('capturedImageContainer');\n" +
                "var backImageData = document.createElement('input');\n" +
                "backImageData.id = 'backImageData';\n" +
                "backImageData.type = 'hidden';\n" +
                "backImageData.value = backData;\n" +
                "capturedImageContainer.appendChild(backImageData);\n" +
                "f2();\n" +
                "}\n" + "}\n" +
                "else if(document.getElementById('capturedTimeout').value=='timeout') {\n" +
                "document.getElementById('captureBackResponse').value = '';\n" +
                "f2();\n" +
                "}\n" +
                "}, 500);\n" +

                "function f2() {\n" +
                "clearInterval(interval);\n" +
                "document.getElementById('loginButton_0').click();\n" +
                "}\n" +
                "var yourCodeToBeCalled = function(){\r\n" +
                "}\r\n" + "loadJS(" + "\"" + scriptURL + "\"" + ", yourCodeToBeCalled, document.body);";
    }
}
