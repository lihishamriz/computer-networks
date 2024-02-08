import java.util.HashMap;

public class HTTPRequest {
    private final String type;
    private final String requestedPage;
    private boolean isImage = false;
    private int contentLength;
    private String referer;
    private String userAgent;
    private final HashMap<String, String> parameters;

    public HTTPRequest(String requestHeader) {
        parameters = new HashMap<>();

        String[] lines = requestHeader.split("\\r?\\n");

        String[] firstLineParts = lines[0].split(" ");
        type = firstLineParts[0];
        requestedPage = firstLineParts[1];

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
            }
        }

        int index = requestedPage.indexOf('?');
        if (index != -1) {
            String paramsString = requestedPage.substring(index + 1);
            String[] params = paramsString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
        }
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
}
