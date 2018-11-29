package smp;

public class MatrixPair {
    private int leftValue;
    private int rightValue;
    private boolean isMatched;

    public MatrixPair()
    {
        leftValue = 0;
        rightValue = 0;
        isMatched = false;

    }

    public  MatrixPair(int leftValue, int rightValue)
    {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.isMatched = false;
    }

    public  MatrixPair(int leftValue, int rightValue, boolean isMatched)
    {
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.isMatched = isMatched;
    }

    public void setLeftValue(int leftValue) {
        this.leftValue = leftValue;
    }

    public int getLeftValue() {
        return leftValue;
    }

    public int getRightValue() {
        return rightValue;
    }

    public void setRightValue(int rightValue) {
        this.rightValue = rightValue;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }

    public boolean isMatched() {
        return isMatched;
    }

    @Override
    public String toString() {
        return Integer.toString(leftValue) + "," + Integer.toString(rightValue);
    }
}
