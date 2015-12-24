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
	String PSW_CARD;//���U���ͪ��d�P�۹������K�X
	
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
			
			File card = new File(CARD);//���U���ͪ��d
			
			//�ھ�client���d�M�K�X���Jclient��profile
			profile = EZCardLoader.loadEnhancedProfile(card, PSW_CARD);
			System.out.println("[client] profile: " + profile);
			
			//���ͤ@��client����
			client = new EnhancedAuthSocketClient(profile);
			client.connect(IP, PORT);//�s����server�P������port
            
			//------------- ACS��server,clent������MAKD���_���t�A����client�|�Pserver���ɤ@��sessionkey------------------------//
			client.doEnhancedKeyDistribution();
			System.out.println("[client] sk: " + client.getSessionKey().getKeyValue());
			//------------- ACS��server,clent������MAKD���_���t�A���ɷ|client�Pserver���ɤ@��sessionkey------------------------//
			
			//!!!!!!!!!!!!!!!!!!!!!�C�������@��MAKD�@�w�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, card, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!�C�������@��MAKD�@�w�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			 
			//--------------���谵�{��---------------------------------------------------//
			client.doRapidAuthentication();
			System.out.println("[client] auth: " + client.isAuthenticated());
			//--------------���谵�{��---------------------------------------------------//
		
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