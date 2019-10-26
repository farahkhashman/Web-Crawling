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

// program to scrape text content from wikipedia
public class WikiInfo {
	
	public final int WIDTH = 1000, HEIGHT = 700;
	
	private JTextField in;	// our text boxes
	private JTextArea out;
	private JFrame frame;
	
	public static void main(String args[]){
		new WikiInfo();
	}
		
	// get our graphics going
	public WikiInfo() {
		
		// input box setup
		in = new JTextField();
		in.setEditable(true);
		in.setPreferredSize(new Dimension(WIDTH,100));
		in.setText("Enter a topic");
		in.setForeground(Color.gray);
		in.setMargin(new Insets(10,10,10,10));
		
		// when user hits enter, run our wiki search
		in.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) 
					getInfo(in.getText().trim());		
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
		panel.setBorder(BorderFactory.createTitledBorder("Wikipedia Fetcher"));
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
		topic = topic.substring(0, 1).toUpperCase() + topic.substring(1);
		while (topic.indexOf(' ') != -1) {
			int i = topic.indexOf(' ');
			topic = topic.substring(0,i) + '_' + topic.substring(i+1,i+2).toUpperCase() 
					+ topic.substring(i+2);
		}
			
		try {
			
			// get the page at desired url
			Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/"+topic).get();

			// we're going to get all 'paragraph' items from the html
			Elements pars = doc.select("p");
			
			// if wikipedia isn't sure which page to go to, it will
			// give a first paragraph containing "may refer to:"
			// so check if this is present in the first paragraph 
			// before proceeding
			String first = pars.first().text();
			if (first.length() > 10 && first.substring(first.length()-9).equals("refer to:")) {
				out.setText("Multiple topics match this description. Try again"
						+ " with more specificity");
				return;
			}
			
			// html text doesn't include new line characters, so we need to 
			// add them in ourselves
			out.setText("");
			boolean findSpace = false;
			for (Element e : pars) {
				String text = e.text();
				String str = "";
				for (int i = 0; i < text.length(); i++) {
					if (i != 0 && i % (frame.getWidth()/8) == 0)
						findSpace = true;
					if (findSpace && text.charAt(i) == ' ') {
						str += "\n";
						findSpace = false;
					}
					else
						str += text.charAt(i);
				}
				out.setText(out.getText()+str+"\n\n");
			}
		} 
		
		// if connecting to the url fails
		catch (Exception e) {
			//e.printStackTrace(System.err);
			out.setText("Topic not found. Try another wording");
		}
	}
}