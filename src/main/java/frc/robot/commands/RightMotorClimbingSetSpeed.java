// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.ClimbingSubsystem;

public class RightMotorClimbingSetSpeed extends Command {
  private final ClimbingSubsystem rightMotorClimbing = ClimbingSubsystem.getInstance();
  double speed;
  /** Creates a new RightMotorClimbingSetSpeed. */
  public RightMotorClimbingSetSpeed(double speed) {
    this.addRequirements(rightMotorClimbing);
    this.speed = speed;
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    this.rightMotorClimbing.setSpeedClimbing(speed);

  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
