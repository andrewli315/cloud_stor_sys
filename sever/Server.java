import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.net.*;

import java.util.HashSet;
import ezprivacy.toolkit.EZCardLoader;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.service.authsocket.AuthSocketServer;
import ezprivacy.service.acs.ACS;
import ezprivacy.service.authsocket.EnhancedAuthSocketServerAcceptor;
import ezprivacy.service.register.EnhancedProfileRegistrationClient;


class Server{
	String[] Cmd = {"on","exit","upload","download","cd","delete"};
	String File_name = new String();
	int PORT = 3038;
	File CARD = new File("server.card");
	String PSW_CARD = "0000";
	DataInputStream netIn;
	DataOutputStream netOut;
	BufferedInputStream buf;
	
	EnhancedProfileManager profile;
	AuthSocketServer server;
	EnhancedAuthSocketServerAcceptor serverAcceptor;
	public Server(){
		String msg = "";
		String get_str= new String();
		int length;
		boolean f= true;
		//open the port to listening the client;
		try{
			ctrl_channel();
		}catch(Exception e){
			e.printStackTrace();
		}
		//continueosly listening the client
		while(f){
		try{
			
		}catch(Exception e){
			f = false;
			e.printStackTrace();
		}
			try{
				netIn = new DataInputStream(server.getInputStream());
				length = netIn.readInt();
				System.out.println("length = "+length);
				byte[] temp = new byte[length];
				netIn.read(temp);
				get_str = get_CMD(temp);
				Command(get_str);
			}catch(Exception e){
				f=false;
				e.printStackTrace();
			}
		}
		close();
		return;
	}
	public void ctrl_channel()throws Exception{
		System.out.println("===== start EnhancedAuthSocketServerTest =====");
            
			//產生一個放profile的fileManager
			ConcurrentSkipListSet<EnhancedProfileManager> profiles = new ConcurrentSkipListSet<EnhancedProfileManager>();
			
			
			//根據server的卡和密碼載入server的profile
			profile = EZCardLoader.loadEnhancedProfile(CARD, PSW_CARD);
            
			profiles.add(profile);//將profile放入profilemanager
			
			
			serverAcceptor = new EnhancedAuthSocketServerAcceptor(profiles);
			serverAcceptor.bind(PORT);//綁定port
			server = serverAcceptor.accept();//等待client連上。
			
			server.waitUntilAuthenticated();//等待是否完成認證
            System.out.println("[Server] sk: " + server.getSessionKey().getKeyValue());
				
			//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, CARD, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
	}
	public void Command(String cmd)throws Exception{
		if(Cmd[0].equals(cmd)){
			ctrl_channel();
		}
		else if(Cmd[1].equals(cmd)){
			close();
			return;
		}
		else if(Cmd[2].equals(cmd)){
			trans_File(File_name);
		}
		else if(Cmd[3].equals(cmd)){
			reciev_File(File_name);
		}
		else if(Cmd[4].equals(cmd)){
			CD(File_name);
		}
		else if(Cmd[5].equals(cmd)){
			Delete(File_name);
		}
		else{
			System.out.println(cmd);
		}		
	}
	public String get_CMD(byte[] encrypted_msg)throws Exception{
		//--------------------------server接收client傳送的密文-------------------------------------//
		System.out.println(encrypted_msg);
		//--------------------------server接收client傳送的密文-------------------------------------//
		
		//SK事實上為256位元長度，我們將其拆對半分別作為加密金鑰與IV//
		//--------------加解密前先把key和iv拿出---------------------------------------//
		byte[] sk = server.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------加解密前先把key和iv拿出---------------------------------------// 
			
			
		//--------------------------解密-----------------------------------------//
		String decrypt_msg = new String(CipherUtil.authDecrypt(k, iv, encrypted_msg));//將client傳來的密文解密
		System.out.println("client say: " + decrypt_msg);
		return decrypt_msg;
		//--------------------------解密-----------------------------------------//	
		
	}
	public void trans_Msg(String msg)throws Exception{
		
		//--------------------------server傳送密文-------------------------------------//
		//SK事實上為256位元長度，我們將其拆對半分別作為加密金鑰與IV//
		//--------------加解密前先把key和iv拿出---------------------------------------//
		byte[] sk = server.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------加解密前先把key和iv拿出---------------------------------------// 
		
		//-------------------------加密--------------------------------------//
		byte[] encrypted_msg = CipherUtil.authEncrypt(k, iv, msg.getBytes());
		//-------------------------加密--------------------------------------//
		
		
		//------------------------ 輸出密文-----------------------------------------//
		netOut = new DataOutputStream(server.getOutputStream());
		netOut.writeInt(encrypted_msg.length);
		netOut.write(encrypted_msg);
		netOut.flush();
		//------------------------ 輸出密文-----------------------------------------//
			
		
		
	}
	public int trans_File(String f){
		System.out.println("trans File");
		return 0;
	}
	public int reciev_File(String f){
		System.out.println("recive File");
		return 0;
	}
	public boolean CD(String f){
		System.out.println("CD dir");
		return true;
	}
	public boolean Delete(String f){
		System.out.println("Delete file");
		return true;
	}
	public boolean isFile_Exist(String File){
		return true;
	}
	public boolean isDirectory(String dir){
		return true;
	}
	public void close(){
		try{
			
			//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, CARD, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			netIn.close();
			server.close();
			System.exit(0);
			return;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}