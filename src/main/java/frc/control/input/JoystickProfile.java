package frc.control.input;

import frc.gen.BIGData;
import frc.util.GRTUtil;
import frc.util.PiecewiseLinearFunction;

public class JoystickProfile {
	private static final double DEFAULT_DEADBAND = 0.1;

	private static PiecewiseLinearFunction inputMapper;

	/** Default array of [original, new] mappings used to define the joystick profile
	 * if the user specified array is invalid */
	private static final double[] DEFAULT_X_VALS = {0, 0.35, 0.7, 1.0};
	private static final double[] DEFAULT_Y_VALS = {0, 0.1, 0.4, 1.0};

	private JoystickProfile() {}

	public static void init() {
		updateProfilingPoints();
	}

	/** parse profiling points from string loaded from config file */
	public static void updateProfilingPoints() {
		// x and y values of the joystick profile piecewise mapping function
		String[] stringXVals = BIGData.getJoystickProfileXVals().split(",");
		String[] stringYVals = BIGData.getJoystickProfileYVals().split(",");
		int arrayLen = Math.min(stringXVals.length, stringYVals.length);
		// parse double values from strings
		double[] xVals = new double[arrayLen];
		double[] yVals = new double[arrayLen];
		try {
			for (int i = 0; i < Math.min(stringXVals.length, stringYVals.length); i++) {
				xVals[i] = Double.parseDouble(stringXVals[i]);
				yVals[i] = Double.parseDouble(stringYVals[i]);
			}
		} catch (NumberFormatException e) {
			System.err.println("Unable to load joystick profile values, loading default instead.");
			xVals = DEFAULT_X_VALS;
			yVals = DEFAULT_Y_VALS;
		}
		inputMapper = new PiecewiseLinearFunction(xVals, yVals);
	}

	public static double applyProfile(double x) {
		double signum = Math.signum(x);
		// apply deadband
		double ans = applyDeadband(Math.abs(x));
		if (ans != 0) { // make values between 0 and 1-deadband
			ans -= DEFAULT_DEADBAND;
		}
		// transform values to be between 0 - 1
		ans = GRTUtil.transformation(0, 1 - DEFAULT_DEADBAND, 0, 1, ans);
		// apply profiling
		ans = inputMapper.apply(ans);
		return ans * signum;
	}

	/** applies the requested deadband to x. */
	public static double applyDeadband(double x, double deadband) {
		return (Math.abs(x) > deadband ? x : 0);
	}

	/** Applies the default deadband to the value passed in */
	public static double applyDeadband(double x) {
		return applyDeadband(x, DEFAULT_DEADBAND);
	}

	/** squares x while keeping the original sign. */
	public static double signedSquare(double x) {
		return Math.copySign(x * x, x);
	}

	/** applies the deadband to x and returns the signed square of the result */
	public static double clipAndSquare(double x) {
		return signedSquare(applyDeadband(x));
	}

}