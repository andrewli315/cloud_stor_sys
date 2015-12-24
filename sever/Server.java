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
            
			//���ͤ@�ө�profile��fileManager
			ConcurrentSkipListSet<EnhancedProfileManager> profiles = new ConcurrentSkipListSet<EnhancedProfileManager>();
			
			
			//�ھ�server���d�M�K�X���Jserver��profile
			profile = EZCardLoader.loadEnhancedProfile(CARD, PSW_CARD);
            
			profiles.add(profile);//�Nprofile��Jprofilemanager
			
			
			serverAcceptor = new EnhancedAuthSocketServerAcceptor(profiles);
			serverAcceptor.bind(PORT);//�j�wport
			server = serverAcceptor.accept();//����client�s�W�C
			
			server.waitUntilAuthenticated();//���ݬO�_�����{��
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
		//--------------------------server����client�ǰe���K��-------------------------------------//
		
		byte[] encrypted_msg = msg.getBytes();
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
			
			//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, CARD, PSW_CARD);
			//!!!!!!!!!!!!!!!!!!!!!�C�������{���e���n�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}