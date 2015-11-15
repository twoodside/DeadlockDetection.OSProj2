package deadlockdetection_osproj2;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeSet;

public class DeadlockDetectionOSProj2 {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int numProcesses=in.nextInt();
        int numResources=in.nextInt();
        int simulation = 0;
        
        while (numProcesses!=0 && numResources!=0){
            System.out.printf("Simulation %d\n",++simulation);
            
            LinkedList<Instruction>[] instructionList = new LinkedList[numProcesses];
            
            loadProcessInstructions(instructionList,in);
            
            //printProcessContents(instructionList);
            
            simulate(numResources,instructionList);
            
            System.out.println();
            
            numProcesses=in.nextInt();
            numResources=in.nextInt();
        }
    }
    
    private static void simulate(int numResources, LinkedList<Instruction>[] instructionList) {
        int executionCycles=0;
        boolean deadlocked=false;
        //Determines if a resource is held by any process
        boolean[] held = new boolean[numResources];
        
        //Determines requests/ownership of resources by processes. If requests[p][r] is true, process p either requests or owns resource r
        boolean[][] requests = new boolean[instructionList.length][numResources];
        
        //Block state of each process
        boolean[] blocked = new boolean[instructionList.length];
        
        //Print queue
        PrintWriter processFinishedQueue = new PrintWriter(System.out);
        
        //The runtime for each process
        int[] runtime = new int[instructionList.length];
        for (int i=0;i<runtime.length;i++){
            runtime[i]=1;
        }
        
        //Queues for each resource when a process has to block
        LinkedList<Integer>[] requestingAccess = new LinkedList[numResources];
        for (int i=0;i<requestingAccess.length;i++){
            requestingAccess[i] = new LinkedList();
        }
        
        //Which process is to run next
        LinkedList<Integer> readyQueue = new LinkedList();
        for (int i=0;i<instructionList.length;i++){
            readyQueue.add(i);
        }
        
        while (!deadlocked && !(readyQueue.isEmpty() && !allBlocked(blocked) )){
            int processNumber=readyQueue.pop();
            
            if (blocked[processNumber]==true || instructionList[processNumber].peek()==null){
                continue;
            }
            
            Instruction currInst = instructionList[processNumber].pop();

            if (currInst.type==1){
                if (!requestResource(currInst.value,processNumber,blocked,held,requests,requestingAccess)){
                    instructionList[processNumber].addFirst(currInst);
                }
                LinkedList<Edge> results = hasCycle(requests, createVisitedMatrix(requests),processNumber, currInst.value, true);

                if  (results!=null){
                    System.out.printf("Deadlock detected at time %d involving...\n",executionCycles);
                    printEdges(results);
                    deadlocked=true;
                }
            }
            else if (currInst.type==2){
                releaseResource(currInst.value,processNumber,blocked,held,requests,requestingAccess,readyQueue);
            }

            if (instructionList[processNumber].isEmpty()){
                processFinishedQueue.write(
                        String.format("Process %d: run time = %d, ended at %d\n",processNumber+1,runtime[processNumber],executionCycles)
                        );
                executionCycles++;
            }
            else if (!blocked[processNumber]){
                readyQueue.add(processNumber);
                executionCycles++;
                runtime[processNumber]++;
            }
        }
        if (!deadlocked){
            System.out.printf("All processes successfully terminated.\n");
            processFinishedQueue.flush();
        }
    }
    
    private static void printProcessContents(LinkedList<Instruction>[] instructionList){
        for (int i=0;i<instructionList.length;i++){
            System.out.printf("Process %d:\n",i);
            while (instructionList[i].peek()!=null){
                Instruction inst = instructionList[i].pop();
                System.out.printf(
                        "(%d,%d)\n",
                        inst.type,
                        inst.value
                );
            }
        }
    }
    
    private static void loadProcessInstructions(LinkedList<Instruction>[] instructionList,Scanner in) {
        for (int i=0;i<instructionList.length;i++){
            instructionList[i]=new LinkedList();
            int instCount = in.nextInt();
            for (int j=0;j<instCount;j++){
                try{
                    int instType = in.nextInt();
                    int instValue = in.nextInt();
                    
                    while (instType==3 && instValue>1){
                        Instruction temp = new Instruction(3,1);
                        instructionList[i].add(temp);
                        instValue--;
                    }
                    
                    Instruction temp = new Instruction(instType,instValue);
                    instructionList[i].add( temp );
                }
                catch (Exception e){
                    System.err.println("Instruction number must be between 1 and 3.");
                    System.err.println("Instruction argument must be greater than 0.");
                }
            }
        }
    }

    private static boolean processesFinished(LinkedList<Instruction>[] instructionList) {
        boolean r=true;
        for (int i=0;i<instructionList.length;i++){
            if (instructionList[i].peek()!=null){
                r=false;
                break;
            }
        }
        return r;
    }

    private static boolean checkForDeadlock(boolean[][] requests) {
        for (int p=0;p<requests.length;p++){
            for (int r=0;r<requests.length;r++){
               hasCycle(requests,createVisitedMatrix(requests),p,r,true);
            }
        }
        
        return false;
    }
    
    private static boolean[][] createVisitedMatrix(boolean[][] requests){
        boolean[][] visited = new boolean[requests.length][requests[0].length];
        
        for (int p=0;p<requests.length;p++){
            for (int r=0;r<requests[p].length;r++){
                visited[p][r]=false;
            }
        }
        
        return visited;
    }
    
    private static LinkedList<Edge> hasCycle(boolean[][] requests, boolean[][] visited, int p, int r,boolean checkingResources) {
        
        if (visited[p][r]==true){
            LinkedList<Edge> ret = new LinkedList();
            ret.add(new Edge(p,r));
            return ret;
        }
        
        visited[p][r]=true;
        
        LinkedList<Edge> ret=null;
        
        if (!checkingResources){
            
            for (int i=0;i<requests[p].length;i++){
                if (i==r)
                    continue;
                
                
                if (requests[p][i]==true){
                    LinkedList<Edge> subCycleContents=hasCycle(requests,visited,p,i,!checkingResources);
                    if (subCycleContents!=null){
                        ret=subCycleContents;
                        ret.add(new Edge(p,r));
                        break;
                    }
                }
            }
        }
        else{
            for (int i=0;i<requests.length;i++){
                if (i==p)
                    continue;
                
                
                if (requests[i][r]==true){
                    LinkedList<Edge> subCycleContents=hasCycle(requests,visited,i,r,!checkingResources);
                    if (subCycleContents!=null){
                        ret=subCycleContents;
                        ret.add(new Edge(p,r));
                        break;
                    }
                }
            }
        }
        
        return ret;
    }

    /*
     * @return True if release left resource free, false if resource was obtained by process from the queue
     */
    private static boolean releaseResource(int resourceNumber, int processNumber, boolean[] blocked, boolean[] held, boolean[][] requests, LinkedList<Integer>[] requestingAccess,LinkedList<Integer> readyQueue) {
        boolean r = true;
        
        requests[processNumber][resourceNumber]=false;
        held[resourceNumber]=false;
        
        try{
            Integer requestingProcess = requestingAccess[resourceNumber].pop();
            if (requestingProcess!=null){
                blocked[requestingProcess]=false;
                readyQueue.add(requestingProcess);
                r=false;
            }
        }
        catch (NoSuchElementException e){
            //No processes requesting the resource
            r=true;
        }
        
        return r;
    }
    
    /**
     * @return True if request successful, false if resource had to block
     */
    private static boolean requestResource(int resource, int process, boolean[] blocked, boolean[] held, boolean[][] requests, LinkedList<Integer>[] requestingAccess){
        boolean r=true;
        
        requests[process][resource]=true;
        if (held[resource]){
            requestingAccess[resource].add(process);
            r=false;
            blocked[process]=true;
        }
        else{
            held[resource]=true;
        }
        return r;
    }

    private static void printEdges(LinkedList<Edge> edgeList) {
    /*    System.out.println("\tProcess/Resource pairs involved:");
        while (!edgeList.isEmpty()){
            System.out.printf("\t%s\n",edgeList.pop());
        }
        */
        
       TreeSet<Integer> processList = new TreeSet<>();
       TreeSet<Integer> resourceList = new TreeSet<>();
       
       while (!edgeList.isEmpty()){
           Edge item = edgeList.pop();
           processList.add(item.process+1);
           resourceList.add(item.resource+1);
       }
       
       System.out.print("\tprocesses:");
       for (Integer i : processList.toArray(new Integer[processList.size()])){
           System.out.printf(" %s",i);
       }
       System.out.println();
       
       System.out.print("\tresources:");
       for (Integer i : resourceList.toArray(new Integer[resourceList.size()])){
           System.out.printf(" %s",i);
       }
       System.out.println();
    }

    private static boolean allBlocked(boolean[] blocked) {
        boolean r=true;
        for (int i=0;i<blocked.length;i++){
            if (!blocked[i]){
                r=false;
                break;
            }
        }
        return r;
    }
}


