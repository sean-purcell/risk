package risk.inet;

import java.net.Socket;

public class HostServer extends Thread{
	
	private Socket client;
	private HostMaster h;
	
	HostServer(Socket client, HostMaster h, int id){
		super("Server-"+Integer.toString(id));
		this.client = client;
		this.h = h;
	}
	
	@Override
	public void run(){
		
	}
}
