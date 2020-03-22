package frc.swerve;

import static frc.util.GRTUtil.TWO_PI;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.AnalogEncoder;
import edu.wpi.first.wpilibj.AnalogInput;
import frc.gen.BIGData;

/**
 * requirements:
 * PID control loop for rotation (input to loop = absolute encoder tied to the wheel's rotation)
 * PID control loop for motor speeds? 
 */
class Wheel {
    // motor controllers for neos in the swerve module
    private CANSparkMax driveMotor1;
    private CANSparkMax driveMotor2;
    // encoders for each neo motor
	private CANEncoder driveEncoder1;
    private CANEncoder driveEncoder2;
    // encoder with the rotation of the wheel (direction its pointing)
    private AnalogEncoder rotateEncoder;
    // ticks per rotation (this is for the rotateEncoder)
    private final int TICKS_PER_ROTATION;
    
    // name of the module (e.g. "fr", "br", etc)
	private String name;

	private boolean reversed;

	public Wheel(String name) {
        this.name = name;
        // instantiate motor controllers
        driveMotor1 = new CANSparkMax(BIGData.getInt(name + "_drive1"), MotorType.kBrushless);
        driveMotor2 = new CANSparkMax(BIGData.getInt(name + "_drive2"), MotorType.kBrushless);
        // instantiate encoders for each neo
        driveEncoder1 = driveMotor1.getEncoder();
        driveEncoder2 = driveMotor2.getEncoder();
        // instantiate encoder for wheel rotation
        rotateEncoder = new AnalogEncoder(new AnalogInput(0));
        TICKS_PER_ROTATION = BIGData.getInt("rotate_encoder_ticks");
        
        configDriveMotor(driveMotor1);
        configDriveMotor(driveMotor2);
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
    
    /** update function with PID loops, etc */
    public void update() {
        // TODO call setModuleSpeeds here (pid agh)
    }

    /**
     * Set this module's rotation and translation speeds.
     * @param turnSpeed the speed to turn the module at (change the direction the wheel points) from -1.0 to 1.0
     * @param translateSpeed the speed to spin the wheel at from -1.0 to 1.0
     */
    private void setModuleSpeeds(double turnSpeed, double translateSpeed) {
        // https://www.youtube.com/watch?v=1BQIqXVhLEc
        // difference between motor speeds = how fast module turns
        // average of |motor speeds| = how fast wheel turns?
        // so -.5 + .5 -> .5 translation
        // 0 + 1 -> .5 translation, .5 module rotation
        // TODO add motor speed scaling too
        
    }



	public void set(double radians, double speed) {
		if (speed != 0.0) {
			double targetPosition = radians / TWO_PI;
			targetPosition = GRTUtil.positiveMod(targetPosition, 1.0);

			int encoderPosition = rotateMotor.getSelectedSensorPosition(0) - OFFSET;
			double currentPosition = encoderPosition / TICKS_PER_ROTATION;
			double rotations = Math.floor(currentPosition);
			currentPosition -= rotations;
			double delta = currentPosition - targetPosition;
			if (Math.abs(delta) > 0.5) {
				targetPosition += Math.signum(delta);
			}
			delta = currentPosition - targetPosition;
			boolean newReverse = false;
			if (Math.abs(delta) > 0.25) {
				targetPosition += Math.signum(delta) * 0.5;
				newReverse = true;
			}
			targetPosition += rotations;
			reversed = newReverse;
			double encoderPos = targetPosition * TICKS_PER_ROTATION + OFFSET;
			rotateMotor.set(ControlMode.Position, encoderPos);
			speed *= (reversed ? -1 : 1);
		}
		driveMotor.set(speed);
    }

	public int getEncoderPosition() {
		return rotateMotor.getSelectedSensorPosition(0) - OFFSET;
	}

	public double getDriveSpeed() {
		return driveEncoder.getVelocity() * DRIVE_TICKS_TO_METERS * (reversed ? -1 : 1) / 60.0;
	}

	public double getCurrentPosition() {
		return GRTUtil.positiveMod((((rotateMotor.getSelectedSensorPosition(0) - OFFSET) * TWO_PI / TICKS_PER_ROTATION)
				+ (reversed ? Math.PI : 0)), TWO_PI);
	}

	/** return the name of this wheel "fr", "br", "bl", "fl" */
	public String getName() {
		return name;
	}

	/** Return the rotationally zero position of the module in encoder ticks */
	public int getOffset() {
        // TODO use rotateEncoder.reset(); instead of OFFSET
		return OFFSET;
	}

	/** get the drive motor speed in rotations/second */
	public double getRawDriveSpeed() {
		// (rotations/minute) * (1 min/60 sec)
		return driveEncoder.getVelocity() / 60;
	}

	/** get the rotate motor speed in rotations/sec */
	public double getRawRotateSpeed() {
		// (ticks/100ms) / (ticks/rotation) * (10 (100ms)/1s)
		return (rotateMotor.getSelectedSensorVelocity() / TICKS_PER_ROTATION) * 10;
	}

	private void configRotateMotor() {
		GRTUtil.defaultConfigTalon(rotateMotor);

		boolean inverted = BIGData.getBoolean("swerve_inverted");
		rotateMotor.setInverted(inverted);
		rotateMotor.setSensorPhase((!inverted) ^ BIGData.getBoolean("sensor_phase"));
		rotateMotor.configSelectedFeedbackSensor(FeedbackDevice.Analog, 0, 0);
		rotateMotor.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 10, 0);

		rotateMotor.config_kP(0, kP / TICKS_PER_ROTATION, 0);
		rotateMotor.config_kI(0, 0, 0);
		rotateMotor.config_kD(0, kD / TICKS_PER_ROTATION, 0);
		rotateMotor.config_kF(0, 0, 0);
		rotateMotor.configMaxIntegralAccumulator(0, 0, 0);
		rotateMotor.configAllowableClosedloopError(0, 0, 0);
	}

	private void configDriveMotor(CANSparkMax m) {
		m.restoreFactoryDefaults();
		m.setIdleMode(IdleMode.kBrake);
		m.setOpenLoopRampRate(0.1);
	}
}
