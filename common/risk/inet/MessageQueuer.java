package risk.inet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import risk.game.Game;
import risk.lib.RiskThread;

public class MessageQueuer extends RiskThread{
	
	private List<String> mQueue;
	private Game g;
	
	public MessageQueuer(Game g){
		mQueue = Collections.synchronizedList(new ArrayList<String>());
		
		this.g = g;
		
		this.setDaemon(true);
		this.start();
	}
	
	public void run(){
		while(mQueue != null){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
			if(!mQueue.isEmpty()){
				g.message(mQueue.remove(0), 7);
			}
		}
	}
	
	public void addMessage(String s){
		mQueue.add(s);
	}

	public void halt(){
		mQueue = null;
	}
}
