package org.usfirst.frc.team4500.robot.subsystems;

import org.usfirst.frc.team4500.robot.RobotMap;

import com.ctre.phoenix.ParamEnum;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Contains the two motors for each individual module. These are then grouped together in the SwerveDrive class
 */
public class WheelModule extends Subsystem {

	private TalonSRX angleMotor;
	private TalonSRX speedMotor;
	
	private String id;
	private int zero;
	
	/**
	 * Constructor that initializes the two motors and configures them with the desired information
	 * @param anglePort port for the angle motor
	 * @param speedPort port for the speed motor
	 * @param id of the module (to identify which one is which through the code)
	 * @param inverted should the angle motor be inverted
	 */
	public WheelModule(int anglePort, int speedPort, String id, boolean inverted, int zero) {
		this.id = id;
		this.zero = zero;
		
		angleMotor = new TalonSRX(anglePort);
		speedMotor = new TalonSRX(speedPort);
		
		angleMotor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, RobotMap.TIMEOUT);
		
//		int absolutePosition = angleMotor.getSensorCollection().getPulseWidthPosition() & 0xFFF;
//		angleMotor.setSelectedSensorPosition(absolutePosition, 0, RobotMap.TIMEOUT);
//		angleMotor.setSelectedSensorPosition(0, 0, RobotMap.TIMEOUT);
//		int absolutePosition = angleMotor.getSelectedSensorPosition(0);
//		angleMotor.setSelectedSensorPosition(absolutePosition, 0, RobotMap.TIMEOUT);
		
		angleMotor.setSensorPhase(false);
		angleMotor.configAllowableClosedloopError(0, 0, RobotMap.TIMEOUT);
		angleMotor.config_kP(0, 2, RobotMap.TIMEOUT); // 0.8
		angleMotor.config_kI(0, 0, RobotMap.TIMEOUT); 
		angleMotor.config_kD(0, 20, RobotMap.TIMEOUT); // 80
		angleMotor.config_kF(0, 0.546, RobotMap.TIMEOUT);
		angleMotor.config_IntegralZone(0, 0, RobotMap.TIMEOUT);
		angleMotor.configMotionCruiseVelocity(7488, RobotMap.TIMEOUT);
		angleMotor.configMotionAcceleration(7488, RobotMap.TIMEOUT); // 1800
		angleMotor.setInverted(inverted);
	}
	
    public void initDefaultCommand() {
        //setDefaultCommand(new MySpecialCommand());
    }
    
    public void calibrate() {
    	int quadPos = angleMotor.getSensorCollection().getQuadraturePosition();
		int pulseWidthPos = angleMotor.getSensorCollection().getPulseWidthPosition();
		
		if (quadPos < 0) {
			quadPos = 4096 - Math.abs(quadPos);
		}
		
		//setPoint = (quadPos % 4096) + (zero - pulseWidthPos);
		//angleMotor.set(ControlMode.Position, setPoint);
		//angleMotor.setSelectedSensorPosition(setPoint);
    }
    
    public void calibrate2() {
		int pulseWidthPos = angleMotor.getSensorCollection().getPulseWidthPosition();// & 0xFFF;
		
		//int setPoint = pulseWidthPos > 0 ? zero - (pulseWidthPos % 4096) : zero - Math.floorMod(pulseWidthPos, 4096);
		//setPoint *= -1;
		int setPoint = zero - (pulseWidthPos % 4096);
		
		//angleMotor.getSensorCollection().setQuadraturePosition(setPoint, RobotMap.TIMEOUT);
		angleMotor.setSelectedSensorPosition(setPoint, 0, RobotMap.TIMEOUT);
		//angleMotor.set(ControlMode.Position, setPoint);
		//angleMotor.setSelectedSensorPosition(setPoint);
    }
    
    public void setToSetPoint() {
    	angleMotor.set(ControlMode.MotionMagic, 0);
    }
    
    
    /*=====================
   	 * motor methods
   	 *=====================*/
    
    /**
	 * Adjusts the angle to fix wheel wrapping
	 * @param angle for the motor to be set to
	 * @return adjusted angle to account for wrapping
	 */
	public double adjustAngle(double angle) {
		double target = 0;
		double current = Math.round(angleMotor.getSelectedSensorPosition(0) / RobotMap.COUNTPERDEG);
		double Rcurrent = 0;
		double dir = Math.abs(current) % 360;
		if (current >= 180) { //OLD: > and <
			if (dir > 180) {
				Rcurrent = dir-360;
			} else {
				Rcurrent = dir;
			}
		} else if (current <= -180) {
			if (dir > 180) {
				Rcurrent = -1*dir+360;
			} else {
				Rcurrent = dir * -1;
			}
		} else {
			Rcurrent = current;
		}
		
		if (angle-Rcurrent <= 180 && angle-Rcurrent >= -180) {
			target = angle - Rcurrent + current;
		} else if (angle-Rcurrent >= 180) { // OLD: >
			target = angle - Rcurrent - 360 + current;
		} else {
			target = angle - Rcurrent + 360 + current;
		}
	
		return target;
	}
    
    /**
     * Called through the calculateVector method in the SwerveDrive class. Signals the two motors to start moving
     * @param speed of the module
     * @param angle of the module
     */
    public void drive(double speed, double angle) {
		angle = adjustAngle(angle);
		angle *= RobotMap.COUNTPERDEG;
		
		//SmartDashboard.putNumber(id + " set point", setPoint);
		//SmartDashboard.putNumber(id + " pwPos", angleMotor.getSensorCollection().getPulseWidthPosition());
    	
		//speedMotor.set(ControlMode.PercentOutput, speed);
		//angleMotor.set(ControlMode.MotionMagic, angle);
	}
    
    /*=====================
   	 * helper methods
   	 *=====================*/
    
    public int getAngleError() {
    	return angleMotor.getClosedLoopError(0);
    }
    
    public int getAnglePosition() {
    	return angleMotor.getSelectedSensorPosition(0);
    }
    
    public int getDrivePosition() {
    	return speedMotor.getSelectedSensorPosition(0);
    }
    
    public int getPulseWidth() {
    	return angleMotor.getSensorCollection().getPulseWidthPosition();
    }
    
    public int getQuadrature() {
    	return angleMotor.getSensorCollection().getQuadraturePosition();
    }
}

