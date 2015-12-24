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
		
			//SK事實上為256位元長度，我們將其拆對半分別作為加密金鑰與IV//
			//--------------加解密前先把key和iv拿出---------------------------------------//
			byte[] sk = client.getSessionKey().getKeyValue();
			byte[] k = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
			byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
			//--------------加解密前先把key和iv拿出---------------------------------------//
			
			//--------------加密欲傳送的內容--------------------------------------------------//
			String msg = "hello, server.";
			byte[] encrypted_msg = CipherUtil.authEncrypt(k, iv, msg.getBytes());
			//--------------加密欲傳送的內容--------------------------------------------------//
            
			//--------------輸出密文-------------------------------------------------------//
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeInt(encrypted_msg.length);
			out.write(encrypted_msg);
			out.flush();
			//--------------輸出密文-------------------------------------------------------//
			
			//--------------接收server傳來的密文---------------------------------------------//
			DataInputStream in = new DataInputStream(client.getInputStream());
			int msg_length = in.readInt();
			byte[] encrypted_rcvd_msg = new byte [ msg_length ];
			in.readFully(encrypted_rcvd_msg);
			//--------------接收server傳來的密文---------------------------------------------//
            
			//--------------解密server傳來的密文---------------------------------------------//
			String rcvd_msg = new String(CipherUtil.authDecrypt(k, iv, encrypted_rcvd_msg));
			System.out.println("server say: " + rcvd_msg);
			//--------------解密server傳來的密文---------------------------------------------//

			System.out.println("===== end EnhancedAuthSocketClientTest =====");
			
			client.close();
	}
	
	
}