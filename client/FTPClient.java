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
import ezprivacy.secret.Signature;
import ezprivacy.service.signature.SignatureClient;

class FTPClient{
	int PORT;
	String IP;
	String CARD;
	String PSW_CARD;//註冊產生的卡與相對應的密碼
	
	DataInputStream fin;
	DataOutputStream fout;
	DataInputStream netIn;
	DataOutputStream netOut;
	
	EnhancedProfileManager profile;
	EnhancedAuthSocketClient client;
	
	public FTPClient(String ip,int port,String card,String psw){
		IP = ip;
		PORT = port;
		CARD = card;
		PSW_CARD = psw;
		try{
			Connect_Data_Channel();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void Connect_Data_Channel()throws Exception{
		System.out.println("===== start EnhancedAuthSocketClientTest =====");
			
			File card = new File(CARD);//註冊產生的卡
			
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
	public void tran_file(String file_name)throws Exception{
		int temp;
		byte[] plain_txt = new byte[1];
		byte[] cipher;

		File f_r = new File(file_name);
		netOut = new DataOutputStream(client.getOutputStream());
		fin = new DataInputStream(new BufferedInputStream(new FileInputStream(f_r)));
				
	}
	public void Reciev_file(String file_name){
		
		
		
		
		
		
		
		
	}
	
	
}