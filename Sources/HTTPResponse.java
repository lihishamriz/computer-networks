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
        if (requestedPage.equals("/")){
            requestedPage = this.config.getDefaultPage();
        }
        if (requestedPage.startsWith("/secret/") && !httpRequest.getIsAuthenticated()) {
            sendUnauthorizedResponse();
            return;
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

            sendOKResponseWithBody(fileBytes, contentType, contentLength, this.httpRequest.getType().equals("GET"));
        } catch (Exception e) {
            sendInternalServerErrorResponse();
        }
    }

    private void handlePostRequest() {
        if (this.httpRequest.getRequestedPage().equals("/params_info.html")) {
            handleParamsInfo();
        } else if (this.httpRequest.getRequestedPage().equals("/upload")) {
            handleFileUpload();
        } else {
            sendOKResponse();
        }
    }

    private void handleTraceRequest() {
        String raw = this.httpRequest.getRawRequestHeader();
        sendOKResponseWithBody(raw.getBytes(StandardCharsets.UTF_8), "application/octet-stream", String.valueOf(raw.length()), true);
    }

    private void sendOKResponseWithBody(byte[] fileBytes, String contentType, String contentLength, Boolean includeBody) {
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

    private void sendOKResponse() {
        sendResponse(200, "OK");
    }

    private void sendBadRequestResponse() {
        sendResponse(400, "Bad Request");
    }

    private void sendUnauthorizedResponse() {
        sendResponse(401, "Unauthorized");
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

    private void handleParamsInfo() {
        HashMap<String, String> parameters = this.httpRequest.getParameters();
        File file = Paths.get(this.config.getRoot() + "/params_info.html").toFile();
        byte[] fileBytes = readFile(file);

        String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
        fileContent = fileContent.replace("[message]", parameters.get("message") != null ? parameters.get("message") : "");
        fileContent = fileContent.replace("[subscribe]", parameters.get("subscribe") != null ? "Yes" : "No");

        byte[] htmlBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        sendOKResponseWithBody(htmlBytes, "text/html", String.valueOf(htmlBytes.length), true);
    }

    private void handleFileUpload() {
        try {
            String fileName = "";
            String body = "";

            String[] headersAndBody = this.httpRequest.getRawRequestBody().split("\r\n\r\n", 2);
            if (headersAndBody.length == 2) {
                String headers = headersAndBody[0];
                body = headersAndBody[1];

                String[] headerLines = headers.split("\r\n");
                for (String line : headerLines) {
                    if (line.startsWith("Content-Disposition") && line.contains("filename=")) {
                        String[] parts = line.split("; ");
                        for (String part : parts) {
                            if (part.startsWith("filename=")) {
                                fileName = part.substring("filename=\"".length(), part.length() - 1);
                            }
                        }
                    }
                }

                String contentType = this.httpRequest.getContentType();
                String boundary = contentType.split("boundary=")[1];

                int index = body.indexOf("--" + boundary) - 2;
                body = body.substring(0, index);
            }

            byte[] fileBytes = body.getBytes();
            Path filePath = Paths.get(config.getRoot(), fileName);
            Files.write(filePath, fileBytes);

            String htmlResponse = "<html><body>" +
                    "<h2>File uploaded successfully!</h2>" +
                    "<p><a href=\"" + fileName + "\">Go to file!</a></p>" +
                    "</body></html>";

            sendOKResponseWithBody(htmlResponse.getBytes(), getContentType(fileName), String.valueOf(htmlResponse.length()), true);
        } catch (Exception e) {
            sendInternalServerErrorResponse();
        }
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "html" -> "text/html";
            case "bmp", "gif", "png", "jpg" -> "image";
            case "ico" -> "icon";
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
