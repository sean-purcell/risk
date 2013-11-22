package risk.inet;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;

import risk.game.Game;

public class Client extends SocketHandler{
	public static Client makeClient(Game g, String ip){
		Socket server = null;
		try {
			server = new Socket(ip, 4913);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("Server not found");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new Client(server, g);
	}
	
	private WeakReference<Game> g;
	
	public Client(Socket server, Game g){
		super(server,"Client");
		this.g = new WeakReference<Game>(g);
	}
	
	protected void useMessage(String message){
		g.get().message(message, 6);
	}
	
	protected boolean running(){
		return g.get() != null;
	}
	
	public void requestResync(){
		String message = "resync";
		writeMessage(message);
	}
}
