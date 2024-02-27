package frc.robot;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.Utils.PathPlanner.PathPlannerHelper;
import frc.robot.Constants.IntakeArmConstants;
import frc.robot.aRobotOperations.CollectToFeeder;
import frc.robot.aRobotOperations.PrepareForShoot;
import frc.robot.aRobotOperations.ShootTouchingBase;
import frc.robot.basicCommands.IntakeArmCommands.IntakeArmDown;
import frc.robot.basicCommands.IntakeArmCommands.IntakeArmUP;
import frc.robot.basicCommands.IntakeArmCommands.ZeroIntakeArm;
import frc.robot.basicCommands.IntakeCommands.IntakeSetSpeed;
import frc.robot.basicCommands.ShooterArmCommands.ZeroShooterArm;
import frc.robot.basicCommands.SwerveCommands.*;
import frc.robot.basicCommands.feederCommands.FeederSetSpeed;
import frc.robot.subsystems.*;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
    /* Controllers */
    private final CommandXboxController driver = new CommandXboxController(0);
    private final CommandXboxController operator = new CommandXboxController(1);
    private final CommandXboxController test = new CommandXboxController(2);

    // Driver Triggers
    Trigger driverYTrigger = driver.y();
    Trigger driverXTrigger = driver.x();
    Trigger driverRightBumperTrigger = driver.rightBumper();
    Trigger driverLeftTriggerToggle = driver.leftTrigger();
    Trigger driverBackTrigger = driver.back();
    Trigger driverPovLeftTrigger = driver.povLeft();
    Trigger driverPovRightTrigger = driver.povRight();

    // Operator Triggers
    Trigger operatorXTrigger = operator.x();
    Trigger operatorYTrigger = operator.y();
    Trigger operatorATrigger = operator.a();
    Trigger operatorRightBumperTrigger = operator.rightBumper();
    Trigger operatorLeftBumperTrigger = operator.leftBumper();
    Trigger operatorBTrigger = operator.b();

    PathPlannerHelper pathPlannerHelper = PathPlannerHelper.getInstace();

    /* Subsystems */
    private final SwerveSubsystem swerve = SwerveSubsystem.getInstance();
    private final IntakeSubsystem intake = IntakeSubsystem.getInstance();
    private final IntakeArmSubsystem intakeArm = IntakeArmSubsystem.getInstance();

    /**
     * The container for the robot. Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
        swerve.setDefaultCommand(
                new TeleopSwerve(
                        swerve,
                        () -> -driver.getHID().getLeftY(),
                        () -> -driver.getHID().getLeftX(),
                        () -> -driver.getHID().getRightX(),
                        true,
                        () -> driver.getHID().getRightTriggerAxis() > 0.5));


        //IntakeArmSubsystem.getInstance().setDefaultCommand(new IntakeArmSetSpeed(()-> -test.getRightX()));


        
        // ShooterArmSubsystem.getInstance().setDefaultCommand(new InstantCommand(()->{
        // ShooterArmSubsystem.getInstance().setSpeedArm(()-> test.getLeftX() / 10);
        // }, ShooterArmSubsystem.getInstance()));
        // test.b().onTrue(new ZeroShooterArm());

        // ShooterSubsystem.getInstance().setDefaultCommand(new InstantCommand(()->{
        // ShooterSubsystem.getInstance().manualSetShooterSpeed(()-> test.getRightY());
        // }, ShooterSubsystem.getInstance()));

        // IntakeSubsystem.getInstance().setDefaultCommand(new IntakeSetSpeed(()->
        // -test.getLeftY()));

        // test.a().onTrue(new CollectToIntake());

        // Configure the button bindings
        // test.x().onTrue(new MoveShooterArmTo(29));

        // driver.leftBumper().onTrue(new FeederSetSpeed(()->1));
        // test.rightBumper().onTrue(new FeederSetSpeed(()->0));
        // driver.start().onTrue(new ZeroShooterArm());
        // driver.x().whileTrue(new InstantCommand(()->
        // ShooterSubsystem.getInstance().setShooterSpeed(100,100),
        // ShooterSubsystem.getInstance()));
        // test.start().whileTrue(new InstantCommand(()->
        // ShooterSubsystem.getInstance().setShooterSpeed(0, 0),
        // ShooterSubsystem.getInstance()));

        // driver.leftTrigger().whileTrue(new AlignToSpeaker());
        // driver.x().onTrue(new ShooterArmSpeakerAngle());
        // driver.b().onTrue(new CollectToFeeder());
        // driver.a().onTrue(new ShootSpeaker());

        // test.b().onTrue(new IntakeArmUP());
        // test.x().onTrue(new IntakeArmDown());

        // driver.a().whileTrue(new InstantCommand(() -> ShooterSubsystem.getInstance().setShooterSpeed(100, 100),
        //         ShooterSubsystem.getInstance()));
        // // test.leftBumper().onTrue(new ShootTouchingBase());

        configureButtonBindings();
    }

    /**
     * Use this method to define your button->command mappings. Buttons can be
     * created by
     * 
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing
     * it to a {@link
     * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings() {

        // Driver Button Bindings
        driverYTrigger.onTrue(new InstantCommand(() -> swerve.zeroGyro()));
        driverXTrigger.onTrue(new ShootTouchingBase());
        driverRightBumperTrigger.whileTrue(new FeederSetSpeed(()->1).repeatedly());
        driverLeftTriggerToggle.toggleOnTrue(new AlignToSpeaker());
        driverBackTrigger.onTrue(swerve.disableHeadingCommand());
        driverPovLeftTrigger.onTrue(new ZeroIntakeArm());
        driverPovRightTrigger.onTrue(new ZeroShooterArm());
        driver.start().onTrue(new InstantCommand(()->ShooterSubsystem.getInstance().setShooterSpeed(70, 70
        ),ShooterSubsystem.getInstance()));

        // Operator Button Bindings
        operator.start().whileTrue(new AlignToAngle(Rotation2d.fromDegrees(90)));
        operatorXTrigger.onTrue(new CollectToFeeder());
        operatorYTrigger.toggleOnTrue(new PrepareForShoot().repeatedly());
        operatorATrigger
                .onTrue(Commands.runOnce(() -> intakeArm.moveArmTo(IntakeArmConstants.SafeSetPoint), intakeArm));
        operatorRightBumperTrigger.onTrue(new IntakeArmDown());
        operatorLeftBumperTrigger.onTrue(new IntakeArmUP());
        operatorBTrigger.onTrue(
                new InstantCommand(() -> ShooterSubsystem.getInstance().coast(), ShooterSubsystem.getInstance()));

        IntakeSubsystem.getInstance().setDefaultCommand(new IntakeSetSpeed(() -> -operator.getHID().getLeftY()));
        FeederSubsystem.getInstance().setDefaultCommand(new FeederSetSpeed(() -> -operator.getHID().getLeftY()));
    }
}
