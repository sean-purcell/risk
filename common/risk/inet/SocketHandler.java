package risk.inet;

import java.io.BufferedReader;
import java.io.IOException;
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
					out.write(message);
					out.flush();
					Risk.showMessage(message);
					System.out.println("Message written");
				}
				if(in.ready()){
					String message = Risk.read(in);
					if("!!!!!!!!".equals(message)){
						System.err.println("Connected client has stopped, EXIT NOW");
						System.exit(-10);
					}
					Risk.showMessage(message);
					useMessage(message);
					System.out.println("Message received: " + message);
				}
				try {
					Thread.sleep(100);
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
		out.write("!!!!!!!!");
		out.flush();
		out.close();
		try {
			in.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
