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

class FTPServer{
	int PORT;
	String CARD;
	String PSW_CARD;//註冊產生的卡與相對應的密碼
	
	DataInputStream fin;
	DataOutputStream fout;
	DataInputStream netIn;
	DataOutputStream netOut;
	
	File card;
	
	EnhancedProfileManager profile;
	AuthSocketServer server;
	
	public FTPServer(int port,String card,String psw){
		PORT = port;
		CARD = card;
		PSW_CARD = psw;
		try{
			Data_Channel();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public void Data_Channel()throws Exception{
		System.out.println("===== start EnhancedAuthSocketServerTest =====");
            
			//產生一個放profile的fileManager
			ConcurrentSkipListSet<EnhancedProfileManager> profiles = new ConcurrentSkipListSet<EnhancedProfileManager>();
			
			
			card = new File(CARD);//註冊產生的卡
			
			
			//根據server的卡和密碼載入server的profile
			profile = EZCardLoader.loadEnhancedProfile(card, PSW_CARD);
            
			profiles.add(profile);//將profile放入profilemanager
			
			
			EnhancedAuthSocketServerAcceptor serverAcceptor = new EnhancedAuthSocketServerAcceptor(profiles);
			serverAcceptor.bind(PORT);//綁定port1234
			server = serverAcceptor.accept();//等待client連上。
            
					
			server.waitUntilAuthenticated();//等待是否完成認證
            System.out.println("[Server] sk: " + server.getSessionKey().getKeyValue());
			
			//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, card, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			
	}
	public void Recieve(String file_name)throws Exception{
		int index=0;
		int temp;
		byte[] cipher = new byte[32];
		byte[] dec_data = new byte[1];
		File f_wr = new File(file_name);
		fout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f_wr)));
		netIn = new DataInputStream(new BufferedInputStream(server.getInputStream()));
		  
		//SK事實上為256位元長度，我們將其拆對半分別作為加密金鑰與IV//
		//--------------加解密前先把key和iv拿出---------------------------------------//
		byte[] sk = server.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------加解密前先把key和iv拿出---------------------------------------//
				
		while((temp = netIn.read()) != -1){
			cipher[index] = (byte)temp;
			System.out.printf("%d ",temp);
			if(index == 31){
				dec_data = CipherUtil.authDecrypt(k, iv, cipher);//將client傳來的密文解密
				//System.out.printf("\n%d ",dec_data);
				fout.write(dec_data);
				index =0;
			}
			else 
				index++;
		}
		//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
		EZCardLoader.saveEnhancedProfile(profile, card, PSW_CARD);
		//!!!!!!!!!!!!!!!!!!!!!每次結束程式前必要要儲存profile!!!!!!!!!!!!!!!!!!!!!!!!!// 
		fout.flush();
		fout.close();
		server.close();
		System.exit(0);
	}
	public void Transmit(String file_name)throws Exception{
		int temp;
		byte[] plain_txt = new byte[1];
		byte[] cipher;

		File f_r = new File(file_name);
		netOut = new DataOutputStream(server.getOutputStream());
		fin = new DataInputStream(new BufferedInputStream(new FileInputStream(f_r)));
	}
	
}