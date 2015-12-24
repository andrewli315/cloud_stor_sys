import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
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
import ezprivacy.service.authsocket.EnhancedAuthSocketClient;
import ezprivacy.service.register.EnhancedProfileRegistrationClient;


class Client{
	DataInputStream netIn;
	DataOutputStream netOut;
	BufferedReader input;
	BufferedOutputStream out;
	String IP;
	int PORT;
	File card = new File("client.card");//���U���ͪ��d
	String password = "1234";//���U���ͪ��d�P�۹������K�X
	EnhancedProfileManager profile;
	EnhancedAuthSocketClient client;
	
	
	
	//constructor of Client class, in order to create needy variable and GUI�@surface
	public Client()throws Exception{
		input = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("input the ip and port: ...");
		IP=input.readLine();
		PORT =3038; 
		connect();
		String msg = input.readLine();
		trans_MSG(msg);
		close();
		return;
		
	}
	//connect to the server
	public void connect()throws Exception{
		System.out.println("===== start EnhancedAuthSocketClientTest =====");
			
			//�ھ�client���d�M�K�X���Jclient��profile
			profile = EZCardLoader.loadEnhancedProfile(card, password);
			System.out.println("[client] profile: " + profile);
			
			//���ͤ@��client����
			client = new EnhancedAuthSocketClient(profile);
			client.connect(IP, PORT);//�s����server�P������port
            
			//------------- ACS��server,clent������MAKD���_���t�A����client�|�Pserver���ɤ@��sessionkey------------------------//
			client.doEnhancedKeyDistribution();
			System.out.println("[client] sk: " + client.getSessionKey().getKeyValue());
			//------------- ACS��server,clent������MAKD���_���t�A���ɷ|client�Pserver���ɤ@��sessionkey------------------------//
			
			//!!!!!!!!!!!!!!!!!!!!!�C�������@��MAKD�@�w�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!!// 
			EZCardLoader.saveEnhancedProfile(profile, card, password);
			//!!!!!!!!!!!!!!!!!!!!!�C�������@��MAKD�@�w�n�x�sprofile!!!!!!!!!!!!!!!!!!!!!!!!!// 
			 
			//--------------���谵�{��---------------------------------------------------//
			client.doRapidAuthentication();
			System.out.println("[client] auth: " + client.isAuthenticated());
			//--------------���谵�{��---------------------------------------------------//	
		
	}
	//transmit the message, include the CMD, File name,and something else;
	public void trans_MSG(String msg)throws Exception{
		
		netOut = new DataOutputStream(client.getOutputStream());
		//SK�ƹ�W��256�줸���סA�ڭ̱N����b���O�@���[�K���_�PIV//
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
		byte[] sk = client.getSessionKey().getKeyValue();
		byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
		byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
		//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
			
		//--------------�[�K���ǰe�����e--------------------------------------------------//
		byte[] encrypted_msg = CipherUtil.authEncrypt(k, iv, msg.getBytes());
		//--------------�[�K���ǰe�����e--------------------------------------------------//
            
		//--------------��X�K��-------------------------------------------------------//
		netOut.writeInt(encrypted_msg.length);
		netOut.write(encrypted_msg);
		netOut.flush();
		
		//--------------��X�K��-------------------------------------------------------//
		
		
		
	}
	//create a new FTPClient class(the class define in another java file, mainly dealing with the data transmission) to create a new data channel
	public void trans_File(String f){
		
	}
	//the purpose is also to create a new data channel to transmit the file
	public void recieve_File(String f){
		
	}
	//recieve the Signature from the Server and save as a file;
	public void recieve_Sign(){
		
	}
	//transmit a command to delete some file or directories
	public void delete(){
		
	}
	//close the client socket
	public void close()throws Exception{
		netOut.close();
		client.close();
	}
}