package frc.swerve;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.SPI;

public class NavXGyro extends AHRS {

	public NavXGyro() {
		super(SPI.Port.kMXP, (byte) 100);
	}

	@Override
	public void calibrate() {
		reset();
	}

}
