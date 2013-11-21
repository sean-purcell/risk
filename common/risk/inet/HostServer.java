package risk.inet;

import java.net.Socket;

public class HostServer extends SocketHandler{
	
	private HostMaster h;
	
	HostServer(Socket client, HostMaster h, int id){
		super(client,"Server-"+Integer.toString(id));
		this.h = h;
	}
	
	protected void useMessage(String message){
		h.message(message, this);
	}
	
	protected boolean running(){
		return h.running();
	}
}
