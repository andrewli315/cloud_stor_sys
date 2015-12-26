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
import ezprivacy.secret.Signature;
import ezprivacy.service.signature.SignatureClient;



class Server{
	String[] Cmd = {"on","exit","upload","download","cd","delete"};
	String File_name = new String();
	int PORT = 3038;
	String CARD = "server.card";
	File SERV_CARD = new File(CARD);
	String PSW_CARD = "0000";
	
	String client_card;
	String client_psw;
	
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
			//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, SERV_CARD, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
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
				get_str = get_Msg(temp);
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
            
			//���ͤ@�ө�profile��fileManager
			ConcurrentSkipListSet<EnhancedProfileManager> profiles = new ConcurrentSkipListSet<EnhancedProfileManager>();
			
			
			//�ھ�server���d�M�K�X���Jserver��profile
			profile = EZCardLoader.loadEnhancedProfile(SERV_CARD, PSW_CARD);
            
			profiles.add(profile);//�Nprofile��Jprofilemanager
			
			
			serverAcceptor = new EnhancedAuthSocketServerAcceptor(profiles);
			serverAcceptor.bind(PORT);//�j�wport
			server = serverAcceptor.accept();//����client�s�W�C
			
			server.waitUntilAuthenticated();//���ݬO�_�����{��
            System.out.println("[Server] sk: " + server.getSessionKey().getKeyValue());
				
			//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, SERV_CARD, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
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
			System.out.println("prepare switching on data channel");
			int length;
			length = netIn.readInt();
			byte[] temp = new byte[length];
			netIn.read(temp);
			String File_name = get_Msg(temp);//recieve the file name
			reciev_File(File_name);
		}
		else if(Cmd[3].equals(cmd)){
			System.out.println("prepare switching on data channel");
			int length;
			length = netIn.readInt();
			byte[] temp = new byte[length];
			netIn.read(temp);
			String File_name = get_Msg(temp);//recieve the file name
			trans_File(File_name);
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
	public String get_Msg(byte[] encrypted_msg)throws Exception{
		//--------------------------server����client�ǰe���K��-------------------------------------//
		System.out.println(encrypted_msg);
		//--------------------------server����client�ǰe���K��-------------------------------------//
		
		//SK�ƹ�W��256�줸���סA�ڭ̱N����b���O�@���[�K���_�PIV//
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
		byte[] sk = server.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------// 
			
			
		//--------------------------�ѱK-----------------------------------------//
		String decrypt_msg = new String(CipherUtil.authDecrypt(k, iv, encrypted_msg));//�Nclient�ǨӪ��K��ѱK
		System.out.println("client say: " + decrypt_msg);
		return decrypt_msg;
		//--------------------------�ѱK-----------------------------------------//	
		
	}
	public void trans_Msg(String msg)throws Exception{
		
		//--------------------------server�ǰe�K��-------------------------------------//
		//SK�ƹ�W��256�줸���סA�ڭ̱N����b���O�@���[�K���_�PIV//
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
		byte[] sk = server.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------// 
		
		//-------------------------�[�K--------------------------------------//
		byte[] encrypted_msg = CipherUtil.authEncrypt(k, iv, msg.getBytes());
		//-------------------------�[�K--------------------------------------//
		
		
		//------------------------ ��X�K��-----------------------------------------//
		netOut = new DataOutputStream(server.getOutputStream());
		netOut.writeInt(encrypted_msg.length);
		netOut.write(encrypted_msg);
		netOut.flush();
		//------------------------ ��X�K��-----------------------------------------//

	}
	public int trans_File(String f)throws Exception{
		
		FTPServer ftpserv = new FTPServer(PORT+1000,CARD,PSW_CARD);
		ftpserv.Transmit(f);
		System.out.println("trans File");
		return 0;
	}
	public void reciev_File(String f)throws Exception{
		FTPServer ftpserv = new FTPServer(PORT+1000,CARD,PSW_CARD);
		ftpserv.Recieve(f);
		System.out.println("recive File");
		ftpserv.close();
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
	
	public void tran_Sig()throws Exception{
		
		EnhancedProfileManager sender = EZCardLoader.loadEnhancedProfile(SERV_CARD, PSW_CARD);//SERV_CARD �n��client��String
		EnhancedProfileManager receiver = EZCardLoader.loadEnhancedProfile(new File(client_card), client_psw);
		
		Signature sig = new SignatureClient.SignatureCreater()
				.initSignerID(sender.getPrimitiveProfile().getIdentifier())
				.initReceiverID(receiver.getPrimitiveProfile().getIdentifier())
				.initMessage("12345".getBytes())
				.initSignatureKey(sender.getPrimitiveProfile().getSignatureKey())
				.initTimestamp(System.nanoTime()).createSignature();

		boolean result = SignatureClient.verifyWithoutArbiter(sig, receiver.getPrimitiveProfile());
		
		System.out.println(result);//�o��n�ǥh��client����
	}
	
	
	public void close(){
		try{
			

			netIn.close();
			server.close();
			System.exit(0);
			return;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}