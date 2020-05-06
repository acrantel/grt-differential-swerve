package frc.util;

public class PiecewiseLinearFunction {
    // x and y values of the points that define this piecewise function
    private double[] xVals;
    private double[] yVals;

    /**
     * xVals and yVals should be arrays of the same length and the xVals array should
     * be in ascending order. The piecewise function will be defined by 
     * points (xVals[i], yVals[i]) for each i.
     * @param xVals the x values that define the piecewise function
     * @param yVals the y values that define the piecewise function
     */
    public PiecewiseLinearFunction(double[] xVals, double[] yVals) {
        this.xVals = xVals.clone();
        this.yVals = yVals.clone();
    }

    /**
     * Applies the function to the input given. Returns 0 if the input is outside
     * of the domain of the piecewise function.
     * @return the function applied to the input given
     */
    public double apply(double input) {
        System.out.print("input:" + input + ",output:");
        // index of the point higher than the 
        int upperBoundIndex = 0;
        // for our purposes, the array isn't going to be long so no binary search
        while (upperBoundIndex < xVals.length && xVals[upperBoundIndex] <= input) {
            if (upperBoundIndex == xVals.length - 1 && 
                xVals[upperBoundIndex] == input) { // deal with upper edge case
                break;
            }
            upperBoundIndex++;
        }
        // if the input is outside the domain of our function
        if (upperBoundIndex == 0 || upperBoundIndex == xVals.length) {
            return -100;
        }
        return GRTUtil.transformation(xVals[upperBoundIndex-1], xVals[upperBoundIndex],
                yVals[upperBoundIndex-1], yVals[upperBoundIndex], input);
    }
}