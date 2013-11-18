package risk.inet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import risk.game.Game;

public class HostMaster extends Thread{
	
	List<HostServer> servers;
	
	private boolean acceptingPlayers;
	
	private Object acceptingPlayersLock;
	
	private Game g;
	
	private ServerSocket server;
	
	public HostMaster(Game g){
		super("Server Master");
		servers = new ArrayList<HostServer>();
		this.acceptingPlayersLock = new Object();
		this.g = g;
		try {
			this.server = new ServerSocket(4193);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not create server, abort.");
			System.exit(-2);
		}
	}
	
	public void run(){
		int index = 0;
		setAcceptingPlayers(true);
		while(getAcceptingPlayers()){
			try {
				Socket client = server.accept();
				HostServer serv = new HostServer(client,this,index); index++;
				servers.add(serv);
				g.serverAdded();
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
}
