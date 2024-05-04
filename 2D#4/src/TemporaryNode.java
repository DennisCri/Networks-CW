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
import java.util.Objects;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {

    Socket socket;
    BufferedReader reader;
    Writer writer;

    public boolean start(String startingNodeName, String startingNodeAddress) {

        System.out.println("Temporary node connecting to "+ startingNodeAddress);
        String[] address = startingNodeAddress.split(":");
        try {
            // Connect to the starting node
            socket = new Socket(address[0], Integer.parseInt(address[1]));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new OutputStreamWriter(socket.getOutputStream());

            // Send START message
            writer.write("START 1 " + startingNodeName + "\n");
            writer.flush();

            // Wait for START message from the server
            String response = reader.readLine();
            if (response != null && response.startsWith("START")) {
                System.out.println("Connection successful. The server said : " + response);
                return true; // Connection successful
            }

//            String response = reader.readLine();
//            System.out.println("The server said : " + response);

            // Close down the connection
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // Connection failed
    }

    public boolean store(String key, String value) {

        // Implement this!
        // Return true if the store worked
        // Return false if the store failed
        return true;
    }

    public String get(String key) {
        try {
            String[] keys = key.split(" ");
            if (keys.length == 0){
                return null;
            }

            // Send GET request
            writer.write("GET? " + keys.length + "\n");
            System.out.println("GET? "+ keys.length);
            for (String s : keys){
                writer.write(s + "\n");
                System.out.println(s);
            }
            writer.flush();

            // Read response
            String[] responses = reader.readLine().split(" ");
            if (Objects.equals(responses[0],"VALUE")) {
                // Value found, extract and return
                System.out.println(responses[0]+ " "+ responses[1]);
                String[] values = new String[Integer.parseInt(responses[1])];
                for (int i = 0; i < Integer.parseInt(responses[1]); i++) {
                    values[i] = reader.readLine();
                    System.out.println(values[i]);
                }
                return String.join(" ",values);

            } else if (Objects.equals(responses[0],"NOPE")) {
                // Value not found
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
