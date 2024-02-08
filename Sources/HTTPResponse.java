import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HTTPResponse {
    private final HTTPRequest httpRequest;
    private final String rootPath;

    public HTTPResponse(HTTPRequest httpRequest, String rootPath) {
        this.httpRequest = httpRequest;
        this.rootPath = rootPath;
    }

    public void generateResponse(OutputStream output) {
        String type = httpRequest.getType();
        if (!type.equals("GET") && !type.equals("POST") && !type.equals("HEAD") && !type.equals("TRACE")) {
            sendResponse(output, 501, "Not Implemented", null);
            return;
        }

        String requestedPage = httpRequest.getRequestedPage();
        Path filePath = Paths.get(rootPath + requestedPage);
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendResponse(output, 404, "Not Found", null);
            return;
        }

        try {
            File file = filePath.toFile();
            byte[] fileBytes = readFile(file);
            String contentType = getContentType(requestedPage);
            String contentLength = String.valueOf(fileBytes.length);

            String responseHeader = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + contentLength + "\r\n" +
                    "\r\n";

            System.out.println("Response Header:");
            System.out.println(responseHeader);

            System.out.write(responseHeader.getBytes());
            System.out.write(fileBytes);

            output.write(responseHeader.getBytes());
            output.write(fileBytes);
            output.flush();
        } catch (IOException e) {
            sendResponse(output, 500, "Internal Server Error", null);
        }
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
