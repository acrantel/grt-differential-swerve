package frc.swerve;

import static frc.util.GRTUtil.TWO_PI;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.AnalogEncoder;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.controller.ProfiledPIDController;
import edu.wpi.first.wpilibj.drive.Vector2d;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;
import frc.gen.BIGData;
import frc.util.GRTUtil;

/**
 * requirements: rn swerve's update (and thus PID) is called every 20 ms -> we
 * should do wheel angle PID every 5 ms?! TODO PID control loop for rotation
 * (input to loop = absolute encoder tied to the wheel's rotation) -> PID
 * control loop for axle speed? not necessary because PID control loop for motor
 * speeds will get it to that speed PID control loop for motor speeds? done ->
 * inside SparkMax
 */
class Module {
	// motor controllers for neos in the swerve module
	private CANSparkMax sparkMax1;
	private CANSparkMax sparkMax2;
	// PID controllers for neos
	private CANPIDController pidMotor1;
	private CANPIDController pidMotor2;
	// encoders for each neo motor
	private CANEncoder driveEncoder1;
	private CANEncoder driveEncoder2;
	// whether the motors should be inverted
	private boolean invertMotor1;
	private boolean invertMotor2;
	/** encoder with the rotation of the wheel (direction its pointing) */
	private AnalogEncoder rotateEncoder;
	/**
	 * client requested wheel angle, in radians, where 0 is the 0 position of the
	 * rotateEncoder. this value will always be between 0 and 2pi
	 */
	private volatile double reqWheelAngle;
	/** client requested wheel spin speed, in radians/sec */
	private volatile double reqSpinSpeed;
	/** PID controller for the angle of the wheel (aka direction its pointing) */
	private ProfiledPIDController pidWheelAngle;

	/** Thread to run the PID loop for wheel angle */
	private Thread anglePIDThread;
	private final int ANGLE_PID_TIMING = 5;

	/** name of the module (e.g. "fr", "br", etc) */
	private String name;

	/** position of the module (with regards to the "center" of the robot), in meters */
	private Vector2d modulePosition;

	/** whether we are currently in "reversed mode". this is a state variable 
	 * used so we turn the shortest amount when changing the wheel angle. */
	private boolean reversed;

    /** ticks per rotation (this is for the rotateEncoder) */
	private final int TICKS_PER_ROTATION;
	/** number of rotations of the drive motor that results in one rotation of the ring gear */
	private final double STEERING_GEAR_RATIO;
	/** number of rotations of the motor shaft that results in one rotation of the wheel axle gear */
	private final double DRIVE_GEAR_RATIO;
	/** maximum motor speed, in radians per second */ 
	private final double MAX_MOTOR_SPEED; 

	/**
	 * @param name the name of the module ("fr", "br", etc)
	 * @param xPos the x position of the module relative to the center of the robot, in meters
	 * @param yPos the y position of the module relative to the center of the robot, in meters
	 */
	public Module(String name, double xPos, double yPos) {
		this.name = name;
		this.modulePosition = new Vector2d(xPos, yPos);
        // instantiate motor controllers
        sparkMax1 = new CANSparkMax(BIGData.getInt(name + "_drive1"), MotorType.kBrushless);
		sparkMax2 = new CANSparkMax(BIGData.getInt(name + "_drive2"), MotorType.kBrushless);
		// set up motor PID controllers
		pidMotor1 = sparkMax1.getPIDController();
		pidMotor2 = sparkMax2.getPIDController();
		configDrivePID(pidMotor1);
		configDrivePID(pidMotor2);
        // instantiate encoders for each neo
        driveEncoder1 = sparkMax1.getEncoder();
		driveEncoder2 = sparkMax2.getEncoder();
		// determine whether motors should be inverted
		invertMotor1 = BIGData.getBoolean(name + "_invert_motor1");
		invertMotor2 = BIGData.getBoolean(name + "_invert_motor2");
		sparkMax1.setInverted(invertMotor1);
		sparkMax2.setInverted(invertMotor2);

        // instantiate encoder for wheel rotation
        rotateEncoder = new AnalogEncoder(new AnalogInput(0));
		TICKS_PER_ROTATION = BIGData.getInt("swerve_rotate_encoder_ticks");

		// gear ratio between motor and wheel's axle
		STEERING_GEAR_RATIO = BIGData.getDouble("swerve_steering_gear_ratio");
		DRIVE_GEAR_RATIO = BIGData.getDouble("swerve_drive_gear_ratio");
		
		// maximum motor speed in radians/sec
		MAX_MOTOR_SPEED = BIGData.getDouble("swerve_max_motor_speed");

		// set up PID loop responsible for driving wheel azimuth to 0
		pidWheelAngle = new ProfiledPIDController(BIGData.getDouble("wheel_angle_kP"), 
			BIGData.getDouble("wheel_angle_kI"), BIGData.getDouble("wheel_angle_kD"), new TrapezoidProfile.Constraints(1, 1), 2);

        configDriveMotor(sparkMax1);
		configDriveMotor(sparkMax2);
		
		// start thread for wheel angle PID loop
		anglePIDThread = new Thread(this::runAnglePID);
		anglePIDThread.start();
	}

	public void enable() {
		// TODO add enabling/disabling functionality
    }
    
	public void disable() {
        // TODO add enabling/disabling functionality
	}

	/** Zeroes the wheel by updating the offset, and returns the new offset */
	public void zero() {
        rotateEncoder.reset();
    }

    /** Function that contains a loop that runs PID calculations for wheel angle 
	 * (at about 3 to 4 times faster than the swerve loop). Runs in a separate thread */
    private void runAnglePID() {
		long prevTime = System.currentTimeMillis();
		try {
			while (true) {
				doPIDCalc();
				while (System.currentTimeMillis() < prevTime + ANGLE_PID_TIMING) {
					Thread.sleep(1);
				}
				prevTime = System.currentTimeMillis();
			}
		} catch (InterruptedException e) {}
	}

	/** Calculate what speed the module should rotate at and what speed the 
	 * wheel should spin at from PID and trapezoidal profile calculations, 
	 * then set the motor speeds to the motors by calling setModuleSpeeds */
	private void doPIDCalc() {
		// current and target positions in rotations (for simpler calculations)
		double currentPosition = GRTUtil.positiveMod(rotateEncoder.get(), 1.0);
		double targetPosition = reqWheelAngle / TWO_PI;

		// below is code for turning the shortest distance to an angle (like 30 degrees instead of 330)
		// we are always be able to turn less than or equal to 90 deg to a target position
		boolean goalChanged = false; // whether targetPosition was changed
		double error = currentPosition - targetPosition;
		if (Math.abs(error) > 0.5) {
			// if the current values are more than half a rotation apart, move targetPosition 
			// one rotation closer to currentPosition so it is within half a rotation apart
			targetPosition += Math.signum(error);
			goalChanged = true;
		}
		error = currentPosition - targetPosition;
		if (Math.abs(error) > 0.25) {
			// move targetPosition a half rotation closer to currentPosition if necessary
			targetPosition += Math.signum(error) * 0.5;
			// at targetPosition, we'll need to reverse our spin direction
			reversed = true;
			goalChanged = true;
		} else {
			reversed = false;
		}
		// if our targetPosition (goal) changed, update the goal for the PID controller
		if (goalChanged) {
			pidWheelAngle.setGoal(targetPosition * TWO_PI);
		}

		double steerSpeed = pidWheelAngle.calculate(currentPosition * TWO_PI);
		setModuleSpeeds(steerSpeed, reqSpinSpeed * (reversed ? -1 : 1));
	}

    /**
     * Set this module's rotation and translation speeds.
     * @param steerSpeed the speed to turn the module at (change the azimuth of the wheel) in radians/sec
     * @param spinSpeed the speed to spin the wheel at in radians/sec
     */
    private void setModuleSpeeds(double steerSpeed, double spinSpeed) {
        // https://www.youtube.com/watch?v=1BQIqXVhLEc
		// (rad/s) the portion of the motors' rotation that goes towards changing the azimuth of the wheel
		double motorSteerSpeed = steerSpeed * STEERING_GEAR_RATIO;
		// (rad/s) the portion of the motors' rotation that goes towards spinning the axle of the wheel (translation)
		double motorSpinSpeed = spinSpeed * DRIVE_GEAR_RATIO;
		// one motor will spin at motorSteerSpeed + motorSpinSpeed, the other will spin at motorSteerSpeed - motorSpinSpeed because math
		// if the speeds will be too fast for the motors, scale down
		if (Math.abs(motorSteerSpeed) + Math.abs(motorSpinSpeed) > MAX_MOTOR_SPEED) {
			double scaleDown = MAX_MOTOR_SPEED / (Math.abs(motorSteerSpeed) + Math.abs(motorSpinSpeed));
			motorSteerSpeed *= scaleDown;
			motorSpinSpeed *= scaleDown;
		}
		pidMotor1.setReference(motorSteerSpeed + motorSpinSpeed, ControlType.kVelocity);
		pidMotor2.setReference(motorSteerSpeed - motorSpinSpeed, ControlType.kVelocity);
    }

	/** Set the speed and angle of this module
	 * @param radians The angle(radians) of the module to set, relative to the zero position of the rotation encoder
	 * @param speed The speed to spin the wheel at, in radians/sec
	 */
	public void set(double radians, double speed) {
		reqWheelAngle = GRTUtil.positiveMod(radians, TWO_PI);
		reqSpinSpeed = speed;
    }

	/** return the name of this wheel "fr", "br", "bl", "fl" */
	public String getName() {
		return name;
	}

	/**
	 * Get the x position of this module relative to the center of the robot
	 * @return the x position of the module (in meters)
	 */
	public double getModuleXPos() {
		return modulePosition.x;
	}

	/**
	 * Get the y position of this module relative to the center of the robot
	 * @return the y position of the module (in meters)
	 */
	public double getModuleYPos() {
		return modulePosition.y;
	}

	/** set up P, I, D, F constants for the velocity PIDF loop responsible for 
	 * keeping the motors spinning at the requested speed */
	private void configDrivePID(CANPIDController pid) {
		pid.setOutputRange(-0.9, 0.9);
		pid.setP(BIGData.getDouble("drive_velocity_kP"));
		pid.setI(BIGData.getDouble("drive_velocity_kI"));
		pid.setD(BIGData.getDouble("drive_velocity_kD"));
		pid.setFF(BIGData.getDouble("drive_velocity_kF"));
	}

	/** configure a drive motor for a diffy swerve module */
	private void configDriveMotor(CANSparkMax m) {
		m.restoreFactoryDefaults();
		m.setIdleMode(IdleMode.kBrake);
		m.setOpenLoopRampRate(0.1);
		m.setClosedLoopRampRate(0.1);
	}
}
