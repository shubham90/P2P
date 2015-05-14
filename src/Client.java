/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.


package p2p;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


/**
 *
 * @author Shubham



class ProgramDivider extends Thread{
        int port;
        
        @Override
        public void run() {
	try {              
            ServerSocket listen = new ServerSocket(0);
            this.port = listen.getLocalPort();
            System.out.println("Listening client Port" + port);
            while(true){  
                System.out.println("Inside While true");
                new ClientListener(listen.accept()).start();
                }         
                
        } catch (IOException ex) {
            System.out.println("In catch of Client- ProgramDivider");
        }                   				     
       }
        
    }




class ClientListener extends Thread{
        private Socket peerSocket=null;
        BufferedReader in;
        DataOutputStream out;
        
        
        public ClientListener(Socket socket) throws IOException{
            this.peerSocket = socket;
            System.out.println("I am in new thread.");
            this.in = new BufferedReader(new InputStreamReader(this.peerSocket.getInputStream()));
            this.out = new DataOutputStream(this.peerSocket.getOutputStream());
            System.out.println("I am in Listening Client Constructor");
        }
        
        @Override
        public void run() {
	try {              
            System.out.println("Listen Mode of Client");            
                listen();
        } catch (IOException ex) {
            System.out.println("In catch of Client- ClientListener");
        } finally{
            try {
                System.out.println("Closing the Socket");
                peerSocket.close();
            } catch (IOException ex) {
                System.out.println("In catch block of client run finally");
            }
        }                    				     
       }
        
       public void listen() throws IOException{
		String line;
                String END_PACK="END\n";
                String responseMessage; 
                String requestMessage = "";
                String packet;
                while (! ((line = this.in.readLine().trim()).equals(END_PACK.trim())) ){
                    requestMessage += line + "\n";
		}
		//As soon as this gets a GET request, it makes an Uploader thread
                
                System.out.println("Message from Client::" + requestMessage);
                String words[] = requestMessage.split(" ");
                System.out.println("Operation :" +words[0]);
                
                packet =  " 200 OK\n" + "Date: " + "Thu, 21 Jan 2001 9:23:46 GMT " + "\n"
					+ "OS: Windows NT 7.6 \n"
					+ "Last Modified: Thu, 21 Jan 2001 9:23:46 GMT"
					+ "Content-Length: 12345" //TODO Insert RFC length here
					+ "Content-Type: text/text"
					+ "Data Data Data"
					+ "END\n";
                this.out.writeBytes(packet);
		this.out.flush();
            
        }
    }


public class Client {
	
    int port;  
    String hostname;
    String version; 
    static final int SERVER_PORT = 7734;
    public static Socket clientSocket;
    
    public Client(String hostname, int port) throws IOException{

		this.port = port;
                System.out.println("Clients Port: " + this.port);
		this.hostname = hostname;
		version = "P2P-CI/1.0";	
               // ServerSocket listen = new ServerSocket(this.port);
               // while(true){
               // new Client.Listener(listen.accept()).start();
               // }
	}

/*	private class Downloader implements Runnable {

		InputStreamReader in;
		DataOutputStream out;
		Socket peerClientSocket;
		int peerServerPort;
		String peerServerName;

		public Downloader(String hostname, int port) throws Exception {
			
			this.peerServerPort = port; 
			this.peerServerName = hostname; 
			                 System.out.println(hostname + port);
			peerClientSocket = new Socket(this.peerServerName,this.peerServerPort);
			System.out.println("After Socket");
			this.in = new InputStreamReader(peerClientSocket.getInputStream());
			this.out = new DataOutputStream(peerClientSocket.getOutputStream());
			
		}
		
		@Override
		public void run() {
                    System.out.println("Downloader run 1");
			String request = "GET RFC 814 " + version + "\n"
					+ "Host: " + this.peerServerName + "\n"
					+ "OS: Windows NT 5.8" + "\n" + "END\n";
                        String END_PACK="END\n";
                        BufferedReader inp =  new BufferedReader(this.in);
                        
			try {
                            System.out.println("Downloader run 2");
				this.out.writeBytes(request);
                                this.out.flush();
                                System.out.println("TO Peer from peer:");
                                System.out.println(request);
                                String responseLine = inp.readLine();
                                String response = responseLine;
                                while(!(responseLine = inp.readLine()).equals(END_PACK.trim())){
                                    response += responseLine + "\n";
                                 }

                                System.out.println("FROM SERVER:\n" + response+ "\n");
                        } catch (IOException e) {
                                    e.printStackTrace(System.out);
                                }
                            }

//////	} 

            
        private void listRFC() throws Exception
	{
            String request = "LIST ALL P2P-CI/1.0 \n" 
				+ "Host: " + this.hostname + "\n"
				+ "Port: " + this.port  + "\n"
				+ "END\n";
            String END_PACK = "END\n";
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out.writeBytes(request);
            out.flush();    
            System.out.println("TO SERVER:");
            System.out.println(request);
            String responseLine = in.readLine();
            String response = responseLine;
            while(!(responseLine = in.readLine()).equals(END_PACK.trim())){
		response += responseLine + "\n";
            }

            System.out.println("FROM SERVER:\n" + response+ "\n");

	}

                
        private void lookupRFC(String line) throws Exception
	{
		// Step 1 :- assembling the lookup packet from various data elements.

		String rfcNumber = line.substring(0,3);
		String rfcTitle = line.substring(4);
		String packet ="";
                String END_PACK = "END\n";
		String response = "No response from server yet";

		packet = "LOOKUP RFC " + rfcNumber + " " + this.version + "\n"
				+ "Host: " + this.hostname + "\n"
				+ "Port: " + this.port  + "\n"
				+ "Title: " + rfcTitle + "\n"
				+ "END\n";


		//Step 2 :- Sending the packet to the Server.
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		out.writeBytes(packet);
		out.flush();
		System.out.println("TO SERVER:");
		System.out.println(packet);

		String responseLine;

		responseLine = in.readLine();
		response = responseLine+"\n";
                
		while(!(responseLine = in.readLine()).equals(END_PACK.trim())){
			response += responseLine + "\n";
                        
		}

		System.out.println("FROM SERVER:\n" + response+ "\n");
                
	}
                
                
        public void rfcLookupList() throws Exception
	{
            String fname = "needed_rfcs.txt";
            try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
                String line = null;
                while((line = reader.readLine())!=null)
                {
                  lookupRFC(line);
                }
            }
	}
        
        public void initiateConnection() throws Exception{
		DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String packet;
                String END_PACK = "END\n";
                packet = "PING " + version + "\n" + "host: " + hostname + "\n" + "port: " + port + "\n" + "END\n"; 
		out.writeBytes(packet);
                out.flush();
                String responseLine = in.readLine();
                String response = responseLine;
                while(!(responseLine = in.readLine()).equals(END_PACK.trim())){
		response += responseLine + "\n";
                }

            System.out.println("FROM SERVER:\n" + response+ "\n");
                
	}
        
        
        public void caller(int port) throws Exception{	
                ProgramDivider d = new ProgramDivider();
                Thread downloaderThread = new Thread(d);
                downloaderThread.start(); 
	}
        
        
        public void addRFC() throws Exception
	{
		File folder = new File(".//rfcs");
		File[] list = folder.listFiles();
                String response;
                String END_PACK = "END\n";
		String filename;
                for (File list1 : list) {
                    if (list1.isFile()) {
                        filename = (".\\\\rfcs\\\\" + list1.getName());
                        String packet = addPacket(filename);
                        System.out.println("TO SERVER: ");
                        System.out.println(packet);
                        
                        
                        // Sending the packet to the server.
                        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                        out.writeBytes(packet);
                        out.flush();
                        
                        //Receiving server response
                        BufferedReader in =  new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String line;
                        line = in.readLine();
                        response = line+"\n";     
                        while(!(line = in.readLine()).equals(END_PACK.trim())){
                            response += line + "\n";              
                        }
                        System.out.println("FROM SERVER :\n" + response+ "\n");
            }
        }
	}
        
        
 	public String addPacket(String filename) throws Exception {

            String rfcNumber = null;
            String rfcTitle = null;
            String packet;
            try (BufferedReader fileReader = new BufferedReader(new FileReader(filename))) {
                String line = null;      
                while ((line = fileReader.readLine()) != null) {
                    String trimmed_line = line.trim();
                    if (trimmed_line.length() > 0) {
                        rfcNumber = trimmed_line.substring(trimmed_line.length()-3);
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
		packet = "ADD RFC " + rfcNumber + " " + this.version + "\n"
				+ "Host: " + this.hostname + "\n"
				+ "Port: " + this.port  + "\n"
				+ "Title: " + rfcTitle + "\n"
				+ "END\n";

		return packet;
	}               
		

    public String getRFC(String hostname,String rfcNumber)
    {
        String packet = "GET RFC " + rfcNumber + " " + this.version + "\n"
				+ "Host: " + hostname + "\n"
				+ "OS : WINDOWS 8 \n"
				+ "END\n";		
        return packet;
    }

    public void closeConnectionToServer() throws Exception
	{
		DataOutputStream dataToServer = new DataOutputStream(clientSocket.getOutputStream());
		String packet = "BYE " + version + "\n" + "Host: "+hostname + "\n" + "Port: "
				+ port + "\n"
				+ "END\n";
		dataToServer.writeBytes(packet);
		clientSocket.close();
	}
    
    
    private void getPeerRFC(String hostname, int rfcnum, int portnumber) throws Exception
	{		
            String request = "GET RFC 814 " + version + "\n"
					+ "Host: " + hostname + "\n"
					+ "OS: Windows NT 5.8" + "\n" + "END\n";
            System.out.println("getPeerRFC");
                
            Socket p2pSocket = new Socket(hostname,portnumber);
                
            System.out.println("getPeerRFC");
            DataOutputStream out = new DataOutputStream(p2pSocket.getOutputStream());
            out.writeBytes(request);
            out.flush();
               
	}

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Please enter the Clients Hostname");
                Scanner s = new Scanner(System.in);
                String hname = s.nextLine();
               // System.out.println("Please enter the Clients Port Number");
               // Scanner s1 = new Scanner(System.in);
               // int portname = s1.nextInt();
                ProgramDivider th = new ProgramDivider();
                th.start();
     
                System.out.println(th.port);
		Client client=new Client(hname, th.port);
                //client.caller(th.port);
		try
		{
			clientSocket = new Socket("127.0.0.1",SERVER_PORT);
                        client.initiateConnection();
			System.out.println("Peer added to active Peer List... \n");
                        System.out.println("Please choose more options from the menu.\n");
			do
			{
				System.out.println(" 1. Inform the server about all the stored RFCs(ADD) \n 2. Request peers having particular RFC (LOOKUP) \n 3. List the whole index of RFCs from the server (LIST) \n 4. Get RFC from a particular peer(GET) \n 5. Close connection to the server(CLOSE). \n ");
				Scanner s2 = new Scanner(System.in);
                                int choice = s2.nextInt();

				switch(choice)
				{
				
				case 1:
					client.addRFC();
					System.out.println("Success...RFC added to RFC List for this Peer \n");
					break;

				case 2:	
					client.rfcLookupList();
					break;

				case 3:
					client.listRFC(); 
					break;

				case 4:
					System.out.println("Enter the Hostname: \n");
					String peerHostname = br.readLine();
					System.out.println("Enter the RFC Number:\n"); 
					int neededRfcNum = Integer.parseInt(br.readLine());
					System.out.println("Enter the port for Peer \n"); 
					int peerPort = Integer.parseInt(br.readLine());
					//String request = client.getRFC(peerHostname, neededRfcNum);
                                        client.getPeerRFC(peerHostname, neededRfcNum, peerPort);
                                        //p2pSocket = new Socket(peerHostname,peerPort);
					//Socket downSocket;
                                        

					break;

				case 5:
					System.out.println("Connection closed");
					client.closeConnectionToServer();
					System.exit(1);
					return;

				default:
					System.out.println("Please enter a valid option: \n");
					break;
				}
			}while(true);

		} catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println("Server has not yet started running ... Start server and then run the peer \n");
		} finally
		{
			return;
		}

	}

}


 */