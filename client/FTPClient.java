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
	String PSW_CARD;//���U���ͪ��d�P�۹������K�X
	
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
			
			 card = new File(CARD);//���U���ͪ��d
			
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
	public void Trans_file(String file_name)throws Exception{
		int temp;
		byte[] plain_txt = new byte[1];
		byte[] cipher;

		File f_r = new File(file_name);
		netOut = new DataOutputStream(client.getOutputStream());
		fin = new DataInputStream(new BufferedInputStream(new FileInputStream(f_r)));
		
		//SK�ƹ�W��256�줸���סA�ڭ̱N����b���O�@���[�K���_�PIV//
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
		byte[] sk = client.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
		
		while((temp = fin.read())!=-1){
			System.out.printf("%d ",temp);
			plain_txt[0] = (byte)temp;
			cipher = CipherUtil.authEncrypt(k, iv, plain_txt);//�N�ɮש���[�K
			netOut.write(cipher);
			Thread.sleep(100);
		}
		//!!!!!!!!!!!!!!!!!!!!!�C�������@��MAKD�@�w�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!!// 
		EZCardLoader.saveEnhancedProfile(profile, card, PSW_CARD);
		//!!!!!!!!!!!!!!!!!!!!!�C�������@��MAKD�@�w�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
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