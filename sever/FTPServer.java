class FTPServer{
	
	DataInputStream netIn;
	DataOutputStream netOut;
	public Server(){
		
	}
	public void Authen(String card,String password)throws Exception{
		
	}
	public void recieve(String file_name)throws Exception{
		
			
	}
	public void tran_Sig()throws Exception{
		EnhancedProfileManager sender = EZCardLoader.loadEnhancedProfile(new File("server.card"), "ps");//card 要讓client給String
		EnhancedProfileManager receiver = EZCardLoader.loadEnhancedProfile(new File("client.card"), "ps");
		
		Signature sig = new SignatureClient.SignatureCreater()
				.initSignerID(sender.getPrimitiveProfile().getIdentifier())
				.initReceiverID(receiver.getPrimitiveProfile().getIdentifier())
				.initMessage("12345".getBytes())
				.initSignatureKey(sender.getPrimitiveProfile().getSignatureKey())
				.initTimestamp(System.nanoTime()).createSignature();

		boolean result = SignatureClient.verifyWithoutArbiter(sig, receiver.getPrimitiveProfile());
		
		System.out.println(result);//這邊要傳去給client接收
	}
	
}