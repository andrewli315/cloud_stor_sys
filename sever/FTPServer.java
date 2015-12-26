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
	String PSW_CARD;//���U���ͪ��d�P�۹������K�X
	
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
            
			//���ͤ@�ө�profile��fileManager
			ConcurrentSkipListSet<EnhancedProfileManager> profiles = new ConcurrentSkipListSet<EnhancedProfileManager>();
			
			
			card = new File(CARD);//���U���ͪ��d
			
			
			//�ھ�server���d�M�K�X���Jserver��profile
			profile = EZCardLoader.loadEnhancedProfile(card, PSW_CARD);
            
			profiles.add(profile);//�Nprofile��Jprofilemanager
			
			
			EnhancedAuthSocketServerAcceptor serverAcceptor = new EnhancedAuthSocketServerAcceptor(profiles);
			serverAcceptor.bind(PORT);//�j�wport1234
			server = serverAcceptor.accept();//����client�s�W�C
            
					
			server.waitUntilAuthenticated();//���ݬO�_�����{��
            System.out.println("[Server] sk: " + server.getSessionKey().getKeyValue());
			
			//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, card, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			
	}
	public void Recieve(String file_name)throws Exception{
		int index=0;
		int temp;
		byte[] cipher = new byte[32];
		byte[] dec_data = new byte[1];
		File f_wr = new File(file_name);
		fout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f_wr)));
		netIn = new DataInputStream(new BufferedInputStream(server.getInputStream()));
		  
		//SK�ƹ�W��256�줸���סA�ڭ̱N����b���O�@���[�K���_�PIV//
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
		byte[] sk = server.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
				
		while((temp = netIn.read()) != -1){
			cipher[index] = (byte)temp;
			System.out.printf("%d ",temp);
			if(index == 31){
				dec_data = CipherUtil.authDecrypt(k, iv, cipher);//�Nclient�ǨӪ��K��ѱK
				//System.out.printf("\n%d ",dec_data);
				fout.write(dec_data);
				index =0;
			}
			else 
				index++;
		}
		//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
		EZCardLoader.saveEnhancedProfile(profile, card, PSW_CARD);
		//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
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