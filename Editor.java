import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.border.Border;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.plaf.metal.*;

class Test extends JFrame implements ActionListener {
    final JFrame frame = new JFrame();
    private JTextArea textarea;    
    public class SuggestionPanel {
        private JList list; //suggestion list - to store values
        private JPopupMenu popupMenu;   
        private String subWord;
        private final int insertionPosition;

        public SuggestionPanel(JTextArea textarea, int position, String subWord, Point location) {
            this.insertionPosition = position;
            this.subWord = subWord;
            popupMenu = new JPopupMenu();
            popupMenu.removeAll();
            popupMenu.setOpaque(false);
            Border blackline = BorderFactory.createLineBorder(Color.black,0);
            popupMenu.setBorder(blackline);
            popupMenu.add(list = createSuggestionList(position, subWord));
            popupMenu.show(textarea, location.x, textarea.getBaseline(0, 0) + location.y);
        }

        public void hide() {
            popupMenu.setVisible(false);
            if (suggestion == this) {
                suggestion = null;
            }
        }

        private JList createSuggestionList(final int position, final String subWord) {
            Object[] data = new Object[10];
            ArrayList<String> l=new ArrayList<String>();
            //Reading keywords file and storing it into ArrayList
            try{
                FileReader keywrds=new FileReader("C:\\Users\\fnaus\\OneDrive\\Desktop\\CP\\java\\keywords.txt");
                int temp=keywrds.read();
                String word="";
                while(temp!=-1){
                    word+=(char)(temp);
                    temp=keywrds.read();
                }
                word=word.trim();
                String[] words=word.split("\n");
                for(int j=0;j<words.length;j++)
                    l.add(words[j]);

            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
            int i=0;
            for(String j:l){
                if(j.startsWith(subWord)){
                    data[i++]=j;
                }
            }
            
            JList list = new JList(data);
            list.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setSelectedIndex(0);
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        insertSelection();
                    }
                }
            });
            return list;
        }

        public boolean insertSelection() {
            if (list.getSelectedValue() != null) {
                try {
                    final String selectedSuggestion = ((String) list.getSelectedValue()).substring(subWord.length());
                    textarea.getDocument().insertString(insertionPosition, selectedSuggestion, null);
                    return true;
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                hideSuggestion();
            }
            return false;
        }

        public void moveUp() {
            int index = Math.min(list.getSelectedIndex() - 1, 0);
            selectIndex(index);
        }

        public void moveDown() {
            int index = Math.min(list.getSelectedIndex() + 1, list.getModel().getSize() - 1);
            selectIndex(index);
        }

        private void selectIndex(int index) {
            final int position = textarea.getCaretPosition();
            list.setSelectedIndex(index);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    textarea.setCaretPosition(position);
                };
            });
        }
    }

    private SuggestionPanel suggestion;

    protected void showSuggestionLater() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showSuggestion();
            }

        });
    }

    protected void showSuggestion() {
        hideSuggestion();
        final int position = textarea.getCaretPosition();
        Point location;
        try {
            location = textarea.modelToView(position).getLocation();
        } catch (BadLocationException e2) {
            e2.printStackTrace();
            return;
        }
        String text = textarea.getText();
        int start = Math.max(0, position - 1);
        while (start > 0) {
            if (!Character.isWhitespace(text.charAt(start))) {
                start--;
            } else {
                start++;
                break;
            }
        }
        if (start > position) {
            return;
        }
        final String subWord = text.substring(start, position);
        if (subWord.length() < 2) {
            return;
        }
        suggestion = new SuggestionPanel(textarea, position, subWord, location);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textarea.requestFocusInWindow();
            }
        });
    }

    private void hideSuggestion() {
        if (suggestion != null) {
            suggestion.hide();
        }
    }

    protected void initUI()  {
        //User Interface
        frame.setTitle("AND EDITOR");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout());
        textarea = new JTextArea(80, 80);
        Font font = new Font("Ariel", Font.PLAIN, 18);
        textarea.setFont(font);
        textarea.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 0));
        //
        textarea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    if (suggestion != null) {
                        if (suggestion.insertSelection()) {
                            e.consume();
                            final int position = textarea.getCaretPosition();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        textarea.getDocument().remove(position - 1, 1);
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && suggestion != null) {
                    suggestion.moveDown();
                } else if (e.getKeyCode() == KeyEvent.VK_UP && suggestion != null) {
                    suggestion.moveUp();
                } else if (Character.isLetterOrDigit(e.getKeyChar())) {
                    showSuggestionLater();
                } else if (Character.isWhitespace(e.getKeyChar())) {
                    hideSuggestion();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }
        });
        panel.add(textarea, BorderLayout.CENTER);
        frame.add(panel);
		JMenuBar mb = new JMenuBar();

		JMenu m1 = new JMenu("File");
        
		
		JMenuItem mi1 = new JMenuItem("New");
		JMenuItem mi2 = new JMenuItem("Open");
		JMenuItem mi3 = new JMenuItem("Save");
		JMenuItem mi9 = new JMenuItem("Print");
        
		mi1.addActionListener(this);
		mi2.addActionListener(this);
		mi3.addActionListener(this);
		mi9.addActionListener(this);

		m1.add(mi1);
		m1.add(mi2);
		m1.add(mi3);
		m1.add(mi9);

		JMenu m2 = new JMenu("Edit");
        
		JMenuItem mi4 = new JMenuItem("Cut");
		JMenuItem mi5 = new JMenuItem("Copy");
		JMenuItem mi6 = new JMenuItem("Paste");

		mi4.addActionListener(this);
		mi5.addActionListener(this);
		mi6.addActionListener(this);

		m2.add(mi4);
		m2.add(mi5);
		m2.add(mi6);
        
		JMenuItem mc = new JMenuItem("Close");
        
		mc.addActionListener(this);

		mb.add(m1);
		mb.add(m2);
		mb.add(mc);

		frame.setJMenuBar(mb);
		frame.add(textarea);
		frame.setSize(500, 500);
		//frame.show(); - coz we use setvisible(true) coz we use setSize()
        frame.pack(); 
        //frame.setLayout(null); 
        frame.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();

		if (s.equals("Cut")) {
			textarea.cut();
		}
		else if (s.equals("Copy")) {
			textarea.copy();
		}
		else if (s.equals("Paste")) {
			textarea.paste();
		}
		else if (s.equals("Save")) {
			
			JFileChooser j = new JFileChooser("f:");

			int r = j.showSaveDialog(null);

			if (r == JFileChooser.APPROVE_OPTION) {

				File fi = new File(j.getSelectedFile().getAbsolutePath());

				try {
					
					FileWriter wr = new FileWriter(fi, false);
					BufferedWriter w = new BufferedWriter(wr);
					w.write(textarea.getText());
					w.flush();
					w.close();
				}
				catch (Exception evt) {
					JOptionPane.showMessageDialog(frame, evt.getMessage());
				}
			}
			else
				JOptionPane.showMessageDialog(frame, "OPERATION TERMINATED!!");
		}
		else if (s.equals("Print")) {
			try {
				textarea.print();
			}
			catch (Exception evt) {
				JOptionPane.showMessageDialog(frame, evt.getMessage());
			}
		}
		else if (s.equals("Open")) {
			JFileChooser j = new JFileChooser("f:");
			int r = j.showOpenDialog(null);
			if (r == JFileChooser.APPROVE_OPTION) {
				File fi = new File(j.getSelectedFile().getAbsolutePath());

				try {
					
					String s1 = "", sl = "";
                    FileReader fr = new FileReader(fi);
					BufferedReader br = new BufferedReader(fr);
					sl = br.readLine();
					while ((s1 = br.readLine()) != null) {
						sl = sl + "\n" + s1;
					}
					textarea.setText(sl);
				}
				catch (Exception evt) {
					JOptionPane.showMessageDialog(frame, evt.getMessage());
				}
			}
			else
				JOptionPane.showMessageDialog(frame, "OPERATION TERMINATED!!");
		}
		else if (s.equals("New")) {
			textarea.setText("");
		}
		else if (s.equals("Close")) {
			frame.setVisible(false);
		}
	}
}

public class Editor{
    public static void main(String[] args) {
        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

			MetalLookAndFeel.setCurrentTheme(new OceanTheme());

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() 
        {
            @Override
            public void run() {
                new Test().initUI();
            }
        });
    }

}
