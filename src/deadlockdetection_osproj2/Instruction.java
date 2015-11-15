package deadlockdetection_osproj2;

import java.lang.IndexOutOfBoundsException;

public final class Instruction {
    int type;
    int value;
    
    public Instruction(int instType,int value){
        if (
            !(instType>=1 && instType<=3) ||
            !(value>=1)
           ){
            throw new IndexOutOfBoundsException();
        }
        
        if (instType==1 || instType==2){
            value--;
        }
        this.type=instType;
        this.value=value;
    }
    
    public String toString(){
        int modifier=0;
        if (this.type==1 || this.type==2){
            modifier=1;
        }
        return String.format("(%d,%d)",this.type,this.value+modifier);
    }
}
