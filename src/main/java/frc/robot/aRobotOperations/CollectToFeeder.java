// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.aRobotOperations;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import frc.robot.Constants.IntakeConstants;
import frc.robot.basicCommands.IntakeArmCommands.MoveIntakeArmToDegree;
import frc.robot.basicCommands.IntakeCommands.IntakeSetSpeed;
import frc.robot.basicCommands.feederCommands.FeedUntilNote;

public class CollectToFeeder extends ParallelCommandGroup {
  /** Creates a new MoveIntakeToCollectNote. */
  public CollectToFeeder() {

    addCommands(
        //new MoveIntakeArmToDegree(IntakeConstants.GroundIntakePose),
        new IntakeSetSpeed(()->IntakeConstants.getNoteSpeed),
        new FeedUntilNote().andThen(new IntakeSetSpeed(()->0).asProxy()));
  }
}
