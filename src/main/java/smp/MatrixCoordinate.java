package smp;

public class MatrixCoordinate {
    private int i;
    private int j ;

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public void setI(int i) {
        this.i = i;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public MatrixCoordinate()
    {
        this.i = 0;
        this.j = 0;
    }

    public MatrixCoordinate(int i,int j)
    {
        this.i = i;
        this.j = j;
    }

    public String toString() {
        return Integer.toString(i) + "," + Integer.toString(j);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
