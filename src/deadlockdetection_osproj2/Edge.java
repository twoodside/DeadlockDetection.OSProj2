package deadlockdetection_osproj2;

public class Edge {

    int process;
    int resource;
    
    public Edge(int process, int resource) {
        this.process = process;
        this.resource = resource;
    }
    
    
    @Override
    public String toString() {
        return String.format("Process %d -> Resource %d",process+1,resource+1);
    }
}
