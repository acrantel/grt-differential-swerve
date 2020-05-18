package frc.swerve;

import edu.wpi.first.wpilibj.Notifier;
import frc.gen.BIGData;
import frc.util.GRTUtil;

public class Swerve {
	private final double SWERVE_WIDTH;
	private final double SWERVE_HEIGHT;
	private final double RADIUS;

	private final double ROTATE_SCALE;
	private NavXGyro gyro;
	/** array of swerve modules */
	private Module[] modules;

	/** requested x velocity, y velocity, angular velocity(rad/s), and angle */
	private volatile double userVX, userVY, userW, angle;
	/** determines if robot centric control or field centric control is used */
	private volatile boolean robotCentric;

	private Notifier swerveRunner;

	public Swerve() {
		this.gyro = new NavXGyro();
		gyro.reset();
		angle = 0.0;
		robotCentric = false;

		modules = new Module[1];
		modules[0] = new Module("module", 0, 0);

		SWERVE_WIDTH = BIGData.getDouble("swerve_width");
		SWERVE_HEIGHT = BIGData.getDouble("swerve_height");
		RADIUS = Math.sqrt(SWERVE_WIDTH * SWERVE_WIDTH + SWERVE_HEIGHT * SWERVE_HEIGHT) / 2;
		ROTATE_SCALE = 1 / RADIUS;
	}

	public void start() {
		if (swerveRunner != null) {swerveRunner.stop();}
		swerveRunner = new Notifier(this::runSwerve);
		swerveRunner.startPeriodic(0.02);
	}
	public void stop() {
		if (swerveRunner != null) {
			swerveRunner.stop();
		}
	}

	public void runSwerve() {
		refreshVals();
		changeModules(userVX, userVY, userW);
	}

	/** get values from BIGData and load into instance variables */
	private void refreshVals() {
		userVX = BIGData.getRequestedVX();
		userVY = BIGData.getRequestedVY();
		userW = BIGData.getRequestedW();

		if (BIGData.getZeroSwerveRequest()) {
			System.out.println("zeroing ALL wheels");
			zeroRotate();
			BIGData.putZeroSwerveRequest(false);
			BIGData.updateLocalConfigFile();
		}
		if (BIGData.getZeroGyroRequest()) {
			System.out.println("zeroing gyro");
			gyro.zeroYaw();
			BIGData.putZeroGyroRequest(false);
		}
		BIGData.putGyroAngle(gyro.getAngle());

		boolean zeroesUpdated = false;

		for (int i = 0; i < modules.length; i++) {
			if (BIGData.getZeroIndivSwerveRequest(i)) {
				zeroRotateIndiv(i);
				zeroesUpdated = true;
				BIGData.putZeroIndivSwerveRequest(i, false);
			}
		}
		if (zeroesUpdated) {
			BIGData.updateLocalConfigFile();
		}
	}

	/** sets whether we use robot centric or field centric control */
	public void setRobotCentric(boolean mode) {
		robotCentric = mode;
	}

	/**
	 * give new wheel position and spin speed values to the modules
	 * 
	 * @param vx
	 *               the requested x velocity from -1.0 to 1.0
	 * @param vy
	 *               the requested y velocity from -1.0 to 1.0
	 * @param w
	 *               the requested angular velocity
	 */
	private void changeModules(double vx, double vy, double w) {
		w *= ROTATE_SCALE;
		double gyroAngle = (robotCentric ? 0 : Math.toRadians(gyro.getAngle()));
		for (int i = 0; i < modules.length; i++) {
			// angle between the module, the center of the robot, and the x axis
			double wheelAngle = Math.atan2(modules[i].getModuleYPos(), modules[i].getModuleXPos()) - gyroAngle;
			// x component of tangential velocity
			double wx = (w * RADIUS) * Math.cos(Math.PI / 2 + wheelAngle);
			// y component of tangential velocity
			double wy = (w * RADIUS) * Math.sin(Math.PI / 2 + wheelAngle);
			double wheelVX = vx + wx;
			double wheelVY = vy + wy;
			double wheelPos = Math.atan2(wheelVY, wheelVX) + gyroAngle - Math.PI / 2;
			double power = Math.sqrt(wheelVX * wheelVX + wheelVY * wheelVY);
			modules[i].set(wheelPos, power);
		}
	}

	/**
	 * Zeroes the azimuth of all the wheels and sets them as zero in the
	 * currently running program and adds them to BIGData
	 */
	private void zeroRotate() {
		for (int i = 0; i < modules.length; i++) {
			modules[i].zero();
		}
	}

	/**
	 * Zeroes module's azimuth (its heading)
	 */
	private void zeroRotateIndiv(int wheelNum) {
		if (wheelNum < modules.length) {
			modules[wheelNum].zero();
		}
	}

}
