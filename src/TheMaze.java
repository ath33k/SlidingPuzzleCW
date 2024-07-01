import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Atheek Mohomed Naheem
 * IIT ID : 20222361
 * UOW ID: 19900867
 */
public class TheMaze {
     private static class Node{
        private final int y;
        private final int x;
        private String movingDirection;

        public Node(int y, int x, String movingDirection) {
            this.y = y;
            this.x = x;
            this.movingDirection = movingDirection;
        }

        @Override
        public String toString() {
            return  movingDirection + " (" + (x + 1)  + " , " + (y + 1) +")";
        }

    }

    private class NodeEntry{
         private final Node node;
         private int priority;

        public NodeEntry(Node node, int priority) {
            this.node = node;
            this.priority = priority;
        }
    }

    // Maze class fields
    int[] startNodeCoords = new int[2];
    int[] endNodeCoords = new int[2];
    String[][] theGrid;
    private int rows = 0;
    private int columns = 0;

    /**
     * It generates a grid by calling two functions inside here
     * calculateSize function to get rows and columns size
     * createGrid function  to create the 2d array
     * @param filePath filepath of the text file
     */
    public void generateGridFromFile(String filePath){
        int[] size = calculateSize(filePath);
        rows = size[0];
        columns = size[1];
        theGrid = new String[rows][columns];
        createGrid(filePath, theGrid);

//        System.out.println("rows: " + rows + " col: " + columns);
//        return new TheGrid(theGrid,rows,columns,startNodeCoords,endNodeCoords);
    }

    private void createGrid(String filePath, String[][] grid){
        int row = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null){
                int charNum = 0;
                for (var ch: line.toCharArray()){

                    grid[row][charNum] = String.valueOf(ch);
                    if (ch == 'S') {
                        startNodeCoords[0] = row;
                        startNodeCoords[1] = charNum ;
                    } else if (ch == 'F') {
                        endNodeCoords[0] = row;
                        endNodeCoords[1] = charNum;
                    }
                    charNum++;
                }
                row++;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private int[] calculateSize(String filePath){
        int col = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null){
                if (line.isEmpty())
                    continue;
                int charNum = 0;
                for (var ch: line.toCharArray()){
                    charNum++;
                }
                col = charNum;
                rows++;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new int[]{rows,col};
    }

    public void shortestPath(){
        if (theGrid == null)
            throw new IllegalStateException();

        int startY = startNodeCoords[0];
        int startX = startNodeCoords[1];
        int endY =  endNodeCoords[0];
        int endX = endNodeCoords[1];

        int visitCount= 0;

        boolean[][] visited = new boolean[rows][columns];
        Map<Node, Node> previousNode = new HashMap<>();

        PriorityQueue<NodeEntry> queue = new PriorityQueue<>(Comparator.comparingInt(ne -> ne.priority));

        int[][] distances = new int[rows][columns];
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < columns; j++){
                distances[i][j] = Integer.MAX_VALUE;
            }
        }
        distances[startY][startX] = 0;

        var startNode = new Node(startY, startX, "Start at");
        queue.add(new NodeEntry(startNode, 0));

        int[][] directions = {
                {0,-1}, //left
                {-1,0},//top
                {1,0}, //bottom
                {0,1}, //right
        };

        while (!queue.isEmpty()){
            var current = queue.remove().node;
            visitCount++;
            visited[current.y][current.x] = true;

            if (current.y == endY && current.x == endX){
//                System.out.println("END NODE FOUNDED : " + current);
//                System.out.println("-------------------------------");
                backTrack(previousNode,current,theGrid,visitCount);
//                System.out.println("visit count : " + visitCount);
                return;
            }

            //All four direction left, top, right, bottom
            for (var dir: directions){  // e.g "left"
                int nextY = dir[0] + current.y;
                int nextX = dir[1] + current.x;
                // for each next direction it moves same direction
                // until it hits a wall or rock
                while (isValidMove(nextY, nextX)){ // check left cell of current is valid

                    // check second left cell of current is valid
                    if (isNextMoveOutOfRange(nextY,nextX,dir) ||
                            isNextMoveRock(nextY, nextX, dir) ||
                            endY == nextY && endX == nextX)
                    {
                        if (!visited[nextY][nextX]){
                            var toNode = new Node(nextY, nextX, movingDirection(dir[0], dir[1]));
                            var gScore = getGScore(startNode, toNode);
                            var hScore = getHScore(toNode, endY, endX);
                            var fnScore = getFScore(gScore,hScore);

                            if (fnScore < distances[nextY][nextX]){
                                visited[nextY][nextX] = true;
                                distances[nextY][nextX] = fnScore;
                                previousNode.put(toNode, current);
                                queue.add(new NodeEntry(toNode, fnScore));
                            }
                        }
                    }
                    nextY += dir[0];
                    nextX += dir[1];
                }
            }
        }
    }

    /** It checks whether the move is out of range and a rock*/
    private boolean isValidMove(int y, int x) {
        if (y < 0 || y >= rows || x < 0 || x >= columns) {
            return false; // Out of bounds
        }
        if (theGrid[y][x].equals("0")) {
            return false; // Obstacle
        }
        return true;
    }


    /** get the particular direction when coordinates has been given */
    private String movingDirection(int dirY, int dirX){
        //{0,-1}, //left
        //{-1,0},//top
        //{0,1}, //right
        //{1,0}, //bottom
        if (dirX == -1)
            return "Move left to";
        else if (dirY == -1)
            return "Move up to";
        else if (dirX == 1)
            return "Move right to";
        else
            return "Move down to";

    }

    /** check whether next move is out of range (going outside the grid range)*/
    private boolean isNextMoveOutOfRange(int nextY, int nextX, int[] dir ){
        return ((nextY + dir[0] < 0 || nextY + dir[0] >= rows) || (nextX + dir[1] < 0 || nextX + dir[1] >= columns));
    }

    /** check whether the next move is hits a rock */
    private boolean isNextMoveRock(int nextY, int nextX, int[] dir ){
        return theGrid[nextY + dir[0]][nextX + dir[1]].equals("0");
    }

    /**
     * G-Score it returns the actual cost to reach the current node from the starting node
     * @param current is the starting node
     * @param toNode is the current node
     */
    private int getGScore(Node current, Node toNode){
        return Math.abs((current.y - toNode.y)) + Math.abs((current.x - toNode.x));
    }

    /**
     * H-Score it returns the heuristic estimate of the cost from the current node to the end node (goal node)
     * @param toNode is the current node, endY endX represents the goal node (end Node)
     */
    private int getHScore(Node toNode, int endY, int endX){
        return Math.abs((toNode.y - endY )) + Math.abs((toNode.x + endX));
    }


    /** sum of G-Score and H-Score, representing the total estimated cost of reaching the goal node through the current node */
    private int getFScore(int gScore, int hScore){
        return gScore + hScore;
    }


    /** which stores the parent node of the visited node until it reaches the goal node.
     * It starts at the goal node and follows the parent pointer through the graph.*/
    private void backTrack(Map<Node, Node> previousNode, Node endNode, String[][] theGrid, int visitCount){
        Stack<Node> stack = new Stack<>();
        stack.push(endNode);

        var previous = previousNode.get(endNode);

        while (previous != null){
            stack.push(previous);
            previous = previousNode.get(previous);
        }

        int steps = stack.size() - 1;
        while (!stack.isEmpty()){
            System.out.println(stack.pop());
        }
        System.out.println("Done!");
        System.out.println("STEPS : " + steps);
        System.out.println("VISIT COUNT : " + visitCount);
        System.out.println("=====================================");
    }

    /** Displays the maze*/
    public void print(String[][] theGrid){
        for (var x: theGrid){
            System.out.println(Arrays.toString(x));
        }
        System.out.println("======================================");
    }

}
