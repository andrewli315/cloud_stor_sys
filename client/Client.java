import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ConcurrentSkipListSet;
import java.net.*;

import java.util.HashSet;
import ezprivacy.toolkit.EZCardLoader;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.service.authsocket.AuthSocketServer;
import ezprivacy.service.acs.ACS;
import ezprivacy.service.authsocket.EnhancedAuthSocketClient;
import ezprivacy.service.register.EnhancedProfileRegistrationClient;
import ezprivacy.secret.Signature;
import ezprivacy.service.signature.SignatureClient;


class Client{
	DataInputStream netIn;
	DataOutputStream netOut;
	BufferedReader input;
	BufferedOutputStream out;
	
	String IP;
	int PORT;
	String CARD = "client.card";
	File CL_CARD = new File(CARD);//註冊產生的卡
	String PSW_CARD = "1234";//註冊產生的卡與相對應的密碼
	
	String[] Cmd = {"on","off","upload","download","cd","delete"};
	
	EnhancedProfileManager profile;
	EnhancedAuthSocketClient client;
	
	
	
	//constructor of Client class, in order to create needy variable and GUI　surface
	public Client()throws Exception{
		boolean f=true;
		
		input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("input the ip and port: ...");
		IP=input.readLine();
		PORT =3038; 
		connect();
		netIn = new DataInputStream(new BufferedInputStream(client.getInputStream()));//listen to the message from server;
		while(f){
			String msg = input.readLine();
			trans_MSG(msg);
			Command(msg);
			if("exit".equals(msg)){
				trans_MSG(msg);
				f= false;
			}
		}
		close();
		return;
		
	}
	//connect to the server
	public void connect()throws Exception{
		System.out.println("===== start EnhancedAuthSocketClientTest =====");
			
			//根據client的卡和密碼載入client的profile
			profile = EZCardLoader.loadEnhancedProfile(CL_CARD, PSW_CARD);
			System.out.println("[client] profile: " + profile);
			
			//產生一個client物件
			client = new EnhancedAuthSocketClient(profile);
			client.connect(IP, PORT);//連接到server與對應的port
            
			//------------- ACS為server,clent之間做MAKD金鑰分配，此時client會與server分享一把sessionkey------------------------//
			client.doEnhancedKeyDistribution();
			System.out.println("[client] sk: " + client.getSessionKey().getKeyValue());
			//------------- ACS為server,clent之間做MAKD金鑰分配，此時會client與server分享一把sessionkey------------------------//
			
			//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, CL_CARD, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			 
			//--------------雙方做認證---------------------------------------------------//
			client.doRapidAuthentication();
			System.out.println("[client] auth: " + client.isAuthenticated());
			//--------------雙方做認證---------------------------------------------------//	
		
	}
	//transmit the message, include the CMD, File name,and something else;
	public void trans_MSG(String msg)throws Exception{
		
		netOut = new DataOutputStream(client.getOutputStream());
		//SK事實上為256位元長度，我們將其拆對半分別作為加密金鑰與IV//
		//--------------加解密前先把key和iv拿出---------------------------------------//
		byte[] sk = client.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------加解密前先把key和iv拿出---------------------------------------//
			
		//--------------加密欲傳送的內容--------------------------------------------------//
		byte[] encrypted_msg = CipherUtil.authEncrypt(k, iv, msg.getBytes());
		//--------------加密欲傳送的內容--------------------------------------------------//
            
		//--------------輸出密文-------------------------------------------------------//
		netOut.writeInt(encrypted_msg.length);
		netOut.write(encrypted_msg);
		netOut.flush();
		//--------------輸出密文-------------------------------------------------------//
	}
	public String get_MSG(byte[] msg)throws Exception{
		//SK事實上為256位元長度，我們將其拆對半分別作為加密金鑰與IV//
		//--------------加解密前先把key和iv拿出---------------------------------------//
		byte[] sk = client.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------加解密前先把key和iv拿出---------------------------------------// 
			
			
		//--------------------------解密-----------------------------------------//
		String decrypt_msg = new String(CipherUtil.authDecrypt(k, iv, msg));//將server傳來的密文解密
		System.out.println("client say: " + decrypt_msg);
		return decrypt_msg;
		//--------------------------解密-----------------------------------------//	
		
		
	}
	public void Command(String cmd)throws Exception{
		int length;
		String msg = "";
		
		
		if(Cmd[0].equals(cmd)){
			connect();
		}
		else if(Cmd[1].equals(cmd)){
			close();
			System.exit(0);
			return;
		}
		else if(Cmd[2].equals(cmd)){
			String File_name = input.readLine();
			trans_MSG(File_name);
			//length = netIn.readInt();
			//byte[] temp = new byte[length];
			//netIn.read(temp);
			Thread.sleep(100);
			System.out.println("trans???");
			//if("ready".equals(get_MSG(temp)))//recieve the ready command
			trans_File(File_name);
		}
		else if(Cmd[3].equals(cmd)){
			recieve_File("test");
		}
		else if(Cmd[4].equals(cmd)){
			netOut.write("cd".getBytes());
		}
		else if(Cmd[5].equals(cmd)){
			netOut.write("delete".getBytes());
		}
		else{
			System.out.println(cmd);
		}		
		
	}
	
	//create a new FTPClient class(the class define in another java file, mainly dealing with the data transmission) to create a new data channel
	public void trans_File(String f)throws Exception{
		System.out.println("transmit File");
		
		FTPClient ftpclient = new FTPClient(IP,PORT+1,CARD,PSW_CARD);
		ftpclient.Connect_Data_Channel();

		ftpclient.Trans_file(f);
		
		System.out.println("transmit File" + f);
					//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, CL_CARD, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
		return;
		
	}
	//the purpose is also to create a new data channel to transmit the file
	public void recieve_File(String f){
		System.out.println("recieve File");
	}
	//recieve the Signature from the Server and save as a file;
	public void recieve_Sig(){
		
	}
	//transmit a command to delete some file or directories
	public void delete(){
		
	}
	//close the client socket
	public void close()throws Exception{
		
			//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, CL_CARD, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
		netOut.close();
		client.close();
	}
}