package GUI;
import java.io.*;
import java.util.*;
import java.lang.Object;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class FTP_Main extends JFrame implements ActionListener{
	
		//get local file_name
		static File search = new File("client");
		File[] file_read = search.listFiles();
		static String[] f = search.list();	
		
		static String[] str = {"test1","test2","test3","test4","test5"};
		
		//the parameter input in GUI panel
		static String s_account ;
		static String s_password;
		static String s_pin;
		static String s_ip;
		static String s_port;
		
		static int port_int;
		
		static String cmd;

		static JFrame ftp_frame = new JFrame("Cloud Storage System");
		
		//set up Label to show the textfield
		JLabel ac = new JLabel("Account");
		JLabel ps = new JLabel("Password");
		JLabel pin = new JLabel("PIN");
		
		//set up the TextField
		JTextField text_ac = new JTextField();
		JTextField text_pin = new JTextField();
			
		//set up password textfield
		JPasswordField text_ps = new JPasswordField();
		
		//ftp panel
		JLabel lb_ip = new JLabel("IP");
		JLabel lb_port = new JLabel("PORT");
		JLabel lb_c_fname = new JLabel("file in client");
		JLabel lb_s_fname = new JLabel("file in server");
		
		static JList<String> client_selector = new JList<String>(f);
		//static JList<String> server_selector = new JList<String>(str);
		JScrollPane client_scroll = new JScrollPane(client_selector);
		
		
		JTextField txt_ip = new JTextField();
		JTextField txt_port = new JTextField();
		
		JButton btn_connect = new JButton("connect");
		JButton btn_upload = new JButton("Upload");
		JButton btn_download = new JButton("Download");
		
		
	public FTP_Main(){		
		System.out.println(s_account);
		System.out.println(s_password);
		System.out.println(s_pin);
		System.out.println(s_ip);
		System.out.println(s_port);
		

		
			
		ftp_frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent arg0){
				System.exit(1);
			}
		});

		ac.setBounds(5,5,50,20);
		ps.setBounds(140,5,60,20);
		pin.setBounds(300,5,30,20);
		
		
		text_ac.setBounds(65,5,60,20);
		text_ps.setBounds(210,5,60,20);
		text_pin.setBounds(340,5,40,20);
			
		
		lb_ip.setBounds(390,5,20,20);
		lb_port.setBounds(480,5,50,20);
		lb_c_fname.setBounds(5,30,80,30);
		lb_s_fname.setBounds(350,30,80,30);
		
		txt_ip.setBounds(420,5,50,20);
		txt_port.setBounds(540,5,50,20);
			
		client_scroll.setBounds(5,60,300,380);
		
		
		btn_connect.setBounds(600,5,80,20);
		btn_upload.setBounds(205,450,100,40);
		btn_download.setBounds(550,450,100,40);
		
		
		//set up ftp frame
		ftp_frame.setLayout(null);
		ftp_frame.setSize(800,600);
		ftp_frame.setBackground(Color.WHITE);
		ftp_frame.setLocation(500,300);
		
		btn_connect.addActionListener(this);
		btn_upload.addActionListener(this);
		btn_download.addActionListener(this);
		
		ftp_frame.add(client_scroll);
		
		ftp_frame.add(lb_ip);
		ftp_frame.add(lb_port);
		ftp_frame.add(lb_c_fname);
		ftp_frame.add(lb_s_fname);
		ftp_frame.add(txt_ip);
		ftp_frame.add(txt_port);
		ftp_frame.add(btn_upload);
		ftp_frame.add(btn_download);
		ftp_frame.add(btn_connect);
		ftp_frame.add(ac);
		ftp_frame.add(ps);
		ftp_frame.add(pin);
		ftp_frame.add(text_ac);
		ftp_frame.add(text_ps);
		ftp_frame.add(text_pin);
		
		ftp_frame.setVisible(true);
	}
	@Override
	 public void actionPerformed(ActionEvent ae){
        String action = ae.getActionCommand();
		byte [] b = cmd.getBytes();
		//if(cmd.equals("")){
			if(action.equals("connect")){
				s_account = text_ac.getText();
				s_password = new String(text_ps.getPassword());
				s_pin = text_pin.getText();
				s_ip = txt_ip.getText();
				s_port = txt_port.getText();
				port_int = Integer.parseInt(s_port);
				System.out.println(s_account);
				System.out.println(s_password);
				System.out.println(s_pin);
				System.out.println(s_ip);
				System.out.println(port_int);
				cmd = new String("connect");
				
				System.out.println(cmd);
			}
			else if(action.equals("Upload")){
				cmd ="upload";
				System.out.println(cmd);
			}
			else if(action.equals("Download")){
				cmd = "download";
				System.out.println(cmd);
			}
		//}
		return;
	 }
	 public static void event(String cmd){
		String[] file_in_server=new String[100];
		JList<String> server_selector;
		byte [] b = cmd.getBytes();
		if(cmd.equals("connect")){
			try{
				System.out.println("cccc");
				Client c = new Client(s_account,s_password,s_pin,s_ip,port_int);
				c.connect();
				file_in_server = c.recieve_file_list();
				server_selector = new JList<String>(file_in_server);
				JScrollPane server_scroll = new JScrollPane(server_selector);
				
				server_scroll.setBounds(350,60,300,380);
				
				ftp_frame.add(server_scroll);
			}catch(Exception e){
				System.out.println("error");
			}
		}
		else if(cmd.equals("upload")){
			String x = f[client_selector.getSelectedIndex()];
			System.out.println("upload file: "+x);
		}
		else if(cmd.equals("download")){
			
			//String x = file_in_server[server_selector.getSelectedIndex()];
			//System.out.println(x);
		}
	 }
	 
	public static void main(String[] args)throws Exception{
		JFrame demo = new FTP_Main();
		cmd = "";
		String temp = "";
		while(true){
			if(temp.equals(cmd)){
				event(cmd);
				temp = "";
			}
			
		}
		
	}
	
	
}