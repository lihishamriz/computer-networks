import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Base64;

public class HTTPRequest {
    private String rawRequestHeader;
    private String rawRequestBody;
    private String type;
    private String requestedPage;
    private boolean isImage = false;
    private int contentLength;
    private String contentType;
    private String referer;
    private String userAgent;
    private HashMap<String, String> parameters;
    private boolean isChunked = false;
    private boolean isAuthenticated = false;
    private boolean isBadRequest = false;

    public HTTPRequest(String requestHeader, BufferedReader reader) {
        Config config = new Config("config.ini");

        try {
            System.out.println("Request header:\n" + requestHeader + "\n");

            rawRequestHeader = requestHeader;
            parameters = new HashMap<>();

            String[] lines = requestHeader.split("\\r?\\n");

            String[] firstLineParts = lines[0].split(" ");
            type = firstLineParts[0];
            String[] pageParts = firstLineParts[1].split("\\?");
            requestedPage = pageParts[0].replaceAll("\\.\\./", "");
            if (pageParts.length > 1) {
                parseParameters(pageParts[1]);
            }
            if (requestedPage.endsWith("/")) {
                requestedPage = requestedPage.substring(0, requestedPage.length() - 1);
            }

            String[] imageExtensions = {".bmp", ".gif", ".png", ".jpg"};
            for (String extension : imageExtensions) {
                if (requestedPage.toLowerCase().endsWith(extension)) {
                    isImage = true;
                    break;
                }
            }

            for (String line : lines) {
                if (line.startsWith("Referer: ")) {
                    referer = line.substring(9);
                } else if (line.startsWith("User-Agent: ")) {
                    userAgent = line.substring(12);
                } else if (line.startsWith("Content-Length: ")) {
                    contentLength = Integer.parseInt(line.substring(16));
                } else if (line.startsWith("Content-Type: ")) {
                    contentType = line.substring(14);
                } else if (line.startsWith("Authorization: Basic ")) {
                    String base64Credentials = line.substring(21).trim();
                    String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
                    String[] parts = credentials.split(":", 2);
                    if (parts.length == 2) {
                        isAuthenticated = parts[0].equals(config.getServerUsername()) && parts[1].equals(config.getServerPassword());
                    }
                } else if (line.equals("chunked: yes")) {
                    isChunked = true;
                }
            }

            if (contentLength > 0) {
                char[] buffer = new char[contentLength];
                reader.read(buffer, 0, contentLength);
                rawRequestBody = new String(buffer);
                parseParameters(rawRequestBody);
            }
        } catch (Exception e) {
            isBadRequest = true;
        }
    }

    private void parseParameters(String rawParameters) {
        if (!rawParameters.isEmpty()) {
            String[] params = rawParameters.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    public String getRawRequestHeader() {
        return rawRequestHeader;
    }

    public String getRawRequestBody() {
        return rawRequestBody;
    }

    public String getType() {
        return type;
    }

    public String getRequestedPage() {
        return requestedPage;
    }

    public boolean getIsImage() {
        return isImage;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public String getReferer() {
        return referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public boolean getIsChunked() {
        return isChunked;
    }

    public boolean getIsBadRequest() {
        return isBadRequest;
    }

    public boolean getIsAuthenticated() {
        return isAuthenticated;
    }
}
