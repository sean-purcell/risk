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

public abstract class SocketHandler extends Thread{

	private Socket sock;

	private List<String> buffer;

	SocketHandler(Socket client, String id){
		super(id);
		this.sock = client;
		buffer = Collections.synchronizedList(new ArrayList<String>());
		this.setDaemon(true);
	}
	
	@Override
	public void run(){
		PrintWriter out = null;
		BufferedReader in = null;
		try{
			out = new PrintWriter(sock.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			while(running()){
				if(!buffer.isEmpty()){
					out.write(buffer.remove(0));
					out.flush();
				}
				if(in.ready()){
					String message = Risk.read(in);
					useMessage(message);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			out.close();
			try {
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
}
