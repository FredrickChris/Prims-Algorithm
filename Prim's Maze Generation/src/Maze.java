import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.util.ArrayList;

public class Maze extends JComponent {
	private Random rand = new Random();
	
	//Maze Properties
	private int tileSize = 20;
	private int colNum = 1530/tileSize; //must be odd
	private int rowNum = 770/tileSize; // must be odd 
	{
		if(colNum%2==0) colNum--;
		if(rowNum%2==0) rowNum--;
	}

	private int screenWidth = colNum*tileSize;
	private int screenHeight = rowNum*tileSize;
	
	private int[][] map = generateMaze(colNum, rowNum);
	
    private ArrayList<int[]> tempWall = convert(map, colNum, rowNum);
    
    private int[][] wall = tempWall.toArray(new int[tempWall.size()][]);

	//================================//
	//            METHODS             //
	//================================//
	
    private int[][] generateMaze(int cols, int rows) {
		//fill the map with walls
		int[][] map = new int[cols][rows];
		for(int c = 0; c < cols; c++) {
			for(int r = 0; r < rows; r++) {
				map[c][r] = 0;
			}
		}
		//create grid intervals
		for(int c = 1; c < cols - 1; c+=2) {
			for(int r = 1; r < rows - 1; r+=2) {
				map[c][r] = 1;
			}
		}

		//choose random path block
		int ic = rand.nextInt((cols-1)/2)*2+1;
		int ir = rand.nextInt((rows-1)/2)*2+1;
		
		ArrayList<int[]> nodes = new ArrayList<>();
		nodes.add(new int[] {ic,ir});
		
		ArrayList<int[]> fronts = new ArrayList<>();
		if(checkFront(ic-2,ir,cols,rows)) fronts.add(new int[] {ic-2,ir});
		if(checkFront(ic+2,ir,cols,rows)) fronts.add(new int[] {ic+2,ir});
		if(checkFront(ic,ir-2,cols,rows)) fronts.add(new int[] {ic,ir-2});
		if(checkFront(ic,ir+2,cols,rows)) fronts.add(new int[] {ic,ir+2});
		
		
		//looping through all nodes to check if all grids had been connected
		int gridNum = ((cols-1)/2)*((rows-1)/2);
		while(nodes.size() < gridNum) {
			//Pick a random coordinate from fronts.
			int frontIndex = rand.nextInt(fronts.size());
			
			//Check if that coordinate is already in nodes (to avoid loops/duplicates).
			if(!checkFront(nodes, fronts.get(frontIndex))) { // true if front is a node, must not be an existing node.
				//Carve the wall between the new node and the existing maze.
				//check all adjacent nodes
				ArrayList<int[]> adjNodes = new ArrayList<>();
				int fc = fronts.get(frontIndex)[0];
				int fr = fronts.get(frontIndex)[1];
				for(int i=0; i<nodes.size(); i++) {
					if(nodes.get(i)[0] == fc-2 && nodes.get(i)[1] == fr) adjNodes.add(new int[] {fc-2,fr});
					if(nodes.get(i)[0] == fc+2 && nodes.get(i)[1] == fr) adjNodes.add(new int[] {fc+2,fr});
					if(nodes.get(i)[0] == fc && nodes.get(i)[1] == fr-2) adjNodes.add(new int[] {fc,fr-2});
					if(nodes.get(i)[0] == fc && nodes.get(i)[1] == fr+2) adjNodes.add(new int[] {fc,fr+2});
				}
				//randomize which node to carve to.
				int nodeIndex = rand.nextInt(adjNodes.size());
				int nc = adjNodes.get(nodeIndex)[0];
				int nr = adjNodes.get(nodeIndex)[1];
				
				//check y and x if +/- 2 and make it 1 and change to path
				map[fc+(nc-fc)/2][fr+(nr-fr)/2] = 1;
				
				
				//Add the new node to the nodes list.
				nodes.add(fronts.get(frontIndex));
				
				//update fronts
				int newc = fronts.get(frontIndex)[0];
				int newr = fronts.get(frontIndex)[1];
				if(checkFront(newc-2,newr,cols,rows)) fronts.add(new int[] {newc-2,newr});
				if(checkFront(newc+2,newr,cols,rows)) fronts.add(new int[] {newc+2,newr});
				if(checkFront(newc,newr-2,cols,rows)) fronts.add(new int[] {newc,newr-2});
				if(checkFront(newc,newr+2,cols,rows)) fronts.add(new int[] {newc,newr+2});
				
				//remove the front
				fronts.remove(frontIndex);
			}
		}
		
		map = braid(map, cols, rows);
		
		return map;
	}
    
    
    private ArrayList<int[]> convert(int[][] map, int cols, int rows) {
		//convert to x,y coordinates
		ArrayList<int[]> wallList = new ArrayList<>();
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++) {
				if (map[c][r] == 0) {
					wallList.add(new int[]{c * tileSize, r * tileSize});
				}
			}
		}
		
		return wallList;
    }
	
	
	private int[][] braid(int[][] map, int cols, int rows) {
		for (int c = 1; c < cols-1; c++) {
			for (int r = 1; r < rows-1; r++) {
				//check for walls, can only break if 2 opposite sides is a path. c+-1 or r+-1
				if (
					map[c][r] == 0 && //is a wall
					(
						(	//opposite rows path, not opposite columns
							map[c-1][r] == 1 && 
							map[c+1][r] == 1 && 
							map[c][r-1] != 1 && 
							map[c][r+1] != 1
						) || 
						(	//opposite columns path, not opposite rows
							map[c-1][r] != 1 && 
							map[c+1][r] != 1 && 
							map[c][r-1] == 1 && 
							map[c][r+1] == 1
						)
					) &&
					rand.nextDouble() < 0.10  //10% chance
					
				) {
					map[c][r] = 1;
				}
			}
		}
		return map;
	}
	
	
	private boolean checkFront(int frontC, int frontR, int cols, int rows) {
		return (
				frontC > 0 &&
				frontC < cols &&
				frontR > 0 &&
				frontR < rows
				);
	}
	
	
	private boolean checkFront(ArrayList<int[]> nodes, int[] front) {  //returns true if front is an existing node
		for(int i=0; i < nodes.size(); i++) {
			if(nodes.get(i)[0] == front[0] && nodes.get(i)[1] == front[1]) {
				return true;
			}
		}
		return false;
	}

	
	public int getCol() {
		return colNum;
	}
	
	public int getRow() {
		return rowNum;
	}
	
	public int[][] getMap() {
		return map;
	}
	
	public int getScreenWidth() {
		return screenWidth;
	}
	
	public int getScreenHeight() {
		return screenHeight;
	}
	
	
	//================================//
	//          PAINT METHOD          //
	//================================//
	public void paintComponent(Graphics g) {
		//Background
		g.setColor(new Color(150,150,150));
		g.fillRect(0, 0, screenWidth, screenHeight);
		
		//Walls
		g.setColor(Color.black);
		for(int i=0; i<wall.length; i++) {
			g.fillRect(wall[i][0], wall[i][1], tileSize, tileSize);
		}
	}
	
	

	//================================//
	//          MAIN METHOD           //
	//================================//
	public static void main(String args[]) {
		JFrame window = new JFrame("CS4 Java Graphics");
		Maze panel = new Maze();
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.add(panel);
		panel.setPreferredSize(new Dimension(panel.screenWidth, panel.screenHeight));
		window.pack();
		window.setVisible(true);
		window.setLocation(-4,0);
	}
}
