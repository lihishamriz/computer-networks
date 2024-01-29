import java.util.HashMap;

public class HTTPRequest {
    private String type;
    private String requestedPage;
    private boolean isImage = false;
    private int contentLength;
    private String referer;
    private String userAgent;
    private HashMap<String, String> parameters;

    private String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp", ".svg"};

    public HTTPRequest(String requestHeader) {
        parameters = new HashMap<>();

        String[] lines = requestHeader.split("\\r?\\n");

        String[] firstLineParts = lines[0].split(" ");
        type = firstLineParts[0];
        requestedPage = firstLineParts[1];
        
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

    public static void main(String[] args) {
        String header = "GET /index.html?q=hello&lang=en HTTP/1.1\n" +
                        "Host: www.example.com\n" +
                        "User-Agent: Mozilla/5.0\n" +
                        "Referer: http://www.google.com\n" +
                        "Content-Length: 100\n";

        HTTPRequest httpRequest = new HTTPRequest(header);

        System.out.println("Type: " + httpRequest.getType());
        System.out.println("Requested Page: " + httpRequest.getRequestedPage());
        System.out.println("Is Image: " + httpRequest.getIsImage());
        System.out.println("Content Length: " + httpRequest.getContentLength());
        System.out.println("Referer: " + httpRequest.getReferer());
        System.out.println("User Agent: " + httpRequest.getUserAgent());
        System.out.println("Parameters: " + httpRequest.getParameters());
    }
}
