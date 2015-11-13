package deadlockdetection_osproj2;

import java.util.LinkedList;
import java.util.Scanner;

public class DeadlockDetectionOSProj2 {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int numProcesses=-1;
        int numResources=-1;
        
        while (numProcesses!=0 && numResources!=0){
            
            numProcesses=in.nextInt();
            numResources=in.nextInt();
            LinkedList<Instruction>[] instructionList = new LinkedList[numProcesses];
            
            loadProcessInstructions(instructionList,in);
            
            //printProcessContents(instructionList);
            
            simulate(numResources,instructionList);
        }
    }
    
    private static void simulate(int numResources, LinkedList<Instruction>[] instructionList) {
        int executionCycles=0;
        //Determines if a resource is held by any process
        boolean[] held = new boolean[numResources];

        //Determines requests/ownership of resources by processes. If requests[p][r] is true, process p either requests or owns resource r
        boolean[][] requests = new boolean[instructionList.length][numResources];

        //Queues for each resource when a process has to block
        LinkedList<Integer>[] requestingAccess = new LinkedList[numResources];
        for (int i=0;i<requestingAccess.length;i++){
            requestingAccess[i] = new LinkedList();
        }
        
        while (!processesFinished(instructionList)){
            for (int processNumber=0;processNumber<instructionList.length;processNumber++){
                if (instructionList[processNumber]==null){
                    continue;
                }
                executionCycles++;
                Instruction currInst = instructionList[processNumber].pop();
                
                if (currInst.type==1){
                    if (!requestResource(currInst.value,processNumber,held,requests,requestingAccess)){
                        executionCycles--;
                    }
                    
                    if  ( 
                            hasCycle(requests, createVisitedMatrix(requests),processNumber, currInst.value, true)
                        ){
                        System.out.println("Deadlocked at ");
                    }
                }
                else if (currInst.type==2){
                    
                }
            }
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
    
    /**
     * @return True if request successful, false if resource had to block
     */
    private static boolean requestResource(int resource, int process, boolean[] held, boolean[][] requests, LinkedList<Integer>[] requestingAccess){
        boolean r=true;
        resource--;
        requests[process][resource]=true;
        if (held[resource]){
            requestingAccess[resource].add(process);
            r=false;
        }
        else{
            held[resource]=true;
        }
        return r;
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
            if (instructionList[i]!=null){
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
            for (int r=0;r<requests.length;r++){
                visited[p][r]=false;
            }
        }
        
        return visited;
    }
    
    /**
     * Needs to account for directional stuff. Should only be able to go from Process to resource if it's held(?) and from resource to process if requested (held?)
     * @param requests
     * @param visited
     * @param p
     * @param r
     * @param checkingResources
     * @return 
     */
    private static boolean hasCycle(boolean[][] requests, boolean[][] visited, int p, int r,boolean checkingResources) {
        //visited[p][r]=true;
        
        if (visited[p][r]==true){
            return true;
        }
        
        visited[p][r]=true;
        
        boolean ret=false;
        
        if (!checkingResources){
            
            for (int i=0;i<requests[p].length;i++){
                if (i==r)
                    continue;
                
                
                if (requests[p][i]==true){
                    boolean hasSubCycle=hasCycle(requests,visited,p,i,!checkingResources);
                    if (hasSubCycle){
                        ret=true;
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
                    boolean hasSubCycle=hasCycle(requests,visited,i,r,!checkingResources);
                    if (hasSubCycle){
                        ret=true;
                        break;
                    }
                }
            }
        }
        
        return ret;
    }
}
