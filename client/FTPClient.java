class FTPClient{
	String IP;
	int Port;
	DataInputStream netIn;
	DataOutputStream netOut;
	
	
	public Client(){
		
	}
	public Auth(String card,String password)throws Exception{
		
		
	}
	public void tran_message(String msg){
		
			//SK�ƹ�W��256�줸���סA�ڭ̱N����b���O�@���[�K���_�PIV//
			//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
			byte[] sk = client.getSessionKey().getKeyValue();
			byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
			byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
			//--------------�[�ѱK�e����key�Miv���X---------------------------------------//
			
			//--------------�[�K���ǰe�����e--------------------------------------------------//
			String msg = "hello, server.";
			byte[] encrypted_msg = CipherUtil.authEncrypt(k, iv, msg.getBytes());
			//--------------�[�K���ǰe�����e--------------------------------------------------//
            
			//--------------��X�K��-------------------------------------------------------//
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeInt(encrypted_msg.length);
			out.write(encrypted_msg);
			out.flush();
			//--------------��X�K��-------------------------------------------------------//
			
			//--------------����server�ǨӪ��K��---------------------------------------------//
			DataInputStream in = new DataInputStream(client.getInputStream());
			int msg_length = in.readInt();
			byte[] encrypted_rcvd_msg = new byte [ msg_length ];
			in.readFully(encrypted_rcvd_msg);
			//--------------����server�ǨӪ��K��---------------------------------------------//
            
			//--------------�ѱKserver�ǨӪ��K��---------------------------------------------//
			String rcvd_msg = new String(CipherUtil.authDecrypt(k, iv, encrypted_rcvd_msg));
			System.out.println("server say: " + rcvd_msg);
			//--------------�ѱKserver�ǨӪ��K��---------------------------------------------//

			System.out.println("===== end EnhancedAuthSocketClientTest =====");
			
			client.close();
	}
	
	
}