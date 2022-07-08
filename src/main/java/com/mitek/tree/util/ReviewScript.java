package com.mitek.tree.util;

/**
 * @author Saucmen(www.sacumen.com)
 * This class contains js script which will use to get image of front, back, selfie and passport.
 */
public class ReviewScript {
    public static String getReviewScript(Integer retakeCount, Integer maxRetakeCount) {
        String removeScript = RemoveElements.removeElements();
        return removeScript +
                "document.getElementById('loginButton_0').style.display='none';\n" +
                "if (document.contains(document.getElementById('footer'))) {\n" +
                "document.getElementById('footer').style.marginBottom='-50%';\n" +
                "}\n" +
                "if (document.contains(document.getElementById('frontImage')) || document.contains(document.getElementById('passportImage'))) {\n" +
                "var parentDiv=document.createElement('div');\n" + "parentDiv.id='parentDiv';\n" +
                "parentDiv.className='float-container';\n" + "var div=document.createElement('div');\n" +
                "div.id='imageContainer';\n" +
                "if (document.contains(document.getElementById('frontImage'))) {\n" +
                "var frontImage = document.getElementById('frontImage').value;\n" +
                "document.getElementById('front').value = frontImage;\n" +
                "var img = document.createElement('img');\n" +
                "img.id='frontImg';\n" +
                "img.src = frontImage;\n" +
                "img.className='float-child-image';\n" +
                "div.appendChild(img);" + "}\n" +
                "else{\n" +
                "document.getElementById('front').value = '';\n" + "}\n" +
                "if (document.contains(document.getElementById('backImageData'))) {\n" +
                "var backImageData = document.getElementById('backImageData').value;\n" +
                "document.getElementById('back').value = backImageData;\n" +
                "var img = document.createElement('img');\n" +
                "img.id='backImg';\n" +
                "img.src = backImageData;\n" +
                "img.className='float-child-image';\n" +
                "div.appendChild(img);" +
                "}\n" +
                "else{\n" +
                "document.getElementById('back').value = '';\n" + "}\n" +
                "if (document.contains(document.getElementById('passportImage'))) {\n" +
                "var passportImage = document.getElementById('passportImage').value;\n" +
                "document.getElementById('passport').value = passportImage;\n" +
                "var img = document.createElement('img');\n" +
                "img.id='passportImg';\n" +
                "img.src = passportImage;\n" +
                "img.className='float-child-image';\n" +
                "div.appendChild(img);" + "}\n" +
                "else{\n" + "document.getElementById('passport').value = '';\n" +
                "}\n" + "if (document.contains(document.getElementById('selfieImage'))) {\n" +
                "var selfieImage = document.getElementById('selfieImage').value;\n" +
                "document.getElementById('selfie').value = selfieImage;\n" +
                "var img1 = document.createElement('img');\n" +
                "img1.id='selfieImg';\n" +
                "img1.src = selfieImage;\n" + "img1.className='float-child-image';\n" +
                "div.appendChild(img1);" + "}\n" + "else{\n" + "document.getElementById('selfie').value = '';\n" +
                "}\n" + "var buttonDiv=document.createElement('div');\n" + "buttonDiv.id='buttonContainer';\n" +
                "var button = document.createElement('button');\n" + "button.id = 'captureRetake';\n" +
                "button.innerHTML = 'Retake'\n" + "button.className = 'btn btn-block btn-primary';\n" +
                "if(" + retakeCount + "===" + maxRetakeCount + "){\n" + "button.disabled = true;\n" + "};\n" +
                "button.onclick = function() {\n" + "document.getElementById('isRetake').value = 'true'\n" +
                "document.getElementById('loginButton_0').click();\n" + "};\n" +
                "var button1 = document.createElement('button');\n" +
                "button1.id = 'captureSubmit';\n" + "button1.innerHTML = 'Submit'\n" +
                "button1.className = 'btn btn-block btn-primary';\n" + "button1.onclick = function() {\n" +
                "button1.disabled = 'true';\n" +
                "document.getElementById('isRetake').value = 'false'\n" +
                "document.getElementById('loginButton_0').click();\n" + "};\n" +
                "buttonDiv.appendChild(button)\n;" + "buttonDiv.appendChild(button1)\n;" +
                "parentDiv.appendChild(div);\n" + "parentDiv.appendChild(buttonDiv);\n" +
                "var scriptTag = document.createElement('script');\r\n" +
                "scriptTag.id='mitekScript';\n" +
                "scriptTag.src = '';\r\n" +
                "var link = document.createElement('link');\r\n" +
                "link.rel = 'stylesheet';\r\n" +
                "link.type = 'text/css';\r\n" +
                "link.href = '/mitek/style.css';\r\n" +
                "scriptTag.appendChild(link);\r\n" +
                "document.body.appendChild(scriptTag);\r\n" +
                "if (document.contains(document.getElementById('capturedImageContainer'))) {\n" +
                "document.getElementById('capturedImageContainer').remove();\n" + "}\n" +
                "document.body.appendChild(parentDiv);\n" + "}\n";
    }
}
