package frc.control;

public abstract class Mode {
    private static DriverControl driverControl;
    private static Mode[] modes;

    public static void initModes() {
        driverControl = new DriverControl();
        modes = new Mode[1];
        modes[0] = driverControl;
    }

    public abstract boolean loop();

    public static Mode getMode(int i) {
        return modes[i];
    }

}
