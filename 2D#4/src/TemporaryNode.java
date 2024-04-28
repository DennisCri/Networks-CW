// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    private Socket clientSocket;
    private BufferedReader reader;
    private Writer writer;

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            // Extract host and port from startingNodeAddress
            String[] parts = startingNodeAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            // Connect to the starting node
            clientSocket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Send START message
            writer.write("START 1 " + startingNodeName + "\n");
            writer.flush();

            // Read response
            String response = reader.readLine();

            // Check if the START message was successful
            if (response != null && response.equals("STARTED")) {
                System.out.println("TemporaryNode connected to the 2D#4 network.");
                return true;
            } else {
                System.out.println("Failed to connect to the 2D#4 network.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        // Implement this!
        // Return true if the 2D#4 network can be contacted
        // Return false if the 2D#4 network can't be contacted
        //return true;
    }

    public boolean store(String key, String value) {
        try {
            // Send PUT request
            writer.write("PUT? 1 " + key.length() + " " + value.length() + "\n" + key + "\n" + value + "\n");
            writer.flush();

            // Read response
            String response = reader.readLine();

            // Check if the store was successful
            if (response != null && response.equals("SUCCESS")) {
                System.out.println("Key-value pair stored successfully.");
                return true;
            } else {
                System.out.println("Failed to store key-value pair.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // Implement this!
        // Return true if the store worked
        // Return false if the store failed
        //return true;
    }

    public String get(String key) {
        try {
            // Send GET request
            writer.write("GET? 1\n" + key + "\n");
            writer.flush();

            // Read response
            String response = reader.readLine();

            // Check if a value was found for the key
            if (response != null && response.startsWith("VALUE")) {
                // Extract the value from the response
                int length = Integer.parseInt(response.split(" ")[1]);
                StringBuilder valueBuilder = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    valueBuilder.append(reader.readLine()).append("\n");
                }
                return valueBuilder.toString().trim();
            } else {
                System.out.println("Value not found for the given key.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // Implement this!
        // Return the string if the get worked
        // Return null if it didn't
        //return "Not implemented";
    }
}
