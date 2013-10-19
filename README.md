Sensors2PD
==========

An Android application that helps you to use sensors from your mobile to control Pure Data patchs.

This application uses libpd, so you need to download PdCore and set this library on your project. Link: https://github.com/libpd/pd-for-android

How To Use
===========

First, you need to create a PD patch using receivers from the sensors available on your Android device. You need to use the Browse menu option to load your patch from your sdcard and start playing. It is possible to select a PD file (*.pd) or a Zip file (*.zip). If you want to use a zip file, you need to use your PD file with the same name as the Zip file and left the PD file on root directory of your Zip file (compress the files, not the directory).

Sensors2PD will send float values from every change on sensors to PD, even if you do not use all the sensors values.

Guide:
 
If you have Acceleromer available, your device will send the sensor values using:
    [s sensor1v0]
    [s sensor1v1]
    [s sensor1v2]

Each sensor has an specific id and the id is the same for every Android device with the same sensor available. Here is the list:
     * Number   Type of Sensor
	 * 	 1		TYPE_ACCELEROMETER
	 * 	 2		TYPE_MAGNETIC_FIELD
	 * 	 3		TYPE_ORIENTATION
	 * 	 4		TYPE_GYROSCOPE
	 * 	 5		TYPE_LIGHT
	 * 	 6		TYPE_PRESSURE
	 * 	 7		TYPE_TEMPERATURE
	 * 	 8		TYPE_PROXIMITY
	 * 	 9		TYPE_GRAVITY
	 * 	10		TYPE_LINEAR_ACCELERATION
	 * 	11		TYPE_ROTATION_VECTOR
	 * 	12		TYPE_RELATIVE_HUMIDITY
	 * 	13		TYPE_AMBIENT_TEMPERATURE
	 *  14      TYPE_MAGNETIC_FIELD_UNCALIBRATED
	 *  15      TYPE_GAME_ROTATION_VECTOR	
	 *  16      TYPE_GYROSCOPE_UNCALIBRATED	
	 *  17      TYPE_SIGNIFICANT_MOTION
Source: http://developer.android.com/reference/android/hardware/Sensor.html
More informations about Android Sensors:
http://developer.android.com/guide/topics/sensors/sensors_overview.html

You can check the sensors available for your device using the Debug switch option on your menu (DebugSW) or checking the Guide.

You can use the Touch position (beta version) with your patch also. Every touch movement on the screen is sent using:
    [s sensorT0vx]
    [s sensorT0vy]


Questions or comments
=====================

Send email to:  dj [at] ime [dot] usp [dot] br