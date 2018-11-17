import java.util.*;
import java.lang.Integer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;
public class keywordcounter {

    // Always has a pointer to the current Maximum Node
    private Node maxNode;
    // contains the no of nodes
    private int size;

    // Initialise empty heap
    public keywordcounter(){
        maxNode=null;
        size=0;
    }

    public void insertNode(Node node){

        //check if heap is empty
        if(size==0){
            maxNode=node;
        }

        else{
            // add new node to the right of MaxNode
            node.prev=maxNode;
            node.next=maxNode.next;
            maxNode.next=node;
            //check if MaxNode has a sibling node and adjust its 'prev' pointer.
            if(node.next!=null){
                node.next.prev=node;
            }
            // make MaxNode the 'next' of new node
            else{
                node.next = maxNode;
                maxNode.prev = node;
            }
            //check and update MaxNode
            setMaxNode(node);

        }
        // increment size after insertion
        size++;
    }

    public void setMaxNode(Node node){
        //check if node is greater than max node and update it to the new max node
        if(maxNode.frequency < node.frequency){
            maxNode=node;
        }
    }

    public void removeNodeFromList(Node node){
        //remove a node and adjust its pointers
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    public void moveNodeToRoot(Node node){
        //add node to root level and adjust the pointers of maxnode
        Node nextNode = maxNode.next;
        maxNode.next=node;
        node.next=nextNode;
        nextNode.prev=node;
        node.prev=maxNode;
    }
    // Insert a new node

    public void increaseFrequency(Node node, int newFrequency){

        int frequency=node.frequency;

        // update the frequency of the current node
        node.frequency=frequency+newFrequency;


        Node parent=node.parent;

        if(parent!=null){

            // If parent's frequency is less than current node, remove current node from tree and move it to root level.
            if(parent.frequency<node.frequency){
                removeNodeFromList(node);

                //update the parents degree
                parent.degree--;
                //if current node had siblings then update parents child pointer to the next sibling
                if(parent.child==node)parent.child=node.next;

                // parents degree drops to zero set child as null
                if(parent.degree==0)parent.child=null;

                //move current node to root
                moveNodeToRoot(node);
                //set parent to null
                node.parent=null;
                //mark the childcut value as false
                node.isMarked=false;
                //call cascading cut on its parent
                cascadingCut(parent);
            }
        }
        //check and update MaxNode
        setMaxNode(node);
    }

    public void cascadingCut(Node node){
        // find the parent of current node
        Node parent=node.parent;

        //check if parent exists
        if(parent != null) {
            // if parent is losing child for first time then stop the cascade and change the childcut value to true
            if(!node.isMarked){
                node.isMarked = true;
            }
            else
            //remove the node and move it to root.
            {
                removeNodeFromList(node);
                //update parent degree
                parent.degree--;
                //if current node had siblings then update parents child pointer to the next sibling
                if(parent.child == node) parent.child = node.next;
                // parents degree drops to zero set child as null
                if(parent.degree == 0) parent.child = null;

                //move the current node to root
                moveNodeToRoot(node);
                //set parent to null
                node.parent = null;
                //mark the childcut value as false
                node.isMarked = false;
                // call cascading cut for parent and keep cascading up till a childcut value is false.
                cascadingCut(parent);
            }
        }
    }

    public Node extractMaxNode(){
        //store the current maxNode
        Node currentMax=maxNode;

        if(currentMax!= null){
            //access child of maxnode
            Node child=currentMax.child;
            Node nextChild;

            //cycle through all the children of the maxnode and move them to root updating their pointers
            for (int i=0;i<currentMax.degree;i++){
                nextChild=child.next;
                removeNodeFromList(child);
                moveNodeToRoot(child);
                child.parent=null;
                child.isMarked=false;
                child=nextChild;
            }

            //remove current maxNode from the heap and clean up its pointers
            removeNodeFromList(currentMax);

            //if currentmax was the only node then set maxNode to null
            if(currentMax==currentMax.next){
                maxNode=null;

            }
            //set the nextnode as max and start combining
            else{
                maxNode=currentMax.next;
                combinePairs();

            }
            //decrease the size of the heap
            size--;
            //return the extracted maxNode
            return currentMax;

        }
        //if maxNode was null return null
        return null;
    }

    public void combinePairs(){


        //store the current maxNode
        Node currentMax=maxNode;
        int treeCount=0;
        if(currentMax!=null){
            do{
                treeCount++;
                currentMax = currentMax.next;
            }while(currentMax != maxNode);
        }

        int len=1000;
        //define a list for the degree table to store the new trees
        List<Node> degreeList=new ArrayList<Node>(len);
        for (int i=0;i<len;i++){
            degreeList.add(null);
        }





        //going through every tree
        for (int i=0; i<treeCount;i++){
            //get the nextNode
            Node nextNode=currentMax.next;
            // get maxnode degree
            int degree=currentMax.degree;

            for(;;){
                //if there is a degree match then merge two trees.
                Node temp=degreeList.get(degree);
                if(temp==null){
                    break;
                }
                //find smaller node
                if(currentMax.frequency<temp.frequency){
                    Node swap=temp;
                    temp=currentMax;
                    currentMax=swap;

                }
                removeNodeFromList(temp);
                // set smaller node as child
                temp.parent=currentMax;
                // set new node as child of currentmax and connect its siblings
                if(currentMax.child!=null){
                    temp.prev=currentMax.child;
                    temp.next=currentMax.child.next;
                    currentMax.child.next=temp;
                    temp.next.prev=temp;
                }else{
                    currentMax.child=temp;
                    temp.next=temp;
                    temp.prev=temp;
                }
                //update degree
                currentMax.degree++;
                temp.isMarked=false;
                //once merged remove the entry from the list
                degreeList.set(degree,null);
                degree++;
            }
            //store new tree in list
            degreeList.set(degree,currentMax);
            currentMax=nextNode;

        }
        maxNode=null;
        //add each max tree in degree table to root level
        for(Node node:degreeList){
            if(node==null){
                continue;
            }
            if(maxNode!=null){
                removeNodeFromList(node);
                moveNodeToRoot(node);
                setMaxNode(node);
            }
            else{
                maxNode=node;
            }
        }
    }

    public static class Node{
        String value;
        int frequency;
        int degree;
        Node parent;
        Node child;
        Node prev;
        Node next;
        boolean isMarked;

        public Node(String keyword, int frequency){
            this.value=keyword;
            this.frequency=frequency;
            this.degree=0;
            this.parent=null;
            this.child=null;
            this.next=this;
            this.prev=this;
            this.isMarked=false;
        }

    }

    public static void main(String[] args){
        long startTime = System.currentTimeMillis();

        String currentLine;
        String inputFile= args[0];
        Map<String, Node> hashtable= new HashMap<String, Node>();
        keywordcounter fHeap = new keywordcounter();

        //regex patterns for keyword and frequency
        String inputkeywordPattern = "((\\$)(.+)(\\s)(\\d+))";
        String frequencyPattern = "(\\d+)";

        //compile the regex patterns
        Pattern pattern1 = Pattern.compile(inputkeywordPattern);
        Pattern pattern2 = Pattern.compile(frequencyPattern);

        PrintWriter outputWriter = null;

        try {
            File f=new File(inputFile);
            FileReader fr=new FileReader(f);
            BufferedReader br=new BufferedReader(fr);
            outputWriter=new PrintWriter("output_file.txt", "UTF-8");

            //read each line of file and look for matches
            while (((currentLine = br.readLine()).compareToIgnoreCase("stop"))!=0){
                Matcher matcher1=pattern1.matcher(currentLine);
                Matcher matcher2=pattern2.matcher(currentLine);

                //if keyword match then update the hashtable with a new entry
                if(matcher1.find()){
                    String value = matcher1.group(3);
                    int key = Integer.parseInt(matcher1.group(5));
                    //insert a new node
                    if(!hashtable.containsKey(value)){
                        Node n=new Node(value,key);
                        fHeap.insertNode(n);
                        hashtable.put(value,n);
                    }
                    // or update an existing one
                    else{
                        Node n= hashtable.get(value);
                        fHeap.increaseFrequency(n,key);
                    }
                }

                // extract the max node according to the query number
                else if(!matcher1.find() && matcher2.find()) {

                    int count = Integer.parseInt(matcher2.group(1));
                    // queue to store the max nodes extracted
                    Queue<Node> list=new LinkedList<Node>();

                    for (int i=0;i<count;i++) {
                        // if query number exceeds the number of existing nodes then stop executing the loop
                        if(fHeap.size==0)continue;
                            Node max = fHeap.extractMaxNode();
                            hashtable.remove(max.value);

                            //list to store the nodes to be reinserted back into heap
                            Node nodes = new Node(max.value, max.frequency);
                            list.add(nodes);

                            if (i == count - 1) {
                                outputWriter.print(nodes.value);

                            } else {
                                outputWriter.print(nodes.value+ ", ");
                            }
                        }
                    // reinsent back into the heap
                    outputWriter.println();
                    while(list.peek()!=null){
                        Node n=list.poll();
                        fHeap.insertNode(n);
                        hashtable.put(n.value,n);


                    }
                }

            }

        }
        catch(Exception E) {
            E.printStackTrace();
        }
        finally {
            if(outputWriter!=null){
                outputWriter.close();
            }
            long stopTime = System.currentTimeMillis();
            //prints time taken to run the program
            //System.out.println(stopTime - startTime+" ms");
        }
    }

}
