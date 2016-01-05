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
	byte[] S_KEY;
	byte[] S_IV;
	byte[] F_KEY;
	byte[] F_IV;
	byte[] MK;
	byte[] M_IV;
	DataInputStream fin;
	DataOutputStream fout;
	DataInputStream netIn;
	DataOutputStream netOut;
	
	File card;
	
	Socket client;
	
	public FTPClient(String ip,int port,byte[] key,byte[] iv,byte[] mk,byte[] m_iv){
		IP = ip;
		PORT = port;
		S_KEY = key;
		S_IV = iv;
		M_IV = m_iv;
		MK = mk;
		F_KEY = generate_key_iv().getBytes();
		F_IV = generate_key_iv().getBytes();
		System.out.println(S_KEY.length);
	}
	public void Connect_Data_Channel(){
		try{
			client = new Socket(IP,PORT);
		}catch(IOException e){
			System.out.println("Connect error");
		}
		
	}
	public void Trans_file(String file_name)throws Exception{
		int temp;
		byte[] plain_txt = new byte[1];
		byte[] s_cipher;
		byte[] f_cipher;
		
		File f_r = new File(file_name);
		send_f_key();
		
		netOut = new DataOutputStream(client.getOutputStream());
		fin = new DataInputStream(new BufferedInputStream(new FileInputStream(f_r)));
		
		
		while((temp = fin.read())!=-1){
			
			plain_txt[0] = (byte)temp;
			f_cipher = CipherUtil.authEncrypt(F_KEY, F_IV, plain_txt);//將檔案明文加密
			s_cipher = CipherUtil.authEncrypt(S_KEY, S_IV, f_cipher);//將檔案明文加密
			for(int i =0;i<64;i++)
				System.out.printf("%d ",s_cipher[i]);
			netOut.write(s_cipher);
			Thread.sleep(100);
		}
		fin.close();
		netOut.flush();
		//netOut.close();
		
		
		//close();
	}
	public void Reciev_file(String file_name)throws Exception{
		
		//get_prev_key();
		int index=0;
		int temp;
		byte[] cipher =	new byte[64];
		byte[] tmp;
		byte[] dec_data;
		File f_wr = new File(file_name);
		fout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f_wr)));
		netIn = new DataInputStream(new BufferedInputStream(client.getInputStream()));
				
		while((temp = netIn.read()) != -1){
			cipher[index] = (byte)temp;
			if(index == 63){
				tmp = CipherUtil.authDecrypt(F_KEY, F_IV, cipher);//將client傳來的密文解密(由session key包的那層)
				dec_data = CipherUtil.authDecrypt(S_KEY, S_IV, tmp);//將client傳來的密文解密(由之前file key加密的)
				fout.write(dec_data);
				fout.flush();
				index =0;
			}
			else 
				index++;
		}
		fout.flush();
		fout.close();
		close();
		System.exit(0);
		
	}
	public void get_prev_key()throws Exception{
		byte[] cipher = new byte[80];
		byte[] iv,key,temp;
		netIn = new DataInputStream(new BufferedInputStream(client.getInputStream()));
		netIn.read(cipher);
		temp = CipherUtil.authDecrypt(S_KEY, S_IV, cipher);//將server傳來的密文key解密
		key = CipherUtil.authDecrypt(MK, M_IV, temp);
		F_KEY = key;
		netIn.read(cipher);
		temp = CipherUtil.authDecrypt(S_KEY, S_IV, cipher);//將server傳來的密文IV解密
		key = CipherUtil.authDecrypt(MK, M_IV, temp);
		F_IV = key;
		System.out.println(key);
		netIn.close();
	}
	public void send_f_key()throws Exception{
		netOut = new DataOutputStream(client.getOutputStream());
		byte[] m_cipher,s_cipher;
		m_cipher = CipherUtil.authEncrypt(MK, M_IV, F_KEY);//用Master key加密file key
		s_cipher = CipherUtil.authEncrypt(S_KEY, S_IV, m_cipher);
		netOut.write(s_cipher);
		System.out.println(m_cipher.length);
		System.out.println(s_cipher.length);
		Thread.sleep(1000);
		m_cipher = CipherUtil.authEncrypt(MK, M_IV, F_IV);//用Master key加密IV
		s_cipher = CipherUtil.authEncrypt(S_KEY, S_IV, m_cipher);
		netOut.write(s_cipher);
		netOut.flush();
		//netOut.close();
	}
	private String generate_key_iv(){
		char[] base = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		StringBuilder str = new StringBuilder();
		Random rand = new Random();
		for(int i=0;i<16;i++){
			char c = base[rand.nextInt(base.length)];
			str.append(c);			
		}
		String output = str.toString();
		System.out.println(output);
		return output;
		
		
	}
	public void close()throws Exception{
		client.close();
	}
	
}