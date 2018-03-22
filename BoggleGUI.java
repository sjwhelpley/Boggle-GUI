import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BoggleGUI
{
	static long numWordsFormed = 0;
	
	JFrame window;
	Container content;
	
	JTextArea numWords;
	JTextArea wordsFound;
	JScrollPane scroll;
	TreeSet<String> wordsResult;

	JLabel dimensionLabel;
	JPanel dimensionPanel;
	JComboBox<String> enter;
	
	JLabel filename;
	JTextField enterFilename;
	JPanel filePanel;
	
	JButton go;
	
	JPanel leftPanel, boardPanel, rightPanel; 
	String[] alphabet = {"a","b","c","d","e","f","g","h","i",
								"j","k","l","m","n","o","p","q","r",
								"s","t","u","v","w","x","y","z"};
	JButton[][] boardButtons;
	
	public BoggleGUI()
	{
		window = new JFrame("Boggle");
		content = window.getContentPane();
		content.setLayout(new GridLayout(1,3)); 
		ButtonListener listener = new ButtonListener();
		
		numWords = new JTextArea();
		wordsFound = new JTextArea();		
		scroll = new JScrollPane(wordsFound);
		wordsResult = new TreeSet<String>();

		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(numWords);
		rightPanel.setAlignmentX(numWords.CENTER_ALIGNMENT);
		rightPanel.add(scroll);
		
		dimensionLabel = new JLabel("Select Dimension Here", SwingConstants.CENTER);
		dimensionPanel = new JPanel();
		
		enter = new JComboBox<>();
		enter.addItem("2");
		enter.addItem("4");
		enter.addItem("5");
		enter.addItem("10");
		enter.setSelectedIndex(0);
		
		dimensionPanel.setLayout(new GridLayout(2, 1));
		dimensionPanel.add(dimensionLabel);
		dimensionPanel.add(enter);
		
		filename = new JLabel("Type Boggle Filename Here", SwingConstants.CENTER);
		enterFilename = new JTextField();
		
		filePanel = new JPanel();
		filePanel.setLayout(new GridLayout(2, 1));
		filePanel.add(filename);
		filePanel.add(enterFilename);
		
		go = new JButton("GO!"); 
		go.addActionListener(listener);
		
		leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(3,1));
		leftPanel.add(dimensionPanel);
		leftPanel.add(filePanel);
		leftPanel.add(go);
		
		boardPanel = new JPanel();
		
		enter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JComboBox comboBox = (JComboBox)event.getSource();

                Object selected = comboBox.getSelectedItem();
				
				boardPanel.removeAll();
				
				if(selected.toString().equals("2"))
				{
					boardButtons = new JButton[2][2];
					boardPanel.setLayout(new GridLayout(2, 2));
				} 
                else if(selected.toString().equals("4"))
				{
					boardButtons = new JButton[4][4];
					boardPanel.setLayout(new GridLayout(4, 4));
				}
                else if(selected.toString().equals("5"))
				{
                    boardButtons = new JButton[5][5];
					boardPanel.setLayout(new GridLayout(5, 5));
				}
				else
				{
					boardButtons = new JButton[10][10];
					boardPanel.setLayout(new GridLayout(10, 10));
				}	
				
				for (int r = 0; r < boardButtons.length; ++r)
				{
					for (int c = 0; c < boardButtons.length; ++c)
					{	
						boardButtons[r][c] = new JButton();
						boardButtons[r][c].setText(" ");
						boardPanel.add( boardButtons[r][c] );
					}
				}
				
				boardPanel.revalidate();
				boardPanel.repaint();
			}
		});

		content.add(leftPanel);
		content.add(boardPanel);
		content.add(rightPanel);
		
		window.setSize(680,480);
		window.setVisible(true);
	}
	
	class ButtonListener implements ActionListener
	{
		TreeSet<String> dictionary; 
		
		public void actionPerformed(ActionEvent e)
		{
			Component whichButton = (Component)e.getSource();
			
			if(whichButton == go)
			{
				String filename = enterFilename.getText();
				String[][] board = null;

				try
				{
					board = loadBoard(filename);
				}
				catch (IOException e1)
				{
					System.out.println(e1);
				}
				
				if(board != null)
					for (int r = 0; r < boardButtons.length; ++r)
						for (int c = 0; c < boardButtons.length; ++c)
							boardButtons[r][c].setText(board[r][c]);
				
				boardPanel.revalidate();
				boardPanel.repaint();
				
				try 
				{
					dictionary = loadDictionary();
				} 
				catch (IOException e2) 
				{
					System.out.println(e2);
				}
				
				for(int r = 0; r < board.length; r++)
				{
					for(int c = 0; c < board[r].length; c++)
					{
						try
						{
							DFS(board, r, c, ""); 
						} 
						catch (IOException e3)
						{
							System.out.println(e3);
						}
					}
				}					
			
				numWords.setText("Number of Words Counted: " + Long.toString(numWordsFormed));
				
				for( String s : wordsResult )
					wordsFound.append(s);
			}
		}
		
		String[][] loadBoard(String filename) throws IOException
		{
			File file = new File(filename);
			Scanner scr = new Scanner(file);
			
			String[][] board = new String[boardButtons.length][boardButtons.length];
				
			if(scr.hasNext())
			{
				scr.next();
				
				for (int r = 0; r < board.length; r++)
				{
					for (int c = 0; c < board.length; c++)
					{
						String temp = scr.next();
						if (temp.equals("q"))
							board[r][c] = temp + scr.next();
						else
							board[r][c] = temp;
					}
				
				}					
			}
			scr.close();

			
			return board;
		}
		
		void DFS( String[][] board, int r, int c, String word ) throws IOException
		{
			word += board[r][c];
			
			if(!(dictionary.ceiling(word)).contains(word))
				return;
			
			if(dictionary.contains(word) && word.length() > 2 && wordsResult.add(word + "\n"))
				++numWordsFormed;
			
			if ( r-1 >= 0 && r-1 <= board.length - 1 && c >= 0 && c <= board.length - 1 && board[r-1][c] != null ) //NORTH
			{
				String unmarked = board[r][c];
				board[r][c] = null;
				DFS(board, r-1, c, word);
				board[r][c] = unmarked;
			}
			if ( r-1 >= 0 && r-1 <= board.length - 1 && c+1 >= 0 && c+1 <= board.length - 1 && board[r-1][c+1] != null ) //NORTHEAST
			{
				String unmarked = board[r][c];
				board[r][c] = null;
				DFS(board, r-1, c+1, word);
				board[r][c] = unmarked;
			}
			if ( r >= 0 && r <= board.length - 1 && c+1 >= 0 && c+1 <= board.length - 1 && board[r][c+1] != null ) //EAST
			{
				String unmarked = board[r][c];
				board[r][c] = null;
				DFS(board, r, c+1, word);
				board[r][c] = unmarked;
			}
			if ( r+1 >= 0 && r+1 <= board.length - 1 && c+1 >= 0 && c+1 <= board.length - 1 && board[r+1][c+1] != null ) //SOUTHEAST
			{
				String unmarked = board[r][c];
				board[r][c] = null;
				DFS(board, r+1, c+1, word);
				board[r][c] = unmarked;
			}
			if ( r+1 >= 0 && r+1 <= board.length - 1 && c >= 0 && c <= board.length - 1 && board[r+1][c] != null ) //SOUTH
			{
				String unmarked = board[r][c];
				board[r][c] = null;
				DFS(board, r+1, c, word);
				board[r][c] = unmarked;
			}
			if ( r+1 >= 0 && r+1 <= board.length - 1 && c-1 >= 0 && c-1 <= board.length - 1 && board[r+1][c-1] != null ) //SOUTHWEST
			{
				String unmarked = board[r][c];
				board[r][c] = null;
				DFS(board, r+1, c-1, word);
				board[r][c] = unmarked;
			}
			if ( r >= 0 && r <= board.length - 1 && c-1 >= 0 && c-1 <= board.length - 1 && board[r][c-1] != null ) //WEST
			{
				String unmarked = board[r][c];
				board[r][c] = null;
				DFS(board, r, c-1, word);
				board[r][c] = unmarked;
			}
			if ( r-1 >= 0 && r-1 <= board.length - 1 && c-1 >= 0 && c-1 <= board.length - 1 && board[r-1][c-1] != null ) //NORTHWEST
			{
				String unmarked = board[r][c];
				board[r][c] = null;
				DFS(board, r-1, c-1, word);
				board[r][c] = unmarked;
			}
		}
		
		TreeSet<String> loadDictionary() throws IOException
		{
			Scanner scr = new Scanner( new File("dictionary.txt") );
			dictionary = new TreeSet<String>();
			
			while(scr.hasNext())
				dictionary.add(scr.nextLine());
			scr.close();
			
			return dictionary;
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		new BoggleGUI();
	}
}
	