# Door Sense Android App
This App uses the Metawear Android API to use the metawear as a door sensor that detects door openings and closings.
This App authenticate and store events logs in a web server via a RESTful API using the [Door Sense WebApp](https://github.com/atddev/Doorsense_webApp).
It also contains a small udp client and server that is used for the alarm functionality. 


## Usage Instructions:

1- Moify the hard-coded server addresses in the [SendToServer class](https://github.com/atddev/Metawear_Android/blob/master/app/src/main/java/com/asaad/metawearnative/SendToServer.java) and [the udp client class]()

2- Modify the hard-coded metawear MAC adddress in [the MainActivity](https://github.com/atddev/Metawear_Android/blob/master/app/src/main/java/com/asaad/metawearnative/MainActivity.java#L83) 

3- Locate the udp server code in [/app/src/main/res/udpServer.c](https://github.com/atddev/Metawear_Android/blob/master/app/src/main/res/udpServer.c#L44) and modify line 44

4- Compile and run

