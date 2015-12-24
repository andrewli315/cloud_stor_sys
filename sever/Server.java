import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import ezprivacy.service.authsocket.EnhancedAuthSocketServerAcceptor;
import ezprivacy.service.register.EnhancedProfileRegistrationClient;


class Server{
	String[] Cmd = {"on","off","upload","download","cd","delete"};
	String File_name = new String();
	String msg;
	int PORT = 3038;
	File CARD = new File("server.card");
	String PSW_CARD = "F74032162";
	DataInputStream netIn;
	DataOutputStream netOut;
	BufferedReader reader;
	
	EnhancedProfileManager profile;
	AuthSocketServer server;
	EnhancedAuthSocketServerAcceptor serverAcceptor;
	public Server(){
		//netIn = new DataInputStream(server.getInputStream());
		Scanner in = new Scanner(System.in);
		String get_str= new String();
		try{
			ctrl_channel();
		}catch(Exception e){
			e.printStackTrace();
		}
			try{
				in.nextLine();
				reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
				msg = reader.readLine();
				get_str = get_CMD(msg);
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println(get_str);
			if(get_str.equals("exit"))
				close();
				
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
	}
	public void Command(String cmd)throws Exception{
		if(cmd.equals(Cmd[0]))
			ctrl_channel();
		else if(cmd.equals(Cmd[1]))
				close();
		else if(cmd.equals(Cmd[2]))
			trans_File(File_name);
		else if(cmd.equals(Cmd[3]))
			reciev_File(File_name);
		else if(cmd.equals(Cmd[4]))
			CD(File_name);
		else if(cmd.equals(Cmd[5]))
			Delete(File_name);
		else{
			System.out.println("Error");
			return;
		}		
	}
	public String get_CMD(String msg)throws Exception{
		//--------------------------server接收client傳送的密文-------------------------------------//
		
		byte[] encrypted_msg = msg.getBytes();
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
		return 0;
	}
	public int reciev_File(String f){
		
		return 0;
	}
	public boolean CD(String f){
		return true;
	}
	public boolean Delete(String f){
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
			netIn.close();
			netOut.close();
			server.close();
			
			//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, CARD, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}