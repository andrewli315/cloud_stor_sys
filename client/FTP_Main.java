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

import javax.swing.plaf.metal.*;
public class FTP_Main extends JFrame{
	public static void main(String[] args){
		
		JFrame ftp_frame = new JFrame("Cloud Storage System");
		
		//get local file_name
		File search = new File("client");
		File[] file_read = search.listFiles();
		String[] f = search.list();	
		
		String[] str = {"test1","test2","test3","test4","test5"};
		
		
		//set up Label to show the textfield
		JLabel ac = new JLabel("Account");
		JLabel ps = new JLabel("Password");
		JLabel pin = new JLabel("PIN");
		
		//set up the TextField
		JTextField text_ac = new JTextField();
		JTextField text_pin = new JTextField();
			
		//set up password textfield
		JPasswordField text_ps = new JPasswordField();
		
		
		
		
		//call the server.java the funtion to get file name in server
		String [] f_in_server;
		
		//ftp panel
		JLabel lb_ip = new JLabel("IP");
		JLabel lb_port = new JLabel("PORT");
		JLabel lb_c_fname = new JLabel("file in client");
		JLabel lb_s_fname = new JLabel("file in server");
		
		JTextField txt_ip = new JTextField();
		JTextField txt_port = new JTextField();
		
		JButton btn_connect = new JButton("connect");
		JButton btn_upload = new JButton("Upload");
		JButton btn_download = new JButton("Download");
		
		//set scroll panel
		JList client_selector = new JList<String>(str);
		JList server_selector = new JList<String>(str);
		JScrollPane client_scroll = new JScrollPane(client_selector);
		JScrollPane server_scroll = new JScrollPane(server_selector);
			
			
		ftp_frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent arg0){
				System.exit(1);
			}
		});

		ac.setBounds(5,5,50,20);
		ps.setBounds(140,5,60,20);
		pin.setBounds(300,5,30,20);
		
		
		text_ac.setBounds(65,5,60,20);
		text_ps.setBounds(210,5,80,20);
		text_pin.setBounds(340,5,30,20);
			
		
		lb_ip.setBounds(390,5,20,20);
		lb_port.setBounds(480,5,50,20);
		lb_c_fname.setBounds(5,30,80,30);
		lb_s_fname.setBounds(350,30,80,30);
		
		txt_ip.setBounds(420,5,50,20);
		txt_port.setBounds(540,5,50,20);
			
		client_scroll.setBounds(5,60,300,380);
		server_scroll.setBounds(350,60,300,380);
		
		btn_connect.setBounds(600,5,80,20);
		btn_upload.setBounds(205,450,100,40);
		btn_download.setBounds(550,450,100,40);
		
		
		//set up ftp frame
		ftp_frame.setLayout(null);
		ftp_frame.setSize(800,600);
		ftp_frame.setBackground(Color.WHITE);
		ftp_frame.setLocation(500,300);
				
		ftp_frame.add(client_scroll);
		ftp_frame.add(server_scroll);
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
	
	
}