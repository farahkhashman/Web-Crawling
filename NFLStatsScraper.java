import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

// program to scrape 2018-2019 NFL statistical information 
// using nfl.com
public class NFLStatsScraper {
	
	private JTextField in, out;		// our text boxes
	private String mode = "RUSHING";	
	private JFrame frame;
	
	private final int WIDTH = 400, HEIGHT = 250;
	
	public static void main(String args[]){
		new NFLStatsScraper();
	}
	
	// sets up the graphics
	public NFLStatsScraper() {
		
		// sets up our 3 buttons in a panel
		JPanel buttonPanel = new JPanel();
		JButton rushButton = new JButton("Rushing");
		rushButton.setForeground(Color.red);
		JButton passButton = new JButton("Passing");
		JButton recButton = new JButton("Receiving");
		rushButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mode = "RUSHING";
				rushButton.setForeground(Color.red);
				recButton.setForeground(Color.black);
				passButton.setForeground(Color.black);
			}
		});
		passButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mode = "PASSING";
				rushButton.setForeground(Color.black);
				recButton.setForeground(Color.black);
				passButton.setForeground(Color.red);
			}
		});
		recButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mode = "RECEIVING";
				rushButton.setForeground(Color.black);
				recButton.setForeground(Color.red);
				passButton.setForeground(Color.black);
			}
		});
		buttonPanel.add(rushButton);
		buttonPanel.add(passButton);
		buttonPanel.add(recButton);
		buttonPanel.setPreferredSize(new Dimension(WIDTH,50));
		
		// sets up the input text box
		in = new JTextField();
		in.setEditable(true);
		in.setPreferredSize(new Dimension(WIDTH,100));
		in.setText("Enter a Name");
		in.setForeground(Color.gray);
		
		// when enter is pressed, run the web search
		in.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) 
					getInfo(in.getText(), 1);		
			}
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
		
		// clear the prompt when box is clicked on
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
		
		// sets up the output text box
		out = new JTextField();
		out.setEditable(false);
		out.setPreferredSize(new Dimension(WIDTH,100));
		
		// sets up the container panel
		JPanel panel = new JPanel();
		BoxLayout boxlayout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(boxlayout);
		panel.setBorder(BorderFactory.createTitledBorder("NFL Stats"));
		panel.add(out);
		panel.add(in);
		panel.add(buttonPanel);
		
		// frame setup
		frame = new JFrame();
		frame.setSize(WIDTH,HEIGHT);
		frame.setResizable(false);
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(300, 200);
		frame.setVisible(true);
	}
		
	// scrapes data from the webpage
	public void getInfo(String name, int pageNum) {
		
		// changes the text boxes when searching
		in.setText("");
		out.setText("Searching database...");
		out.update(out.getGraphics());
		
		try {
			// gets the webpage, using the current page number and stat mode
			Document doc = Jsoup.connect("http://www.nfl.com/stats/categorystats?tabSeq=0&season=2018&seasonType=REG&d-447263-p="
					+pageNum+"&statisticCategory="+mode+"&qualified=true").get();

			// get first (only) table
			Element table = doc.select("table").get(0);
			
			// get (all) the rows from the table
			Elements rows = table.select("tr");
			
			// go through the rows
			for (Element row : rows) {
				
				// cols will hold each piece of information going across the row
				Elements cols = row.select("td");
				
				// make sure this row wasn't empty
				if (cols.size() > 1) {
					
					// player name is found in the first column
					String name2 = cols.get(1).text();
					
					// checks if current row is the player we're looking for
					if (name2.equalsIgnoreCase(name) || name2.toLowerCase().contains(
							name.toLowerCase())) {
						
						// columns 6 and 9 hold our rushing information
						if (mode.equals("RUSHING"))
							out.setText(name2 + ": " + cols.get(6).text()+ " rushing yds,  " + cols.get(9).text()+" tds");
						
						// columns 8, 11, and 12 hold our rushing information
						else if (mode.equals("PASSING"))
							out.setText(name2 + ": " + cols.get(8).text()+ " passing yds,  " + cols.get(11).text()+" tds,  " + cols.get(12).text()+" ints");
						
						// columns 5 and 9 hold our rushing information
						else
							out.setText(name2 + ": " + cols.get(5).text()+ " receiving yds,  " + cols.get(9).text()+" tds");
						
						return;   // no need to keep searching
					}
				}
			}

		} 
		
		// if we go past the last page, we'll get an error
		catch (Exception e) {
			out.setText("Player not found");
			return;
		}
		
		// if the player wasn't found on the current page, go to the next one
		getInfo(name, pageNum+1);
	}
}