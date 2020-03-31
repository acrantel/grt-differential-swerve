
public class Test {
    public static void main(String[] args) {
        double[] xVals = {0, .35, .7, 1};
        double[] yVals = {0, .1, .4, 1};
        PiecewiseLinearFunction f = new PiecewiseLinearFunction(xVals, yVals);
        System.out.println(f.apply(0));
        System.out.println(f.apply(.1));
        System.out.println(f.apply(.35));
        System.out.println(f.apply(.5));
        System.out.println(f.apply(.6));
        System.out.println(f.apply(.7));
        System.out.println(f.apply(.8));
        System.out.println(f.apply(.9) );
        System.out.println(f.apply(1));
        System.out.println(f.apply(1.1));
        System.out.println(f.apply(-1));
    }
    /**
     * joystick_x1=0.35
joystick_y1=0.1
joystick_x2=0.7
joystick_y2=0.4
     */
}