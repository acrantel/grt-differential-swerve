/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import frc.control.input.JoystickProfile;
import frc.gen.BIGData;
import frc.swerve.Swerve;
import frc.control.Mode;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    private Swerve swerve;
    @Override
    public void robotInit() {
        BIGData.start();
        JoystickProfile.init();
        Mode.initModes();
        swerve = new Swerve();
        swerve.start();
    }

    @Override
    public void autonomousInit() {
    }

    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void teleopInit() {
    }

    @Override
    public void teleopPeriodic() {
        Mode.getMode(0).loop();
    }

    @Override
    public void testInit() {
    }

    @Override
    public void testPeriodic() {
    }

}
