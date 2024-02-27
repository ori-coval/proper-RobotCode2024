// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.Utils.interpolation.InterpolateUtil;
import frc.robot.Constants;

import static frc.robot.Constants.ShooterArmConstants.*;

import java.util.function.DoubleSupplier;

public class ShooterArmSubsystem extends SubsystemBase {
  /** Creates a new ShooterArmSubsystem. */

  // created the motor and MotionMagic
  private TalonFX m_shooterArmMotor;
  DigitalInput limitSwitch = new DigitalInput(SwitchID);
  private final MotionMagicVoltage motionMagic = new MotionMagicVoltage(0,
  false,
  0.0, 
  0, 
  true, 
  false, 
  getSwitch());
  double targetPose = 0;

  boolean atLimit = false;
  boolean zeroedOut = false;

  // the instance
  private static ShooterArmSubsystem instance;

  public static ShooterArmSubsystem getInstance() {
    if (instance == null) {
      instance = new ShooterArmSubsystem();
    }
    return instance;
  }

  private ShooterArmSubsystem() {
    this.m_shooterArmMotor = new TalonFX(ShooterArmID, Constants.canBus_name);

    // create the full MotionMagic
    TalonFXConfiguration configuration = new TalonFXConfiguration();
    MotionMagicConfigs mm = new MotionMagicConfigs();

    mm.MotionMagicCruiseVelocity = mmCruise;
    mm.MotionMagicAcceleration = mmAcceleration;
    mm.MotionMagicJerk = mmJerk;
    configuration.MotionMagic = mm;

    configuration.Slot0.kP = kp;
    configuration.Slot0.kD = kd;
    configuration.Slot0.kV = kv;
    configuration.Slot0.kS = ks;

    configuration.Voltage.PeakForwardVoltage = peekForwardVoltage;
    configuration.Voltage.PeakReverseVoltage = peekReverseVoltage;

    // forward and backword limits
    configuration.SoftwareLimitSwitch.ForwardSoftLimitEnable = false;
    configuration.SoftwareLimitSwitch.ReverseSoftLimitEnable = false;
    configuration.SoftwareLimitSwitch.ForwardSoftLimitThreshold = forwardLimit;
    configuration.SoftwareLimitSwitch.ReverseSoftLimitThreshold = backwordLimit;

    configuration.Feedback.SensorToMechanismRatio = TICKS_PER_DEGREE;
    
    configuration.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;

    StatusCode statusCode = StatusCode.StatusCodeNotInitialized;

    for (int i = 0; i < 5; i++) {
      statusCode = m_shooterArmMotor.getConfigurator().apply(configuration);
      if (statusCode.isOK())
        break;
    }
    if (!statusCode.isOK())
      System.out.println("shooter Arm could not apply config, error code:" + statusCode.toString());

    m_shooterArmMotor.setPosition(shooterArmStartPose); 

    

  }

  // set the position of the arm
  public void setPosition(double pose) {
    m_shooterArmMotor.setPosition(pose);
  }

  // moving function for the Arm
  public void moveArmTo(double degrees) {
    targetPose = degrees;
    m_shooterArmMotor.setControl(motionMagic.withPosition(degrees));
  }

  // get function for the Arm pose
  public double getArmPose() {
    return m_shooterArmMotor.getPosition().getValue();
  }

  // Checking the degree difference conditions
  public boolean isArmReady() {
    return (Math.abs(getArmPose() - targetPose) < minimumError);
  }

  // geting if the switch is open
  public boolean getSwitch() {
    return !this.limitSwitch.get();
  }

  public void setSpeedArm(DoubleSupplier speed) {
    m_shooterArmMotor.set(speed.getAsDouble());
  }

  public double angleFromDistance(Double distance) {
    return InterpolateUtil.interpolate(SHOOTER_ANGLE_INTERPOLATION_MAP, distance);
  }

  public void runCharacterizationVolts(Double voltage) {
    m_shooterArmMotor.setVoltage(voltage);
  }

  public double getCharacterizationVelocity() {
    return m_shooterArmMotor.getVelocity().getValueAsDouble();
  }
  public void coast(){
    m_shooterArmMotor.setNeutralMode(NeutralModeValue.Coast);
  }

  public void breakMode(){
    m_shooterArmMotor.setNeutralMode(NeutralModeValue.Brake);
  }
  
  public void manualZeroShooterArm(){
    coast();
    if(getSwitch()){
      while (getSwitch()) {
      }
    }
    while (!getSwitch()) {}
    setPosition(resetPose);
    zeroedOut = true;
    breakMode();
  }

  @Override
  public void periodic() {
    SmartDashboard.putBoolean("ShooterArmSwitch", getSwitch());
    SmartDashboard.putNumber("ShooterArm pose", getArmPose());
    SmartDashboard.putBoolean("shooter arm zero", zeroedOut);
    if(getSwitch()&&!atLimit){
      m_shooterArmMotor.setPosition(resetPose);
      atLimit = true;
    }
    if(atLimit&&!getSwitch()){
      atLimit = false;
    }
  }
}
