package risk.inet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import risk.Risk;
import risk.lib.RiskThread;

public abstract class SocketHandler extends RiskThread{

	Socket sock;

	private List<String> buffer;

	OutputStream o;
	InputStream i;
	
	private PrintWriter out;
	private BufferedReader in;
	
	SocketHandler(Socket client, String id){
		super(id);
		this.sock = client;
		buffer = Collections.synchronizedList(new ArrayList<String>());
		this.setDaemon(true);
	}
	
	@Override
	public void run(){
		try{
			o = sock.getOutputStream();
			i = sock.getInputStream();
			while(running()){
				if(!buffer.isEmpty()){
					String message = buffer.remove(0);
					write(message);
					o.flush();
					//Risk.showMessage(message);
					System.out.println("Message written");
				}
				if(i.available() > 0){
					String message = new String(read(), Charset.forName("UTF-8"));
					if("!!!!!!!!".equals(message)){
						System.err.println("Connected client has stopped, EXIT NOW");
						System.exit(-10);
					}
					//Risk.showMessage(message);
					useMessage(message);
					System.out.println("Message received: " + message);
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			out.close();
			try {
				in.close();
				sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected abstract boolean running();
	
	protected abstract void useMessage(String message);
	
	public void writeMessage(String message){
		buffer.add(message);
		this.interrupt();
	}
	
	public void halt(){
		finalize();
	}
	
	protected void finalize(){
		System.out.println("Sending shutdown message");
		out.write("" + (char) 0 + (char) 8 + "!!!!!!!!");
		out.flush();
		out.close();
		try {
			in.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] read(){
		try{
			int size = (i.read() << 8) | i.read();
			if(size > 10)
			Risk.out.println("Read Length: " + size);
			byte[] buf = new byte[size];

			i.read(buf);
			return buf;
		}
		catch(IOException e){
		}
		return null;
	}
	
	public synchronized void write(String str){
		byte[] out = str.getBytes(Charset.forName("UTF-8"));
		write(out);
	}
	
	public synchronized void write(byte[] out){
		byte[] len = new byte[2];
		int length = out.length;
		
		Risk.out.println("Message Length: " + Integer.toString(length));
		
		len[0] = (byte) ((length & 0xFF00) >>> 8);
		len[1] = (byte) (length & 0x00FF);
		try {
			o.write(len);
			o.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public OutputStream getOutputStream(){
		return o;
	}
}
