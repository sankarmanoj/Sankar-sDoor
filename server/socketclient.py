
from threading import Thread
import os
class SocketClient(Thread):
    def __init__(self,Socket,Servo,mytype):
        self.socket=Socket
        self.servo = Servo
        self.type=mytype
        self.servo.open()
        self.running = True
        Thread.__init__(self)
        self.buffer = []
    def run(self):
        inputBufferString = ""
        while self.running:
            try:
                inChar = self.socket.recv(1)
                if ord(inChar)==13:
                    inputBufferString=""
                elif ord(inChar)==14:
                    self.execute(inputBufferString)
                else:
                    inputBufferString+=inChar
            except:
                self.socket.close()
                if "bluetooth" in self.type:
                    resetResult = os.popen("hciconfig hci0 reset").read()
                    print "HCI0 RESET"
                    if 'hci0' in resetResult:
                        resetBluetoothDongle()
                        print "Bluetooth USB Reset"
                break
    def execute(self,inputBufferString):
        self.buffer.append(inputBufferString)
        if("open" in inputBufferString):
            self.servo.open()
            print "Got Open State is "+self.servo.state
            self.socket.send(chr(13)+self.servo.state+chr(14))
        if ("close" in inputBufferString):
            self.servo.close()
            print "Got Close State is "+self.servo.state
            self.socket.send(chr(13)+self.servo.state+chr(14))
        if("status" in inputBufferString):
            print "Got Status Request"
            self.socket.send(chr(13)+self.servo.state+chr(14))
        if ("set" in inputBufferString):
            number = inputBufferString.split("=")[-1]
            try:
                percentage = int(number)
                print number
                self.servo.setAngleInPercentage(percentage)
            except ValueError:
                print " Invalid input!!"
        if("killmenow" in inputBufferString):
            self.socket.close()
            self.running=False
        print inputBufferString
    def sendMessage(self,message):
            self.socket.send(message)
class ServoController:
    def __init__(self,port):
        self.port = port
        self.state = "open"
        self.open()
    def open(self):
        commandString="echo %d=50%% > /dev/servoblaster"%self.port
        os.system(commandString)
        self.state="open"
    def close(self):
        commandString="echo %d=0%% > /dev/servoblaster"%self.port
        os.system(commandString)
        self.state="close"
    def setAngleInPercentage(self , percentage):
        commandString="echo %d=%d%% > /dev/servoblaster"%(self.port,percentage)
        os.system(commandString)
