package deadlockdetection.osproj2;

import java.lang.IndexOutOfBoundsException;

public final class Instruction {
    int type;
    int value;
    
    public Instruction(int instType,int value){
        if (instType>=1 && instType<=3 && value>0){
            this.type=instType;
            this.value=value;
        }
        else{
            throw new IndexOutOfBoundsException();
        }
    }
}
