# grt-differential-swerve
This repository contains code for a differential swerve prototype for GRT (FRC Team 192).

Note: an actual prototype has yet to be built, and thus much of this code is untested.

Overview
--------
In differential swerve, module azimuth/speed is controlled by two drive motors linked through a differential, and the relationship between the two motor velocities determine the azimuthal velocity and wheel velocity. The difference between motor velocities determines the azimuthal velocity, and the average of the motor velocities determines the wheel's rotational velocity. 

This is in comparison with a normal swerve drive, which has separate steering and drive motors. Much of the code for controlling the swerve drive is the same, but the code for controlling individual modules is different.

Code Stuff
----------
In this implementation, the drivetrain is controlled by a triple cascade loop. The driver's input provides the setpoint for the general drivetrain loop (controls the heading and speed of the drivetrain), which then provides a setpoint for each individual module's loop. Each module will then determine the setpoint of the PID loops running on the motor controllers.

Currently, the upper loop (drivetrain) runs at 50Hz, the middle loops (modules) run at 200Hz, and the lower loops (motors) run at 1000Hz.

`src/main/java/frc/swerve` contains the Module and Swerve classes. 

`src/main/java/frc/gen/BIGData.java` is a central key-value database that stores robot state

`src/main/java/frc/control` contains driver control code

`src/main/deploy/diffswerve.txt` is a config file that will be automatically loaded onto the roboRIO on deploy

Mechanical Setup
---------------
This code is written for differential swerve modules with two NEO drive motors and an encoder for the azimuth, but it should be simple to adapt to other designs.

TODO
-----
- [ ] actually test on prototype
- [ ] tune PID loops


Helpful Links
------------
https://github.com/Team5818/DiffSwerve

https://www.chiefdelphi.com/t/team-5818-differential-swerve-drive/166571

https://www.chiefdelphi.com/t/yddr-775-pro-differential-swerve-prototype/160589

https://www.controlglobal.com/blogs/controltalkblog/cascade-control-perspective-tips/

