package risk.inet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import risk.Risk;
import risk.lib.RiskThread;

public abstract class SocketHandler extends RiskThread{

	private Socket sock;

	private List<String> buffer;

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
			out = new PrintWriter(sock.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			while(running()){
				if(!buffer.isEmpty()){
					String message = buffer.remove(0);
					writeLength(message);
					out.write(message);
					out.flush();
					//Risk.showMessage(message);
					System.out.println("Message written");
				}
				if(in.ready()){
					String message = read(in);
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
	
	public static String read(BufferedReader i, InputStream in){
		try{
			int size = (in.read() << 8) | in.read();
			if(size > 10)
			Risk.out.println("Read Length: " + size);
			char[] buf = new char[size];

			i.read(buf);
			return new String(buf);
		}
		catch(IOException e){
		}
		return null;
	}
	
	private void write(String str){
		byte[] out = str.getBytes();
		byte[] len = new byte[2];
		int length = out.length;
		len
	}
	
	private void writeLength(String str){
		int i = str.length();
		if(i >= 65536){
			System.out.println("Attemping to print message longer than 65536 bytes");
		}
		if(i > 10)
		Risk.out.println("Message Length: " + Integer.toBinaryString(i));
		byte[] bytes = new byte[2];
		bytes[0] = (byte) ((i & 0xFF00) >>> 8);
		bytes[1] = (byte) (i & 0x00FF);
		try {
			sock.getOutputStream().write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
