#! /usr/bin/python2

import time
from time import sleep
import sys
#import firebase
#from firebase import firebase 
import firebase_admin
from firebase_admin import db
from firebase_admin import credentials
from datetime import datetime
import copy


#const admin = require("firebase-admin")
config = {
    "databaseURL": 'https://tooltrack-8c83f.firebaseio.com/',
    "storageBucket": 'gs://tooltrack-8c83f.appspot.com',
    "projectId": "tooltrack-8c83f",
    "databaseAuthVariableOverride": None,
}
cred = credentials.Certificate('/home/pi/Desktop/tooltrack-8c83f-firebase-adminsdk-pg30e-d6b1a9c340.json')

firebase_admin.initialize_app(cred, config)

#firebase = firebase.FirebaseApplication('https://tooltrack-8c83f.firebaseio.com/')
tools_ref = db.reference('/fablab/tools')
racks_ref = db.reference('/fablab/racks') ###update: added racks_ref


EMULATE_HX711=False

referenceUnit = 1

if not EMULATE_HX711:
    import RPi.GPIO as GPIO
    from hx711 import HX711
else:
    from emulated_hx711 import HX711

def cleanAndExit():
    print("Cleaning...")

    if not EMULATE_HX711:
        GPIO.cleanup()
        
    print("Bye!")
    sys.exit()

hx = HX711(5, 6)

# I've found out that, for some reason, the order of the bytes is not always the same between versions of python, numpy and the hx711 itself.
# Still need to figure out why does it change.
# If you're experiencing super random values, change these values to MSB or LSB until to get more stable values.
# There is some code below to debug and log the order of the bits and the bytes.
# The first parameter is the order in which the bytes are used to build the "long" value.
# The second paramter is the order of the bits inside each byte.
# According to the HX711 Datasheet, the second parameter is MSB so you shouldn't need to modify it.
hx.set_reading_format("MSB", "MSB")

# HOW TO CALCULATE THE REFFERENCE UNIT
# To set the reference unit to 1. Put 1kg on your sensor or anything you have and know exactly how much it weights.
# In this case, 92 is 1 gram because, with 1 as a reference unit I got numbers near 0 without any weight
# and I got numbers around 184000 when I added 2kg. So, according to the rule of thirds:
# If 2000 grams is 184000 then 1000 grams is 184000 / 2000 = 92.
hx.set_reference_unit(29)


#hx.set_reference_unit(referenceUnit)

hx.reset()

hx.tare()

print("Tare done! Add weight now...")

# to use both channels, you'll need to tare them both
#hx.tare_A()
#hx.tare_B()

##setup for the motor part
in1=24
in2=23
en=25
#direction=cw  #replace temp1 in the source code

GPIO.setmode(GPIO.BCM)
GPIO.setup(in1,GPIO.OUT)
GPIO.setup(in2,GPIO.OUT)
GPIO.output(in1,GPIO.LOW)
GPIO.output(in2,GPIO.LOW)
GPIO.setup(en,GPIO.OUT)
p = GPIO.PWM(en,1000)  #1000 means 1khz
p.start(100) #dutycycle in % of time ON

while True:
    try:
        # These three lines are useful to debug whether to use MSB or LSB in the reading formats
        # for the first parameter of "hx.set_reading_format("LSB", "MSB")".
        # Comment the two lines "val = hx.get_weight(5)" and "print val" and uncomment these three lines to see what it prints.
        
        # np_arr8_string = hx.get_np_arr8_string()
        # binary_string = hx.get_binary_string()
        # print binary_string + " " + np_arr8_string
        
        # Prints the weight. Comment if you're debugging the MSB and LSB issue.
        
        rack_ref = racks_ref.child('rack0') ###update: added racks_ref, since ref was for tools
        tool_ref = tools_ref.child('tool0') ###updated: ref to tools_ref
        weight_ref = tool_ref.child('weight').get()
        
#         print("user id: "+str(rack_ref.child('unlocked').get()))
        
        if rack_ref.child('updated').get() == "open":
            GPIO.output(in1,GPIO.HIGH)
            GPIO.output(in2,GPIO.LOW)
            #print direction here
            sleep(0.3) #depends how long the motor takes to turn 90 deg
            #stop the motor
            GPIO.output(in1,GPIO.LOW)
            GPIO.output(in2,GPIO.LOW)
            print("stopping")
            rack_ref.update({
                "updated": "opened"
                })
            
               
        ##repeat same but opposite for in1 and in2 to LOCK
        elif rack_ref.child('updated').get() == "close":
            #rotate motor
            print("closing")
            GPIO.output(in1,GPIO.LOW)
            GPIO.output(in2,GPIO.HIGH)
            #print direction here
            sleep(0.3) #turn 90 deg for 50rpm motor
            #stop the motor
            GPIO.output(in1,GPIO.LOW)
            GPIO.output(in2,GPIO.LOW)
            print("closed")
            rack_ref.update({
                "updated": "done"
                })
        
        
        
        ##check the weight to see if object is in
        sensor = hx.get_weight(5)
        print(sensor)
        
        #print(weight_ref)
        
        if rack_ref.child('updated').get() == "opened" and sensor > 0.8*weight_ref and sensor < 1.2*weight_ref:
            tool_ref.update({
                    'available' : 'true'
                    }
                )
            

        elif not (sensor > 0.8*weight_ref and sensor < 1.2*weight_ref):
            tool_ref.update({
                    'available' : 'false',
                    }
                
                )

        sleep(1)
        
        # To get weight from both channels (if you have load cells hooked up 
        # to both channel A and B), do something like this
        #val_A = hx.get_weight_A(5)
        #val_B = hx.get_weight_B(5)
        #print "A: %s  B: %s" % ( val_A, val_B )

        hx.power_down()
        hx.power_up()
        sleep(0.1)
    
    except (KeyboardInterrupt, SystemExit):
        #cleanAndExit()
        print('stop')
        
# print("clean up")
# GPIO.cleanup() #cleanup all GPIO, prevents "GPIO already in use" warnings

    
