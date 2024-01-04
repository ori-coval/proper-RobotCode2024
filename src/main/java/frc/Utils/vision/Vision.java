/*
 * MIT License
 *
 * Copyright (c) PhotonVision
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package frc.Utils.vision;

import static frc.robot.Constants.Vision.*;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import frc.robot.subsystems.SwerveSubsystem;

import java.util.Optional;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.PhotonUtils;
import org.photonvision.targeting.PhotonPipelineResult;

public class Vision {
    private static final PhotonCamera rightCamera = new PhotonCamera(kRightCameraName);
    private static final PhotonPoseEstimator rightPhotonEstimator =
                new PhotonPoseEstimator(
                        kTagLayout, PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR, rightCamera, kRightRobotToCam);
    public static double rightLastEstTimestamp = 0;

    private static final PhotonCamera leftCamera = new PhotonCamera(kLeftCameraName);
    private static final PhotonPoseEstimator leftPhotonEstimator =
                new PhotonPoseEstimator(
                    kTagLayout, PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR, leftCamera, kLeftRobotToCam);

    public static double leftLastEstTimestamp = 0;

    public static PhotonPipelineResult getLatestResult() {
        return rightCamera.getLatestResult();
    }

    /**
     * The latest estimated robot pose on the field from vision data. This may be empty. This should
     * only be called once per loop.
     *
     * @return An {@link EstimatedRobotPose} with an estimated pose, estimate timestamp, and targets
     *     used for estimation.
     */
    public static Optional<EstimatedRobotPose> getRightEstimatedGlobalPose() {
        var rightVisionEst = rightPhotonEstimator.update();

        double rightLatestTimestamp = rightCamera.getLatestResult().getTimestampSeconds();

        boolean rightNewResult = Math.abs(rightLatestTimestamp - rightLastEstTimestamp) > 1e-5;

        if (rightNewResult) rightLastEstTimestamp = rightLatestTimestamp;
        return rightVisionEst;
    }
    
    /**
     * The latest estimated robot pose on the field from vision data. This may be empty. This should
     * only be called once per loop.
     *
     * @return An {@link EstimatedRobotPose} with an estimated pose, estimate timestamp, and targets
     *     used for estimation.
     */
    public static Optional<EstimatedRobotPose> getLeftEstimatedGlobalPose() {
        var leftVisionEst = rightPhotonEstimator.update();

        double leftLatestTimestamp = rightCamera.getLatestResult().getTimestampSeconds();

        boolean leftNewResult = Math.abs(leftLatestTimestamp - leftLastEstTimestamp) > 1e-5;

        if (leftNewResult) leftLastEstTimestamp = leftLatestTimestamp;
        return  leftVisionEst;
    }

    public static Matrix<N3, N1> getEstimationStdDevs(Pose2d estimatedPose) {
        return rightLastEstTimestamp < leftLastEstTimestamp ? getEstimationStdDevsRight(estimatedPose) : getEstimationStdDevsLeft(estimatedPose);
    }
    
    private static Matrix<N3, N1> getEstimationStdDevsRight(Pose2d estimatedPose) {
        var estStdDevs = kRightSingleTagStdDevs;
        var targets = getLatestResult().getTargets();
        int numTags = 0;
        double avgDist = 0;
        for (var tgt : targets) {
            var tagPose = rightPhotonEstimator.getFieldTags().getTagPose(tgt.getFiducialId());
            if (tagPose.isEmpty())
                continue;
            numTags++;
            avgDist += tagPose.get().toPose2d().getTranslation().getDistance(estimatedPose.getTranslation());
        }
        if (numTags == 0)
            return estStdDevs;
        avgDist /= numTags;
        // Decrease std devs if multiple targets are visible
        if (numTags > 1)
            estStdDevs = kRightMultiTagStdDevs;
        // Increase std devs based on (average) distance
        if (numTags == 1 && avgDist > 4)
            estStdDevs = VecBuilder.fill(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        else
            estStdDevs = estStdDevs.times(1 + (avgDist * avgDist / 30));

        return estStdDevs;
    }

    private static Matrix<N3, N1> getEstimationStdDevsLeft(Pose2d estimatedPose) {
        var estStdDevs = kLeftSingleTagStdDevs; // Assuming you have constants for left camera as well
        var targets = getLatestResult().getTargets();
        int numTags = 0;
        double avgDist = 0;
        for (var tgt : targets) {
            var tagPose = leftPhotonEstimator.getFieldTags().getTagPose(tgt.getFiducialId());
            if (tagPose.isEmpty())
                continue;
            numTags++;
            avgDist += tagPose.get().toPose2d().getTranslation().getDistance(estimatedPose.getTranslation());
        }
        if (numTags == 0)
            return estStdDevs;
        avgDist /= numTags;
        // Decrease std devs if multiple targets are visible
        if (numTags > 1)
            estStdDevs = kLeftMultiTagStdDevs;
        // Increase std devs based on (average) distance
        if (numTags == 1 && avgDist > 4)
            estStdDevs = VecBuilder.fill(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        else
            estStdDevs = estStdDevs.times(1 + (avgDist * avgDist / 30));

        return estStdDevs;
    }

    public static double DistanceFromTarget() {
        return PhotonUtils.getDistanceToPose(SwerveSubsystem.getInstance().getPose(), target);

    }

    public static Rotation2d GetAngleFromTarget() {
        return PhotonUtils.getYawToPose(SwerveSubsystem.getInstance().getPose(), target);

    }
}
