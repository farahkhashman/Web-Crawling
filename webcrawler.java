import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

public class webcrawler {
	public final int WIDTH = 1000, HEIGHT = 700;
	
	private JTextField in;	// our text boxes
	private JTextField in2;
	private JTextArea out;
	private JFrame frame;
	
	public static void main(String args[]){
		new webcrawler();
	}
	
	public webcrawler() {
		// input box setup
		in = new JTextField();
		in.setEditable(true);
		in.setPreferredSize(new Dimension(WIDTH,100));
		in.setText("Enter an artist");
		in.setForeground(Color.gray);
		in.setMargin(new Insets(10,10,10,10));
			
		// when user hits enter, run our search
		in.addKeyListener(new KeyListener() {
		int count = 0;
		String title = "";
		public void keyTyped(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER && count==1) {
				title += "\n" +in.getText();
				System.out.println(title);
				getInfo(title);
				count = 0;
			}
			else if(e.getKeyChar() == KeyEvent.VK_ENTER && count == 0){
				title = in.getText().trim();
				count++;
				in.setText("Enter a song");
			}
				
			}
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
			
		// I'll display a prompt until the user presses on the input box
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
			
			// output box & scroll bar setup
			out = new JTextArea();
			out.setEditable(false);
			out.setMargin(new Insets(10,10,10,10));
			JScrollPane scroll = new JScrollPane (out);
			scroll.setVerticalScrollBarPolicy(
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroll.setPreferredSize(new Dimension(WIDTH, HEIGHT-100));
			
			// inner panel setup
			JPanel panel = new JPanel();
			BoxLayout boxlayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
			panel.setLayout(boxlayout);
			panel.setBorder(BorderFactory.createTitledBorder("Lyrics Fetcher"));
			panel.add(scroll);
			panel.add(in);
			
			// frame setup - I'll let the user resize this one
			frame = new JFrame();
			frame.setSize(WIDTH,HEIGHT);
			frame.setResizable(true);
			frame.add(panel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocation(100, 0);
			frame.setVisible(true);
	}
	
	// scrapes text from a webpage
		public void getInfo(String topic) {
			
			// changes text boxes while searching
			in.setText("");
			out.setText("Searching database...");
			out.update(out.getGraphics());
			
			// to match wiki url format
			topic = topic.toLowerCase();
			String artist = topic.substring(0, 1).toUpperCase() + topic.substring(1, topic.indexOf("\n"));
			String song = topic.substring(topic.indexOf("\n")+1);
			while (artist.indexOf(' ') != -1) {
				int i = artist.indexOf(' ');
				artist = artist.substring(0,i) + '-' + artist.substring(i+1);
			}
			while(song.indexOf(' ') != -1) {
				int i = song.indexOf(' ');
				song = song.substring(0,i) + '-' + song.substring(i+1);
			}
			
			topic = artist+'-'+song+'-'+"lyrics";
			System.out.println(topic);
				
			try {
				
				// get the page at desired url
				Document doc = Jsoup.connect("https://genius.com/"+topic).get();

				// we're going to get all 'paragraph' items from the html
				Elements pars = doc.select("p");
				
				// html text doesn't include new line characters, so we need to 
				// add them in ourselves
				out.setText("");
				boolean findSpace = false;
				Element e = pars.get(0);
				
				String text = e.text();
				String str = "";
				/*for (int i = 0; i < text.length(); i++) {
					if (i != 0 && i % (frame.getWidth()/8) == 0)
						findSpace = true;
					if (findSpace && text.charAt(i) == ' ') {
						str += "\n";
						findSpace = false;
					}
					else
						str += text.charAt(i);
				}*/
				
				
				int k = 0;
				for(Element b : pars) {
					if(b.equals("br")) {
						int j = pars.indexOf(b);
						System.out.println(j);
						str += pars.text().substring(k, j);
						str += "\n";
						k = j+1;
					}
				}
				out.setText(out.getText()+str);
			} 
			
			// if connecting to the url fails
			catch (Exception e) {
				//e.printStackTrace(System.err);
				out.setText("Topic not found. Try another wording");
			}
		}
	}
	
