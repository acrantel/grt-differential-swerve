package frc.control;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.control.input.Input;
import frc.control.input.JoystickProfile;
import frc.gen.BIGData;
import frc.util.GRTUtil;

class DriverControl extends Mode {
    // if we are currently trying to center using camera/lidar data
    private boolean useCenter = false;

    private int lastPov;

    @Override
    public boolean loop() {
        JoystickProfile.updateProfilingPoints();
        driveSwerve();
        return true;
    }

    private void driveSwerve() {
        // zero swerve gyro if start button (menu button) is pressed on swerve
        // controller
        if (Input.SWERVE_XBOX.getStartButtonReleased()) {
            BIGData.putZeroGyroRequest(true);
        }

        double x = Input.SWERVE_XBOX.getX(Hand.kLeft);
        // negativize y so that up is forward
        double y = -Input.SWERVE_XBOX.getY(Hand.kLeft);
        x = JoystickProfile.applyProfile(x);
        y = JoystickProfile.applyProfile(y);

        // rotate the robot
        double lTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kLeft);
        double rTrigger = Input.SWERVE_XBOX.getTriggerAxis(Hand.kRight);
        double rotate;
        if (Input.SWERVE_XBOX.getBumper(Hand.kLeft)) {
            rotate = JoystickProfile.applyProfile(-(Math.abs(rTrigger) - Math.abs(lTrigger)));
            rotate = GRTUtil.transformation(-1, 1, -0.1, 0.1, rotate);

        } else {
            rotate = JoystickProfile.applyProfile(-(Math.abs(rTrigger) - Math.abs(lTrigger)));
        }
        if (rotate != 0) {
            BIGData.setPIDFalse();
        }

        int pov = Input.SWERVE_XBOX.getPOV();
        if (pov != -1) {
            lastPov = pov;
            BIGData.setAngle(pov);
        }
        BIGData.requestDrive(x, y, rotate);
    }
}
