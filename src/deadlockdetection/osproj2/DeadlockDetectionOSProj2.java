package deadlockdetection.osproj2;

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
            
            for (int i=0;i<instructionList.length;i++){
                instructionList[i]=new LinkedList();
                int instCount = in.nextInt();
                for (int j=0;j<instCount;j++){
                    instructionList[i].add(new Instruction(in.nextInt(),in.nextInt()));
                }
            }
            
            for (int i=0;i<instructionList.length;i++){
                System.out.printf("Process %d:\n",i);
                while (instructionList[i].peek()!=null){
                    Instruction inst = instructionList[i].pop();
                    System.out.printf(
                            "(%d,%d)\n",
                            inst.instType,
                            inst.value
                    );
                    
                }
            }
        }
    }
    
    private static void init(){
        
    }
}
