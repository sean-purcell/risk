package risk.inet;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import risk.game.Game;

public class HostMaster extends Thread{
	
	public static HostMaster createHostMaster(Game g){
		try {
			return new HostMaster(g);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not create server, abort.");
			return null;
		}
	}
	
	List<HostServer> servers;
	
	private boolean acceptingPlayers;
	
	private Object acceptingPlayersLock;
	
	private WeakReference<Game> g;
	
	private ServerSocket server;
	
	public HostMaster(Game g) throws IOException{
		super("Server Master");
		servers = new ArrayList<HostServer>();
		this.acceptingPlayersLock = new Object();
		this.g = new WeakReference<Game>(g);
		
		this.server = new ServerSocket(4913);
	}
	
	public void run(){
		int index = 0;
		setAcceptingPlayers(true);
		while(getAcceptingPlayers()){
			try {
				Socket client = server.accept();
				HostServer serv = new HostServer(client,this,index); index++;
				serv.start();
				servers.add(serv);
				g.get().serverAdded(client);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not accept client.");
			}
		}
	}
	
	public void setAcceptingPlayers(boolean b){
		synchronized(acceptingPlayersLock){
			acceptingPlayers = b;
		}
	}
	
	private boolean getAcceptingPlayers(){
		synchronized(acceptingPlayersLock){
			return acceptingPlayers;
		}
	}
	
	public void message(String message, HostServer source){
		for(HostServer h : servers){
			if(h != source)
				h.writeMessage(message);
		}
		if(source != null){
			g.get().message(message, 5);
		}
	}
	
	boolean running(){
		return g.get() != null;
	}
}
