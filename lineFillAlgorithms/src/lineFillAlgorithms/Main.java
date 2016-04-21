package lineFillAlgorithms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JFrame {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DrawingPanel dp;
	private JPanel mainPanel;

	public static void main(String args[]) {
		
		Main m = new Main();
		
		m.init();
	}
	
	public Main() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.setTitle("Line Filling Algorithms");
		this.setPreferredSize(new Dimension(1110,900));
		this.setLocation(400, 100);
		this.pack();
		this.setVisible(true);
		
		
		dp = new DrawingPanel();
		mainPanel = new JPanel(new BorderLayout());
		
		//mainPanel.setBackground(Color.RED);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.add(dp);
		
		dp.setBackground(Color.WHITE);
		dp.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		

		
	}
	
	public void init() {
		dp.convexSplit();
		
		dp.zoneSearch();
	}
}
