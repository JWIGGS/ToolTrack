credits to https://github.com/tatobari/hx711py for providing the reference code for interfacing the RPi with the HX711 analog-to-digital converter.

Our group's implementation involved modifying example.py for the purpose of our app. This file is the main python code responsible for the operation of the lock mechanism, and the weight sensors which are used to tell the user if an item is available or not.

LOCK:
A while loop is used to continuously check the 'updated' status of the rack, which will have the value "open" or "close" to indicate the user's intention, when a user presses the lock button on the app. If the value is indeed either "open"/"close", the motor will unlock/lock the smart cabinet accordingly. Finally, the rack status will be updated to "opened" or "done" to indicate on Firebase if the motor movement has been completed.

WEIGHT SENSORS:
the data processing of the raw sensor data will be done by functions inside the hx711.py file, and returned here as the variable named sensor to be compared against the weight value that's retrieved from Firebase (we named this variable weight_ref). We check if the value of sensor is within our chosen margin of error, which depends on the sensitivity of the sensors used. In this case, we used a 20% margin of error.

The rest of the code remains mostly unchanged from the source as stated above.
In a nutshell, the weight sensors send analog data which are first read as bits and then converted into useful data represented in milligrams by the functions in hx711.py
