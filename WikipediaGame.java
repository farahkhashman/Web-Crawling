import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class WikipediaGame {
	public final int WIDTH = 1000, HEIGHT = 700;
	
	private JTextField in;	// our text boxes
	private JTextField in2;
	private JTextArea out;
	private JFrame frame;
	HashMap<String, String> leadsto = new HashMap<String, String>();
	
	public static void main(String args[]){
		new WikipediaGame();
	}
	
	public WikipediaGame() {
		// input box setup
		in = new JTextField();
		in.setEditable(true);
		in.setPreferredSize(new Dimension(500, 100));
		in.setText("Starting Point");
		in.setForeground(Color.gray);
		in.setMargin(new Insets(10,10,10,10));
		
		in2 = new JTextField();
		in2.setEditable(true);
		in2.setPreferredSize(new Dimension(500, 100));
		in2.setText("Target Point");
		in2.setForeground(Color.gray);
		in2.setMargin(new Insets(10,10,10,10));
		
	in2.addKeyListener(new KeyListener() {
		public void keyTyped(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER) {
				String starting = in.getText().trim();
				String target = in2.getText().trim();
				getInfo(starting, target);
			}
		}
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
	
	in.addMouseListener(new MouseListener() {
		public void mouseClicked(MouseEvent e) {
			in.setText("");
			in.setForeground(Color.black);
		}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
	});
		
		out = new JTextArea();
		out.setEditable(false);
		out.setMargin(new Insets(10,10,10,10));
		JScrollPane scroll = new JScrollPane (out);
		scroll.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setPreferredSize(new Dimension(WIDTH, HEIGHT-100));
		
		JPanel panel = new JPanel();
		BoxLayout boxlayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(boxlayout);
		panel.setBorder(BorderFactory.createTitledBorder("Wikipedia Game"));
		panel.add(scroll);
		panel.add(in);
		panel.add(in2);
		
		frame = new JFrame();
		frame.setSize(WIDTH,HEIGHT);
		frame.setResizable(true);
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(100, 0);
		frame.setVisible(true);
	}
	
	public void getInfo(String topic, String target) {
		in.setText("");
		out.setText("Searching database...");
		out.update(out.getGraphics());
		
		topic = topic.substring(0,1).toUpperCase()+topic.substring(1);
		while (topic.indexOf(' ') != -1) {
			int i = topic.indexOf(' ');
			topic = topic.substring(0,i) + '_' + topic.substring(i+1,i+2).toUpperCase() + topic.substring(i+2);
		}
		
		target = target.substring(0,1).toUpperCase()+target.substring(1);
		while(target.indexOf(' ') != -1) {
			int i = target.indexOf(' ');
			target = target.substring(0,i) + '_' + topic.substring(i+1);
		}
		
		BFS("wiki/"+target, "wiki/"+topic);
	
	}
	
	public void BFS(String target, String starting) {
		ArrayList<String> visited = new ArrayList<String>();
		ArrayList<String> toVisit = new ArrayList<String>();
		ArrayList<String> urls = new ArrayList<String>();
		String current = starting;

		toVisit.add(current);
		while(!toVisit.isEmpty()) {
			current = toVisit.get(0);
			System.out.println(current);
			//System.out.println(target.substring(5));
			try {
				Document doc = Jsoup.connect("https://en.wikipedia.org/"+current).get();
				System.out.println("connect successful " + "https://en.wikipedia.org/"+current);
				Elements pars = doc.select("div#bodyContent").select("a");
				for(Element e : pars) {
					if(e.toString().contains("/wiki")) {
						urls.add(e.attr("href"));
					}
				}
				for(String neighbour : urls) {
					if(!toVisit.contains(neighbour) && !visited.contains(neighbour) && !neighbour.contains(target)) {
						toVisit.add(neighbour);
						System.out.println(neighbour);
						leadsto.put(neighbour, current);
					}
					else if(neighbour.contains(target)) {
						leadsto.put(neighbour, current);
						backtrace(starting, neighbour);
						return;
					}
				}
				visited.add(toVisit.remove(0));
				
			
			} catch(IOException e1){
				try {
					while(current.indexOf('_') != -1) {
						int i = current.indexOf('_');
						current = current.substring(5,i) + '+' + current.substring(i+1);
					}
					if(current.contains("wiki/")) {
						int j = current.indexOf("wiki/");
						current = current.substring(j+5);
						//System.out.println(current);
					}
					
					Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/Special:Search?search="+current+"&go=Go").get();
					if(!doc.select("div.searchdidyoumean").select("em").first().toString().equals(null)) {
						String didyou = doc.select("div.searchdidyoumean").select("em").first().toString();
						didyou = didyou.substring(4, didyou.indexOf("</em>"));
						out.setText("");
						out.setText("Did you mean");
						out.setText(out.getText()+" "+didyou+" or: "+"\n");
					}
					Elements pars = doc.select("li.mw-search-result");
					//System.out.println(pars.size());
					ArrayList<String> list = new ArrayList<String>();
					for(Element e : pars) {
						if(e.select("a").attr("href").contains("/wiki")) {
							//System.out.println(e.select("a").attr("title"));
							//System.out.println(e.select("a").attr("href"));
							list.add(e.select("a").attr("title"));
						}
					}
					//System.out.println(list.size());
					
					for(String s : list) {
						System.out.println(s);
						out.setText(out.getText()+"\n"+s);
					}
					out.setText(out.getText()+"\n"+"please re-enter starting point with the correct format from above");
					out.update(out.getGraphics());
					return;
				}
				catch(IOException e2){
					System.out.println("no topic");
					return;
				}
			}
			
		}
	}
	
	public void backtrace(String initial, String target) {
		String s = target;
		String next = "";
		out.setText("");
		out.setText("https://en.wikipedia.org/"+target);
		
		while(!s.contains(initial)) {
			next = leadsto.get(s);
			System.out.println(next);
		
		out.setText(out.getText()+"\n"+"https://en.wikipedia.org/"+next);
		s = next;
		}
		
	}
}

