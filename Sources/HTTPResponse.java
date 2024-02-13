import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HTTPResponse {
    private final HTTPRequest httpRequest;
    private final OutputStream output;
    private final Config config;

    public HTTPResponse(HTTPRequest httpRequest, OutputStream output) {
        this.httpRequest = httpRequest;
        this.output = output;
        this.config = new Config("config.ini");
    }

    public void generateResponse() {
        if (httpRequest.getIsBadRequest()) {
            sendBadRequestResponse();
            return;
        }

        String type = httpRequest.getType();
        switch (type) {
            case "GET":
            case "HEAD":
                handleGetAndHeadRequest();
            break;
            case "POST":
                handlePostRequest();
                break;
            case "TRACE":
                handleTraceRequest();
                break;
            default:
                sendNotImplementedResponse();
        }
    }

    private void handleGetAndHeadRequest() {
        String requestedPage = httpRequest.getRequestedPage();
        if(requestedPage.equals("/")){
            requestedPage = this.config.getDefaultPage();
        }
        Path filePath = Paths.get(this.config.getRoot() + requestedPage);
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendNotFoundResponse();
            return;
        }

        try {
            File file = filePath.toFile();
            byte[] fileBytes = readFile(file);
            String contentType = getContentType(requestedPage);
            String contentLength = String.valueOf(fileBytes.length);

            sendOKResponse(fileBytes, contentType, contentLength, this.httpRequest.getType().equals("GET"));
        } catch (Exception e) {
            sendInternalServerErrorResponse();
        }
    }

    private void handlePostRequest() {
        HashMap<String, String> parameters = this.httpRequest.getParameters();
        if (this.httpRequest.getRequestedPage().equals("/params_info.html")) {
            File file = Paths.get(this.config.getRoot() + "/params_info.html").toFile();
            byte[] fileBytes = readFile(file);

            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
            fileContent = fileContent.replace("[message]", parameters.get("message") != null ? parameters.get("message") : "");
            fileContent = fileContent.replace("[subscribe]", parameters.get("subscribe") != null ? "Yes" : "No");

            byte[] htmlBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            sendOKResponse(htmlBytes, "text/html", String.valueOf(htmlBytes.length), true);
        }
    }

    private void handleTraceRequest() {
        String raw = this.httpRequest.getRawRequest();
        sendOKResponse(raw.getBytes(StandardCharsets.UTF_8), "application/octet-stream", String.valueOf(raw.length()), true);
    }

    private void sendOKResponse(byte[] fileBytes, String contentType, String contentLength, Boolean includeBody) {
        boolean isChunked = this.httpRequest.getIsChunked();
        String responseHeader = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n";


        if (isChunked) {
            responseHeader += "Transfer-Encoding: chunked\r\n\r\n";
        } else {
            responseHeader += "Content-Length: " + contentLength + "\r\n\r\n";
        }

        System.out.println("Response header:\n" + responseHeader);

        try {
            output.write(responseHeader.getBytes());

            if (includeBody) {
                if (isChunked) {
                    int chunkSize = 1024;
                    for (int i = 0; i < fileBytes.length; i += chunkSize) {
                        int length = Math.min(chunkSize, fileBytes.length - i);
                        output.write(Integer.toHexString(length).getBytes(StandardCharsets.UTF_8));
                        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
                        output.write(fileBytes, i, length);
                        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
                    }
                    output.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                } else {
                    output.write(fileBytes);
                }
            }

            output.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendBadRequestResponse() {
        sendResponse(400, "Bad Request");
    }

    private void sendNotFoundResponse() {
        sendResponse(404, "Not Found");
    }

    private void sendInternalServerErrorResponse() {
        sendResponse(500, "Internal Server Error");
    }

    private void sendNotImplementedResponse() {
        sendResponse(501, "Not Implemented");
    }

    private void sendResponse(int statusCode, String statusMessage) {
        String responseHeader = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n\r\n";
        System.out.println("Response header:\n" + responseHeader);

        try {
            output.write(responseHeader.getBytes());
        } catch (IOException ioException) {
            sendInternalServerErrorResponse();
        }
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "html" -> "text/html";
            case "bmp" -> "image/bmp";
            case "gif" -> "image/gif";
            case "png" -> "image/png";
            case "jpg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    private byte[] readFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bFile = new byte[(int)file.length()];

            // read until the end of the stream.
            while(fis.available() != 0) {
                fis.read(bFile, 0, bFile.length);
            }

            return bFile;
        }
        catch(FileNotFoundException e) {
            sendNotFoundResponse();
        } catch(IOException e) {
            sendInternalServerErrorResponse();
        }
        return new byte[0];
    }
}
