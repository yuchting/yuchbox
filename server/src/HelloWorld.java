
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;



class Inte{
	int m_value;
	Inte(int _val){
		m_value = _val;
	}
}
/*!
 *  @brief note
 *  @author tzz
 *  @version 0.1
 */
public class HelloWorld {
	/*!
	 *  @brief main function
	 *  @param arg  parameters
	 */
	public static void main(String arg[]){
		
		HelloWorld test = new HelloWorld(); 
		test.test3();
	}
	
	
	public void berryTest(){
		try{
			
			Socket t_socket = GetSocketServer("111111","localhost",9716);
			
			sendReceive t_receive = new sendReceive(t_socket);
			
			while(true){

				ByteArrayInputStream in = new ByteArrayInputStream(t_receive.RecvBufferFromSvr());
				switch(in.read()){
					case msg_head.msgMail:
						fetchMail t_mail = new fetchMail();
						t_mail.InputMail(in);
						prt("receive idx: " + t_mail.GetMailIndex() + " subject: " + t_mail.GetSubject());
						
						// send msgSendMail to increase fetch index imm
						//
						ByteArrayOutputStream t_os = new ByteArrayOutputStream();
						t_os.write(msg_head.msgSendMail);
						fetchMail.WriteInt(t_os,t_mail.GetMailIndex());
						t_receive.SendBufferToSvr(t_os.toByteArray(), true);
						
						// TODO display in berry
						//
						
						break;
					case msg_head.msgSendMail:
						
						// TODO display in berry
						// the mail has been send
						//
						
						break;
				}
			}
			
		}catch(Exception _e){
			prt(_e.getMessage());
			_e.printStackTrace();
		}
	}
	
	public static Socket GetSocketServer(String _userPassword,String _host,int _port)throws Exception{
		
		String	key				= "YuchBerryKey";  
		
		char[] keyStorePass		= _userPassword.toCharArray();
		char[] keyPassword		= _userPassword.toCharArray();
		
		KeyStore ks				= KeyStore.getInstance(KeyStore.getDefaultType());
		
		ks.load(new FileInputStream(key),keyStorePass);
		
		KeyManagerFactory kmf	= KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks,keyPassword);
		
		SSLContext sslContext = SSLContext.getInstance("SSLv3");
		sslContext.init(kmf.getKeyManagers(),null,null);
		  
		SSLSocketFactory factory=sslContext.getSocketFactory();
		
		return factory.createSocket(_host,_port);
		  
	}

	public void test3(){
		
		try{
			
			Properties p = new Properties(); 
			p.load(new FileInputStream("config.ini"));
			p.setProperty("userFetchIndex",Integer.toString(120));
		}catch(Exception _e){
			sendReceive.prt(_e.getMessage());
			_e.printStackTrace();
		}
	}
	public void test2(){
		byte[] t_data = {0,2,3,4,5,6,7,8,9};
		ByteArrayInputStream in = new ByteArrayInputStream(t_data);
		
		byte[] t_rdata = new byte[5];
		in.read(t_rdata,0,t_rdata.length);
		
		prtA(t_rdata);
		
	}
	public void test1(){
		try{
			byte[] t_data = new byte[6540];
			
			for(int i = 0;i < t_data.length ;i++){
				t_data[i] = (byte)(Math.random() * 100);
			}
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			GZIPOutputStream zo = new GZIPOutputStream(os);
			zo.write(t_data);
			zo.close();
			
			ByteArrayOutputStream t_finalos = new ByteArrayOutputStream();
			
			final int length = (t_data.length << 16) | (os.toByteArray().length);
			
			WriteInt(t_finalos,length);
			t_finalos.write(os.toByteArray());
			
			t_finalos.close();
			
			//prtA(t_finalos.toByteArray());
				
			ByteArrayInputStream in = new ByteArrayInputStream(t_finalos.toByteArray());
			
			
			final int t_rLength = ReadInt(in);
			final int t_orgLength = t_rLength >>> 16;
			final int t_zipLength = t_rLength & 0x0000ffff;
			
			byte[] t_zipdata = new byte[t_zipLength];
			byte[] t_orgdata = new byte[t_orgLength];
			
			in.read(t_zipdata);
			
			//prtA(t_zipdata);
			
			GZIPInputStream zi	= new GZIPInputStream(
									new ByteArrayInputStream(t_zipdata));
			
			int t_readIndex = 0;
			int t_readNum = 0;
			while((t_readNum = zi.read(t_orgdata,t_readIndex,t_orgLength - t_readIndex)) > 0){
				t_readIndex += t_readNum;
			}
			
			
			prtA(t_orgdata);
			
		}catch(Exception _e){
			
			prt(_e.getMessage());
			_e.printStackTrace();			
		}
	}
	public void test(){
		try{
			int t_version = 5000;
			Vector<String> temp = new Vector<String>();
			for(int i = 0;i < 5;i++){				
				temp.addElement(new String("input " + Math.random()));
			}
			ByteArrayOutputStream t_stream = new ByteArrayOutputStream();
			
			WriteInt(t_stream,t_version);
			WriteStringVector(t_stream,temp);
			
			ByteArrayInputStream t_stream1 = new ByteArrayInputStream(t_stream.toByteArray());
			
			int t_version1 = 0;
			Vector<String> temp1 = new Vector<String>();
			
			t_version1 = ReadInt(t_stream1);
			ReadStringVector(t_stream1,temp1);
			
			
			prt("version " + t_version1);
			for(int i = 0;i < temp1.size();i++){
				prt(temp1.get(i));
			}
		}catch(Exception _e){
			prt(_e.getMessage());
			_e.printStackTrace();
		}
	}
	private void WriteStringVector(OutputStream _stream,Vector<String> _vect)throws Exception{
		
		final int t_size = _vect.size();
		_stream.write(t_size);
		
		for(int i = 0;i < t_size;i++){
			WriteString(_stream,_vect.get(i));
		}
	}

	private int ReadInt(InputStream _stream)throws Exception{
		return _stream.read() | (_stream.read() << 8) | (_stream.read() << 16) | (_stream.read() << 24);
	}

	private void WriteInt(OutputStream _stream,int _val)throws Exception{
		_stream.write(_val);
		_stream.write(_val >>> 8 );
		_stream.write(_val >>> 16);
		_stream.write(_val >>> 24);
	}
	
	private void WriteString(OutputStream _stream,String _string)throws Exception{
		WriteInt(_stream,_string.length());
		_stream.write(_string.getBytes());
	}
		
	private void ReadStringVector(InputStream _stream,Vector<String> _vect)throws Exception{
		
		_vect.clear();
		
		int t_size = 0;
		t_size = _stream.read();
		
		for(int i = 0;i < t_size;i++){
			_vect.addElement(ReadString(_stream));
		}
	}
	
	private String ReadString(InputStream _stream)throws Exception{
		
		byte[] t_buffer = new byte[ReadInt(_stream)];
		
		_stream.read(t_buffer);	
		return new String(t_buffer);
	}
	
	static void prt(String s) {
		System.out.println(s);
	}
	
	static void prtA(byte[] a) {
		
		for(int i = 0;i < a.length;i++){
			prt(String.valueOf(a[i]));
		}
	}
}
