package frc.gen;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class BIGData {

	private static Map<String, String> map;

	// RPM map of <distance (inches), RPM> for when the hood is up
	public static TreeMap<Integer, Integer> upRPMMap;
	// RPM map of <distance (inches), RPM> for when the hood is down
	public static TreeMap<Integer, Integer> downRPMMap;

	public static void start() {
		map = new HashMap<String, String>();
		Config.start(map);
	}

	private static void existenceCheck(String key, String type) {
		if (!map.containsKey(key)) {
			switch (type) {
			case "boolean":
				put(key, false);
				break;
			case "double":
				put(key, 0.0);
				break;
			case "int":
				put(key, 0);
				break;
			case "String":
				put(key, "");
				break;
			case "long":
				put(key, 0);
				break;
			}
		}
	}

	/**
	 * Get the boolean config value corresponding to the key passed in.
	 * 
	 * @return The corresponding boolean value, or false if the key was invalid
	 */
	public static boolean getBoolean(String key) {
		existenceCheck(key, "boolean");
		return Boolean.parseBoolean(map.get(key));
	}

	/**
	 * Get the double config value corresponding to the key passed in.
	 * 
	 * @return The corresponding double value, or 0.0 if the key was invalid
	 */
	public static double getDouble(String key) {
		existenceCheck(key, "double");
		try {
			return Double.parseDouble(map.get(key));
		} catch (Exception e) {
			return 0.0;
		}
	}

	/**
	 * Get the int config value corresponding to the key passed in.
	 * 
	 * @return The corresponding integer value, or -1 if the key was not
	 *         found/invalid
	 */
	public static int getInt(String key) {
		existenceCheck(key, "int");
		try {
			return Integer.parseInt(map.get(key));
		} catch (Exception e) {
			return -1;
		}
	}

	public static long getLong(String key) {
		existenceCheck(key, "long");
		try {
			return Long.parseLong(map.get(key));
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Get the string value corresponding to the key passed in.
	 * 
	 * @return The corresponding string value, or the empty string if the key was
	 *         not found/invalid
	 */
	public static String getString(String key) {
		existenceCheck(key, "String");
		return map.get(key);
	}

	/**
	 * Request translational and angular velocity of the robot
	 * 
	 * @param vx
	 *               requested x velocity from -1.0 to 1.0
	 * @param vy
	 *               requested y velocity from -1.0 to 1.0
	 * @param w
	 *               requested angular velocity
	 */
	public static void requestDrive(double vx, double vy, double w) {
		put("requested_vx", vx);
		put("requested_vy", vy);
		put("requested_w", w);
	}

	/** get the requested x velocity of the robot */
	public static double getRequestedVX() {
		return getDouble("requested_vx");
	}

	/** get the requested y velocity of the robot */
	public static double getRequestedVY() {
		return getDouble("requested_vy");
	}

	/** get the requested angular velocity of the robot */
	public static double getRequestedW() {
		return getDouble("requested_w");
	}

	/** Request that the gyro be zeroed. */
	public static void putZeroGyroRequest(boolean request) {
		put("zero_gyro", request);
	}

	/** Get whether the gyro has been requested to be zeroed. */
	public static boolean getZeroGyroRequest() {
		return getBoolean("zero_gyro");
	}

	/** get a string containing a comma separated, ordered list of the joystick input mapping's x values */
	public static String getJoystickProfileXVals() {
		return getString("joystick_x_vals");
	}
	/** get a string containing a comma separated, ordered list of the joystick input mapping's y values */
	public static String getJoystickProfileYVals() {
		return getString("joystick_y_vals");
	}

	/** set the gyro's angle */
	public static void putGyroAngle(double angle) {
		put("gyro_ang", angle);
	}

	/** Request that swerve be zeroed. */
	public static void putZeroSwerveRequest(boolean request) {
		put("zero_swerve", request);
	}

	/** Get whether swerve has been requested to be zeroed. */
	public static boolean getZeroSwerveRequest() {
		return getBoolean("zero_swerve");
	}

	/** request to zero a single swerve module */
	public static void putZeroIndivSwerveRequest(int wheelNum, boolean setTo) {
		put("zero_module_" + wheelNum, setTo);
	}

	/** get whether a single swerve module has been requested to be zeroed */
	public static boolean getZeroIndivSwerveRequest(int wheelNum) {
		return getBoolean("zero_module_" + wheelNum);
	}
	/** set the config file message to display to drivers */
	public static void putConfigFileMsg(String msg) {
		put("config_msg", msg);
	}

	/** get the config file message to display to drivers */
	public static String getConfigFileMsg() {
		return getString("config_msg");
	}


	/** put (or update) a key/value mapping into the map */
	public static void put(String key, String val) {
		map.put(key, val);
	}

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, double val) {
		map.put(key, "" + val);
	}

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, int val) {
		map.put(key, "" + val);
	}

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, boolean val) {
		map.put(key, "" + val);
	}

	/** resets the local config file (that contains the swerve zeros) */
	public static void resetLocalConfigFile() {
		Config.resetLocalConfigFile();
	}

	/**
	 * Writes the current local mappings to the local config file in home/lvuser.
	 * (updates swerve zeroes in local file)
	 */
	public static void updateLocalConfigFile() {
		Config.updateLocalConfigFile();
    }
}