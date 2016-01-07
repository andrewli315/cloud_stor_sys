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
	byte[] IV;
	byte[] SK;
	
	DataInputStream fin;
	DataOutputStream fout;
	DataInputStream netIn;
	DataOutputStream netOut;
	
	File card;
	
	ServerSocket server;
	Socket serv;
	
	public FTPServer(int port,byte[] key,byte[] iv){
		PORT = port;
		SK = key;
		IV = iv;
		try{
			Data_Channel();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public void Data_Channel()throws Exception{
		server = new ServerSocket(PORT);
		serv = server.accept();
		System.out.println("Server is ready...");
	}
	public void send_file_list()throws Exception{
		
		byte[] cipher;
		
		File search = new File("account");
		File[] file_read = search.listFiles();
		String[] f = search.list();	
		
		netOut = new DataOutputStream(serv.getOutputStream());
		netOut.write(f.length);
		
		for(int i=0;i<f.length;i++){
			System.out.println(f[i]);
			cipher = CipherUtil.authEncrypt(SK,IV,f[i].getBytes());
			netOut.write(cipher);
		}
		netOut.close();
		
	}
	public void Recieve(String file_name)throws Exception{
		int index=0;
		int temp;
		byte[] cipher = new byte[64];
		byte[] dec_data;
		File f_wr = new File(file_name);
		fout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f_wr)));
		netIn = new DataInputStream(new BufferedInputStream(serv.getInputStream()));
		
		
		while((temp = netIn.read()) != -1){
			cipher[index] = (byte)temp;
			System.out.printf("%d ",temp);
			if(index == 63){
				dec_data = CipherUtil.authDecrypt(SK, IV, cipher);//將client傳來的密文解密(F* 用session key 包住的那層)
				fout.write(dec_data);
				index =0;
				fout.flush();
			}
			else 
				index++;
		}
		fout.flush();
		fout.close();
		Thread.sleep(1000);
		
	}
	public void Transmit(String file_name)throws Exception{
		int temp;
		byte[] plain_txt = new byte[32];
		byte[] cipher;
		int index =0;
		
		System.out.println("test");
		
		File f_r = new File(file_name);
		
		netOut = new DataOutputStream(serv.getOutputStream());
		fin = new DataInputStream(new BufferedInputStream(new FileInputStream(f_r)));

		
		while((temp = fin.read())!=-1){
			plain_txt[index] = (byte)temp;
			if(index==31){
				cipher = CipherUtil.authEncrypt(SK, IV, plain_txt);//將檔案明文加密
				/*for(int i =0;i<64;i++)
					System.out.printf("%d ",cipher[i]);*/
				System.out.println(cipher.length);
				netOut.write(cipher);
				index = 0;
				Thread.sleep(100);
			}
			else{
				index++;
			}
			Thread.sleep(5);
		}
		fin.close();
		netOut.flush();
		netOut.close();
		Thread.sleep(100);
		
	}
	public void send_prev_key(String file_name){
		byte[] key = new byte[48];
		byte[] cipher;
		int temp;
		int index =0;
		File f = new File("key"+File.separator+file_name+".key");
		
		try{
			fin = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			netOut = new DataOutputStream(serv.getOutputStream());
			
			while((temp = fin.read())!=-1){
			//System.out.printf("%d ",(byte)temp);
			key[index] = (byte)temp;
			if(index ==47){
				cipher = CipherUtil.authEncrypt(SK, IV, key);//將密文金鑰用session key加密
				System.out.println(new String(cipher));
				//System.out.printf("%d\n",cipher.length);
				netOut.write(cipher);
				index =0;
				netOut.flush();
			}
			else{
				index++;
			}
		}
			netOut.flush();
			System.out.println("send key..");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void get_key_iv(String file_name){
		byte[] cipher = new byte[80];
		byte[] key;
		File f = new File("key"+File.separator+file_name+".key");
			
		try{
			fout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			netIn = new DataInputStream(serv.getInputStream());
			
			
			netIn.read(cipher);
			key = CipherUtil.authDecrypt(SK,IV,cipher);
			fout.write(key);
			fout.flush();
			System.out.println(key.length);
			netIn.read(cipher);
			key = CipherUtil.authDecrypt(SK,IV,cipher);
			fout.write(key);
			fout.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void close(){
		try{
			server.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}