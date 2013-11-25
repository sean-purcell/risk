package risk.inet;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import risk.game.Game;
import risk.lib.RiskThread;

public class HostMaster extends RiskThread{
	
	public static HostMaster createHostMaster(Game g){
		try {
			return new HostMaster(g);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not create server, abort.");
			return null;
		}
	}
	
	static List<HostServer> servers;
	
	private boolean acceptingPlayers;
	
	private Object acceptingPlayersLock;
	
	private WeakReference<Game> g;
	
	private ServerSocket server;
	private ServerSocket spectators;
	
	private static boolean second;
	private boolean type;
	private static int index;
	
	public HostMaster(Game g) throws IOException{
		super("Server Master");
		servers = Collections.synchronizedList(new ArrayList<HostServer>());
		this.acceptingPlayersLock = new Object();
		this.g = new WeakReference<Game>(g);
		type = second;
		second = true;
		if(!type)
			this.server = new ServerSocket(4913);
		else
			this.spectators = new ServerSocket(4914);
	}
	
	public void run(){
		setAcceptingPlayers(true);
		while(getAcceptingPlayers()){
			try {
				Socket client = (type ? spectators : server).accept();
				HostServer serv = new HostServer(client,this,index); index++;
				serv.start();
				servers.add(serv);
				g.get().serverAdded(serv,client,type);
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
			return acceptingPlayers && g.get() != null;
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
	
	protected void resyncRequested(HostServer hs, OutputStream o){
		g.get().resyncRequested(hs,o);
	}
	
	boolean running(){
		return g.get() != null;
	}
	
	protected void finalize(){
		halt();
	}
	
	public void halt(){
		servers = null;
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		server = null;
	}
}
