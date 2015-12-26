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
import ezprivacy.service.authsocket.EnhancedAuthSocketClient;
import ezprivacy.service.register.EnhancedProfileRegistrationClient;

class FTPClient{
	int PORT;
	String IP;
	String CARD;
	String PSW_CARD;//註冊產生的卡與相對應的密碼
	
	DataInputStream fin;
	DataOutputStream fout;
	DataInputStream netIn;
	DataOutputStream netOut;
	
	File card;
	
	EnhancedProfileManager profile;
	EnhancedAuthSocketClient client;
	
	public FTPClient(String ip,int port,String card,String psw){
		IP = ip;
		PORT = port;
		CARD = card;
		PSW_CARD = psw;
	}
	public void Connect_Data_Channel()throws Exception{
		System.out.println("===== start EnhancedAuthSocketClientTest =====");
			
			 card = new File(CARD);//註冊產生的卡
			
			//根據client的卡和密碼載入client的profile
			profile = EZCardLoader.loadEnhancedProfile(card, PSW_CARD);
			System.out.println("[client] profile: " + profile);
			
			//產生一個client物件
			client = new EnhancedAuthSocketClient(profile);
			client.connect(IP, PORT);//連接到server與對應的port
            
			//------------- ACS為server,clent之間做MAKD金鑰分配，此時client會與server分享一把sessionkey------------------------//
			client.doEnhancedKeyDistribution();
			System.out.println("[client] sk: " + client.getSessionKey().getKeyValue());
			//------------- ACS為server,clent之間做MAKD金鑰分配，此時會client與server分享一把sessionkey------------------------//
			
			//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, card, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			 
			//--------------雙方做認證---------------------------------------------------//
			client.doRapidAuthentication();
			System.out.println("[client] auth: " + client.isAuthenticated());
			//--------------雙方做認證---------------------------------------------------//
		
	}
	public void Trans_file(String file_name)throws Exception{
		int temp;
		byte[] plain_txt = new byte[1];
		byte[] cipher;

		File f_r = new File(file_name);
		netOut = new DataOutputStream(client.getOutputStream());
		fin = new DataInputStream(new BufferedInputStream(new FileInputStream(f_r)));
		
		//SK事實上為256位元長度，我們將其拆對半分別作為加密金鑰與IV//
		//--------------加解密前先把key和iv拿出---------------------------------------//
		byte[] sk = client.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------加解密前先把key和iv拿出---------------------------------------//
		
		while((temp = fin.read())!=-1){
			System.out.printf("%d ",temp);
			plain_txt[0] = (byte)temp;
			cipher = CipherUtil.authEncrypt(k, iv, plain_txt);//將檔案明文加密
			netOut.write(cipher);
			Thread.sleep(100);
		}
		//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!!// 
		EZCardLoader.saveEnhancedProfile(profile, card, PSW_CARD);
		//!!!!!!!!!!!!!!!!!!!!!每次做完一次MAKD一定要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
		fin.close();
		netOut.flush();
		netOut.close();
		client.close();
		System.exit(0);
				
	}
	public void Reciev_file(String file_name){
		
		
	}
	public void close()throws Exception{
		
	}
	
}