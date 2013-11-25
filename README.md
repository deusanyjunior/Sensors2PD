Sensors2PD
==========

An Android application that helps you to use sensors from your mobile to control Pure Data patchs.

This application uses [**libpd**](https://github.com/libpd/pd-for-android), so you need to download PdCore and set this library on your project.

How To Use
==========

First, you need to create a PD patch using receivers from the sensors available on your Android device. Your patch needs a [dac~] to use your device speaker or headphone jack as an audio output, and you can also use [adc~] to get audio from mic or headset.

Use Browse menu option to load your patch from your sdcard and start playing. It is possible to select a PD file (*.pd) or a Zip file (*.zip). If you want to use a zip file, you need to use your PD file with the same name as the Zip file and left this PD file on root directory of your Zip file (compress the files, not the directory).

Sensors2PD will send float values from every change on sensors to PD, even if you do not use all the sensors values.

Native sensors
--------------

You can receive values from all sensors available on your mobile device. Depending on the sensor id (ID) and variable number (#), you need to configure the receiver like that:

*    [r sensorIDv#]
 
If you have Acceleromer available, which has ID=1 and 3 variables, you can receive the sensor values using:

*    [r sensor1v0]
*    [r sensor1v1]
*    [r sensor1v2]

Touch position 
--------------

You can use the Touch position (beta version) up to as many touches your device screen can detect at the same time. The receiver needs touch id (ID) and position coordinate x or y (#).

*   [r sensorTIDv#]

If you want to track first touch position, which has ID=0, you need:

*   [r sensorT0vx]
*   [r sensorT0vy]

Wi-Fi level
----------

You can also use the Wi-Fi level. You will need to configure the receiver with the SSID without space on the name (ID).

*   [r sensorW-ID]

If your Wi-Fi SSID is "MyInternet", you'll need the receiver:

*   [r sensorW-MyInternet]


Sensors Descriptions
====================

Native sensores
---------------

Each sensor has an specific id and the id is the same for every Android device with the same sensor available. Here is the list:

*   #  Type of Sensor
*   1  TYPE_ACCELEROMETER
    *   values[0]: -9.81 to 9.81. Acceleration minus Gx on the x-axis
    *   values[1]: -9.81 to 9.81. Acceleration minus Gy on the y-axis
    *   values[2]: -9.81 to 9.81. Acceleration minus Gz on the z-axis
*   2  TYPE_MAGNETIC_FIELD
    *   values[0]: -2000 to 2000. Ambient magnetic field on the x-axis
    *   values[1]: -2000 to 2000. Ambient magnetic field on the y-axis
    *   values[2]: -2000 to 2000. Ambient magnetic field on the z-axis
*   3  TYPE_ORIENTATION  
    *   values[0]: 0 to 359, Azimuth, angle between the magnetic north direction and the y-axis. 0=North, 90=East, 180=South, 270=West
    *   values[1]: -180 to 180, Pitch, rotation around x-axis.
    *   values[2]: -90 to 90, Roll, rotation around the x-axis. Increasing as the device moves clockwise.
*   4  TYPE_GYROSCOPE
    *   values[0]: Angular speed around the x-axis
    *   values[1]: Angular speed around the y-axis
    *   values[2]: Angular speed around the z-axis
*   5  TYPE_LIGHT
    *   values[0]: Ambient light level in SI lux units
*   6  TYPE_PRESSURE
    *   values[0]: Atmospheric pressure in hPa (millibar)
*   7  TYPE_TEMPERATURE
    *   values[0]: ambient (room) temperature in degree Celsius.
*   8  TYPE_PROXIMITY
    *   values[0]: 0 to 1. Proximity sensor distance measured in centimeters
*   9  TYPE_GRAVITY
    *   values[0]: -9.81 to 9.81. Direction and magnitude of gravity on the x-axis
    *   values[1]: -9.81 to 9.81. Direction and magnitude of gravity on the y-axis
    *   values[2]: -9.81 to 9.81. Direction and magnitude of gravity on the z-axis
*   10 TYPE_LINEAR_ACCELERATION
    *   values[0]: -9.81 to 9.81. Acceleration along the x-axis
    *   values[1]: -9.81 to 9.81. Acceleration along the y-axis
    *   values[2]: -9.81 to 9.81. Acceleration along the z-axis
*   11 TYPE_ROTATION_VECTOR
    *   values[0]: x*sin(theta/2)
    *   values[1]: y*sin(theta/2)
    *   values[2]: z*sin(theta/2)
    *   values[3]: cos(theta/2)
    *   values[4]: estimated heading Accuracy (in radians) (-1 if unavailable)
*   12 TYPE_RELATIVE_HUMIDITY
    *   values[0]: Relative ambient air humidity in percent
*   13 TYPE_AMBIENT_TEMPERATURE
    *   values[0]: ambient (room) temperature in degree Celsius.
*   14 TYPE_MAGNETIC_FIELD_UNCALIBRATED
    *   values[0] = x_uncalib
    *   values[1] = y_uncalib
    *   values[2] = z_uncalib
    *   values[3] = x_bias
    *   values[4] = y_bias
    *   values[5] = z_bias
*   15 TYPE_GAME_ROTATION_VECTOR	
    *   values[0]: x*sin(theta/2)
    *   values[1]: y*sin(theta/2)
    *   values[2]: z*sin(theta/2)
    *   values[3]: cos(theta/2)
    *   values[4]: estimated heading Accuracy (in radians) (-1 if unavailable)
*   16 TYPE_GYROSCOPE_UNCALIBRATED
    *   values[0] : angular speed (w/o drift compensation) around the X axis in rad/s
    *   values[1] : angular speed (w/o drift compensation) around the Y axis in rad/s
    *   values[2] : angular speed (w/o drift compensation) around the Z axis in rad/s
    *   values[3] : estimated drift around X axis in rad/s
    *   values[4] : estimated drift around Y axis in rad/s
    *   values[5] : estimated drift around Z axis in rad/s
*   17 TYPE_SIGNIFICANT_MOTION
*   18 TYPE_STEP_DETECTOR
*   19 TYPE_STEP_COUNTER
*   20 TYPE_GEOMAGNETIC_ROTATION_VECTOR

More informations about Android Sensors:
*   http://developer.android.com/reference/android/hardware/Sensor.html
*   http://developer.android.com/guide/topics/sensors/sensors_overview.html
*   http://developer.android.com/reference/android/hardware/SensorEvent.html#values

You can check the sensors available for your device using the Debug switch option on menu.


Wi-Fi levels
-----------

The Wi-Fi level varies between -1 dBm to -100dBm.

Questions or comments
=====================

Send email to:  dj [at] ime [dot] usp [dot] br