import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HTTPResponse {
    private final HTTPRequest httpRequest;
    private final String rootPath;

    public HTTPResponse(HTTPRequest httpRequest) {
        this.httpRequest = httpRequest;

        Config config = new Config("config.ini");
        this.rootPath = config.getRoot();
    }

    public void generateResponse(OutputStream output) {
        String type = httpRequest.getType();
        if (!type.equals("GET") && !type.equals("POST") && !type.equals("HEAD") && !type.equals("TRACE")) {
            sendNotImplementedResponse(output);
            return;
        }

        String requestedPage = httpRequest.getRequestedPage();
        Path filePath = Paths.get(rootPath + requestedPage);
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendNotFoundResponse(output);
            return;
        }

        try {
            File file = filePath.toFile();
            byte[] fileBytes = readFile(file);
            String contentType = getContentType(requestedPage);
            String contentLength = String.valueOf(fileBytes.length);

            sendOKResponse(output, fileBytes, contentType, contentLength);
        } catch (Exception e) {
            sendInternalServerErrorResponse(output);
        }
    }

    private void sendOKResponse(OutputStream output, byte[] fileBytes, String contentType, String contentLength) {
        String responseHeader = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + contentLength + "\r\n" +
                "\r\n";

        System.out.println("Response Header:");
        System.out.println(responseHeader);

        try {
            output.write(responseHeader.getBytes());
            output.write(fileBytes);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNotFoundResponse(OutputStream output) {
        sendResponse(output, 404, "Not Found", null);
    }

    private void sendNotImplementedResponse(OutputStream output) {
        sendResponse(output, 501, "Not Implemented", null);
    }

    private void sendBadRequestResponse(OutputStream output) {
        sendResponse(output, 400, "Bad Request", null);
    }

    private void sendInternalServerErrorResponse(OutputStream output) {
        sendResponse(output, 500, "Internal Server Error", null);
    }

    private void sendResponse(OutputStream output, int statusCode, String statusMessage, Exception e) {
        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n\r\n";
        System.out.println("Response Header:");
        System.out.println(response);

        try {
            output.write(response.getBytes());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        if (e != null) {
            e.printStackTrace();
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
            // do something
        } catch(IOException e) {
            // do something
        }
        return new byte[0];
    }
}
