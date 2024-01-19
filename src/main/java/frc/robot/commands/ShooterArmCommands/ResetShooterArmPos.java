// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.ShooterArmCommands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.ShooterArmSubsystem;

public class ResetShooterArmPos extends Command {
  /** Creates a new ResetShooterArmPos. */
  private final ShooterArmSubsystem shooterArmSubsystem = ShooterArmSubsystem.getInstance();
  
  public ResetShooterArmPos() {
    addRequirements(shooterArmSubsystem);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    shooterArmSubsystem.moveArmbySpeed();
  }


  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    shooterArmSubsystem.setPosition(0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return shooterArmSubsystem.getSwitch();
  }
}
