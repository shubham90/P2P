package p2p;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.io.*;

class peer_Server extends Thread {

    int pserver_port;

    @Override
    public void run() {
        try (ServerSocket s_sock = new ServerSocket(0)) {
            pserver_port = s_sock.getLocalPort();
            System.out.println("Client Server is Listening on port : " + pserver_port);
            while (true) {
                new ClientListener(s_sock.accept()).start();
            }
        } catch (IOException ex) {
            System.out.println("In catch of Client- peer_Server");
        }
    }
}

class ClientListener extends Thread {

    private Socket socket = null;
    BufferedReader in;
    DataOutputStream out;

    public ClientListener(Socket socket) throws IOException {
        this.socket = socket;
        System.out.println("I am in new thread.");
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.out = new DataOutputStream(this.socket.getOutputStream());
        System.out.println("I am in Listening Client Constructor");

    }

    public void run() {
        //String version="P2P-CI/1.0",ext=".txt";
        try {
            System.out.println("Listen Mode of Client");
            String line;
            String version = "P2P-CI/1.0";
            String END_PACK = "END\n";
            String responseMessage;
            String requestMessage = "";
            //String packet;
            while (!((line = this.in.readLine().trim()).equals(END_PACK.trim()))) {
                requestMessage += line + "\n";
            }
	//As soon as this gets a GET request, it makes an Uploader thread

            System.out.println("Message from Client::" + requestMessage);
            String words[] = requestMessage.split(" ");
            System.out.println("Operation :" + words[0]);
            String ReplytoClient;
            File f = new File(".//rfcs//rfc" + words[2] + ".txt");
            if (words[0].equals("GET") & words[1].equals("RFC")) {
                if (f.exists()) {
                    
                    ReplytoClient = "200 OK " + "\n" + "Date: " + "Thu, 21 Jan 2001 9:23:46 GMT " + "\n"
                            + "OS: Windows NT 7.6 \n"
                            + "Last Modified: Thu, 21 Jan 2001 9:23:46 GMT\n"
                            + "Content-Length: 12345\n" //TODO Insert RFC length here
                            + "Content-Type: text/text\n"
                            + "Data Data Data\n"
                            + "END\n";
						//this.out.writeBytes(packet);
                    //this.out.flush();
                } else {
                    ReplytoClient = "P2P-CI/1.0 : 404 Not Found" + "\r\n";
                }
                //}
            } else {
                ReplytoClient = "P2P-CI/1.0 : 400 Bad Request" + "\r\n";
            }
            System.out.println(ReplytoClient);
            if (ReplytoClient.contains("200 OK")) {
                this.out.writeBytes(ReplytoClient);
                this.out.flush();
                OutputStream os = socket.getOutputStream();
				byte[] buffer = new byte[1024];
                FileInputStream fin;
                BufferedOutputStream bout = new BufferedOutputStream(os, 1024);
				fin = new FileInputStream(f);
				int i = 0;
				while ((i = fin.read(buffer, 0, 1024)) != -1) {
					Thread.sleep(100);
					bout.write(buffer, 0, i);
					bout.flush();
				}   
                socket.shutdownOutput();
                bout.close();
                           
				fin.close();
            }
            else{
                this.out.writeBytes(ReplytoClient);
                this.out.flush();
            }

            socket.close();
        } catch (IOException ex) {
            System.out.println("In catch of Client- ClientListener");
        } catch (InterruptedException ex1) {
            System.out.println("In catch of Client- ClientListener");
        } 
    }
}

public class Client {

    public static void main(String[] args) {
		//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // System.out.println("Please enter the Clients Hostname");
        //Scanner s = new Scanner(System.in);
        //String hname = s.nextLine();
      //  String server_port = args[0];
        String END_PACK = "END\n";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = null;
        DataOutputStream client_out;
        DataInputStream client_in;
        int SERVER_PORT = 7734;
        //String ext=".txt",version="P2P-CI/1.0";
        String msg_to_server, p_message = "", final_msg;
        peer_Server client_server = new peer_Server();
        client_server.start();
        try {
            clientSocket = new Socket("127.0.0.1", SERVER_PORT);
            client_out = new DataOutputStream(clientSocket.getOutputStream());
            client_in = new DataInputStream(clientSocket.getInputStream());
            BufferedReader brS = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String packet;
            String response_from_server;
            String temp_response="Nothing came from Server";
            
            packet = "PING " + "P2P-CI/1.0" + "\n" + "host: " + java.net.InetAddress.getLocalHost().getHostName() + "\n" + "port: " + client_server.pserver_port + "\n" + "END\n";
            client_out.writeBytes(packet);
            client_out.flush();
            response_from_server = brS.readLine();
	    temp_response = response_from_server;
            while(!(response_from_server = brS.readLine()).equals(END_PACK.trim())){
                temp_response += response_from_server + "\n";
            }
        //    System.out.println("Reply from Server:" + temp_response);

            System.out.println("\nPlease choose more options from the menu.\n");
            do {
                System.out.println(" 1. Inform the server about all the stored RFCs(ADD) \n 2. Request peers having particular RFC (LOOKUP) \n 3. List the whole index of RFCs from the server (LIST) \n 4. Get RFC from a particular peer(GET) \n 5. Close connection to the server(CLOSE). \n ");
                Scanner s2 = new Scanner(System.in);
                int choice = s2.nextInt();

                switch (choice) {
                    case 1:
                        File folder = new File(".//rfcs");
                        File[] list = folder.listFiles();
                        String response;
                        
                        String filename;
                        for (File list1 : list) {
                            if (list1.isFile()) {
                                filename = (".//rfcs//" + list1.getName());
                                String rfcNumber = null;
                                String rfcTitle = null;
				System.out.println(filename);
                                try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {
					//System.out.println("reading file now");                                    
					String line = null;
                                    	while ((line = fileReader.readLine()) != null) {
                                        String trimmed_line = line.trim();
                                        if (trimmed_line.length() > 0) {
                                            rfcNumber = trimmed_line.substring(trimmed_line.length() - 3);
                                            break;
                                        }
                                    }

                                    while ((line = fileReader.readLine()) != null) {
                                        if (line.trim().length() > 0) {
                                            rfcTitle = line.trim();
                                            break;
                                        }
                                    }
                                }
                                String add_packet = "ADD RFC " + rfcNumber + " " + "P2P-CI/1.0" + "\n"
                                        + "Host: " + java.net.InetAddress.getLocalHost().getHostName() + "\n"
                                        + "Port: " + client_server.pserver_port + "\n"
                                        + "Title: " + rfcTitle + "\n"
                                        + "END\n";
                                System.out.println("TO SERVER: ");
                                System.out.println(add_packet);
                                // Sending the packet to the server.
                                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                                out.writeBytes(add_packet);
                                out.flush();

                                //Receiving server response
                                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                String line;
                                line = in.readLine();
                                response = line + "\n";
                                while (!(line = in.readLine()).equals(END_PACK.trim())) {
                                    response += line + "\n";
                                }
                                System.out.println("FROM SERVER :\n" + response + "\n");
                            }
                        }
                        
                        break;

                    case 2:
                        String fname = "needed_rfcs.txt";
                        try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                String rfcNumber = line.substring(0, 3);
                                String rfcTitle = line.substring(4);
                                String lookup_packet = "";
                                String lookup_END_PACK = "END\n";
                                String lookup_response = "No response from server yet";

                                lookup_packet = "LOOKUP RFC " + rfcNumber + " " + "P2P-CI/1.0" + "\n"
                                        + "Host: " + java.net.InetAddress.getLocalHost().getHostName() + "\n"
                                        + "Port: " + client_server.pserver_port + "\n"
                                        + "Title: " + rfcTitle + "\n"
                                        + "END\n";
                                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                out.writeBytes(lookup_packet);
                                out.flush();
                                System.out.println("TO SERVER:");
                                System.out.println(lookup_packet);
                                String responseLine;
                                responseLine = in.readLine();
                                lookup_response = responseLine + "\n";
                                while (!(responseLine = in.readLine()).equals(lookup_END_PACK.trim())) {
                                    lookup_response += responseLine + "\n";
                                }
                                System.out.println("FROM SERVER:\n" + lookup_response + "\n");
                            }
                        }
                        break;

                    case 3:
                        String list_request = "LIST ALL P2P-CI/1.0 \n"
                                + "Host: " + java.net.InetAddress.getLocalHost().getHostName() + "\n"
                                + "Port: " + client_server.pserver_port + "\n"
                                + "END\n";
                        String list_END_PACK = "END\n";
                        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        out.writeBytes(list_request);
                        out.flush();
                        System.out.println("TO SERVER:");
                        System.out.println(list_request);
                        String responseLine = in.readLine();
                        String list_response = responseLine;
                        while (!(responseLine = in.readLine()).equals(list_END_PACK.trim())) {
                            list_response += responseLine + "\n";
                        }
                        System.out.println("FROM SERVER:\n" + list_response + "\n");
                        break;

                    case 4:
                        System.out.println("Enter the Hostname: \n");
                        String peerHostname = br.readLine();
                        System.out.println("Enter the RFC Number:\n");
                        int neededRfcNum = Integer.parseInt(br.readLine());
                        System.out.println("Enter the port for Peer \n");
                        int peerPort = Integer.parseInt(br.readLine());
                        String getRfc_response = "";
                        String getRfc_request = "GET RFC " + neededRfcNum + " P2P-CI/1.0" + "\n"
                                + "Host: " + peerHostname + "\n"
                                + "OS: Windows NT 5.8" + "\n" + "END\n";
                        //System.out.println("getPeerRFC");

                        Socket p2pSocket = new Socket(peerHostname, peerPort);
                        System.out.println("getPeerRFC");
                        DataOutputStream getRfc_out = new DataOutputStream(p2pSocket.getOutputStream());
                        BufferedReader getRfc_in = new BufferedReader(new InputStreamReader(p2pSocket.getInputStream()));
                        getRfc_out.writeBytes(getRfc_request);
                        getRfc_out.flush();
             
                        String temp_response1 =null;
                        while(!(temp_response1 = getRfc_in.readLine()).equals(END_PACK.trim())){
                              getRfc_response += temp_response1 + "\n";
                        }
                        
                        System.out.println("Received: " + getRfc_response);
                        if (getRfc_response.contains("200 OK")) {
                            int file_size = 0;
                            boolean flag=true;
                            byte[] bytes = new byte[1024];
                            int bcount=1024;
                            FileOutputStream file_outstream = new FileOutputStream(".//rfcs//rfc" + neededRfcNum + ".txt");;                          
                            InputStream is = p2pSocket.getInputStream();
                            BufferedInputStream b_instream = new BufferedInputStream(is,1024);
                            while ((file_size = is.read(bytes, 0, 1024)) != -1) {
                                    bcount = bcount + 1024;
                                    file_outstream.write(bytes, 0, file_size);
                                    //System.out.println(new String(bytes));
                            }
							System.out.println(bcount);
                            b_instream.close();
                            file_outstream.close();
                            flag=true;
                            
                        }
                        else if(getRfc_response.contains("505")){
                        	System.out.println("Bad version error");
                        }
                        else if(getRfc_response.contains("404")){
                        	System.out.println("RFC not found");
                        }
                        else if(getRfc_response.contains("400")){
                        	System.out.println("Bad Request");
                        }
                        getRfc_in.close();
                        getRfc_out.close();
                        p2pSocket.close();
                        
                                   //Sending an add packet to the server
                        File filename1 = new File(".//rfcs//rfc"+neededRfcNum+".txt");
                        String rfcNumber = null;
                        String rfcTitle = null;
                     //   System.out.println(filename1);
                        try (BufferedReader fileReader = new BufferedReader(new FileReader(filename1))) {
                           // System.out.println("reading file now");                                    
                            String line = null;
                            while ((line = fileReader.readLine()) != null) {
                                String trimmed_line = line.trim();
                                if (trimmed_line.length() > 0) {
                                    rfcNumber = trimmed_line.substring(trimmed_line.length() - 3);
                                    break;
                                }
                            }

                            while ((line = fileReader.readLine()) != null) {
                                if (line.trim().length() > 0) {
                                    rfcTitle = line.trim();
                                    break;
                                }
                            }
                        }
                        String add_packet = "ADD RFC " + rfcNumber + " " + "P2P-CI/1.0" + "\n"
                                        + "Host: " + java.net.InetAddress.getLocalHost().getHostName() + "\n"
                                        + "Port: " + client_server.pserver_port + "\n"
                                        + "Title: " + rfcTitle + "\n"
                                        + "END\n";
                                System.out.println("TO SERVER: ");
                                System.out.println(add_packet);
                                // Sending the packet to the server.
                        out = new DataOutputStream(clientSocket.getOutputStream());
                        out.writeBytes(add_packet);
                        out.flush();

                                //Receiving server response
                        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String line;
                        line = in.readLine();
                        response = line + "\n";
                        while (!(line = in.readLine()).equals(END_PACK.trim())) {
                            response += line + "\n";
                        }
                        System.out.println("FROM SERVER :\n" + response + "\n");
                        
                        break;

                    case 5:
                        System.out.println("Connection closed");
                        DataOutputStream dataToServer = new DataOutputStream(clientSocket.getOutputStream());
                        String bye_packet = "BYE " + "P2P-CI/1.0" + "\n" + "Host: " + java.net.InetAddress.getLocalHost().getHostName() + "\n" + "Port: "
                                + client_server.pserver_port + "\n"
                                + "END\n";
                        dataToServer.writeBytes(bye_packet);
                        clientSocket.close();
                        System.exit(1);
                        return;

                    default:
                        System.out.println("Please enter a valid option: \n");
                        break;
                }
            } while (true);
        } catch (IOException e) {
            System.out.println("Server has not yet started running ... Start server and then run the peer \n");
        } finally {
            return;
        }
    }
}
