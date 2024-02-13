import java.io.BufferedReader;
import java.util.HashMap;

public class HTTPRequest {
    private String rawRequest;
    private String type;
    private String requestedPage;
    private boolean isImage = false;
    private int contentLength;
    private String referer;
    private String userAgent;
    private HashMap<String, String> parameters;
    private boolean isChunked = false;
    private boolean isBadRequest = false;

    public HTTPRequest(String requestHeader, BufferedReader reader) {
        try {
            System.out.println("Request header:\n" + requestHeader +"\n");

            rawRequest = requestHeader;
            parameters = new HashMap<>();

            String[] lines = requestHeader.split("\\r?\\n");

            String[] firstLineParts = lines[0].split(" ");
            type = firstLineParts[0];
            requestedPage = firstLineParts[1].replaceAll("\\.\\./", "");

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
                } else if (line.equals("chunked: yes")) {
                    isChunked = true;
                }
            }

            if (contentLength > 0) {
                char[] buffer = new char[contentLength];
                reader.read(buffer, 0, contentLength);
                String requestBody = new String(buffer);

                if (!requestBody.isEmpty()) {
                    String[] params = requestBody.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2) {
                            parameters.put(keyValue[0], keyValue[1]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            isBadRequest = true;
        }
    }

    public String getRawRequest() {
        return rawRequest;
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
}
