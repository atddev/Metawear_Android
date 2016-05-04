# Door Sense Android App
This App uses the Metawear Android API to use the metawear as a door sensor that detects door openings and closings.
This App authenticate and store events logs in a web server via a RESTful API using the [Door Sense WebApp](https://github.com/atddev/Doorsense_webApp).
It also contains a small udp client and server that is used for the alarm functionality. 


## Usage Instructions:

1. Moify the hard-coded server addresses in the [SendToServer class](https://github.com/atddev/Metawear_Android/blob/master/app/src/main/java/com/asaad/metawearnative/SendToServer.java#L63) and address used for [the udp client class](https://github.com/atddev/Metawear_Android/blob/master/app/src/main/java/com/asaad/metawearnative/MainActivity.java#L62)

2. Modify the hard-coded metawear MAC adddress in [the MainActivity](https://github.com/atddev/Metawear_Android/blob/master/app/src/main/java/com/asaad/metawearnative/MainActivity.java#L53) 

3. Compile and run
 
## Additonal Instructions:
###UDP server:

- Locate the udp server code in [/app/src/main/res/udpServer.c](https://github.com/atddev/Metawear_Android/blob/master/app/src/main/res/udpServer.c)

- This is a basic udp server in C. it can be used to store the logs in a text file instead of the webapp (ex. for testing). It is also used in this app for the alert functionality, to send email alert using [sendmail](http://www.sendmail.org).

- If you plan in enabling the alert functionality and using the udp server, you have to [install sendmail](http://www.sendmail.org/~ca/email/doc8.12/op.html), and test the utility in the command line to make sure it is working.

- Modify [line 44 in the udp server code](https://github.com/atddev/Metawear_Android/blob/master/app/src/main/res/udpServer.c#L44)



