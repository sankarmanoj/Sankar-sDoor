import socketclient
from socketclient import ServoController
import bluetooth
import socket
import signal
import os
from threading import Thread
def resetBluetoothDongle():
    result = os.popen("lsusb").read()
    devices = result.split("\n")
    for device in devices:
        if "Bluetooth Dongle" in device:
            words = device.split(" ")
            bus = int(words[1].strip('0'))
            devicecode = int(words[3].strip('0').strip(':'))
            print "Bus = ",bus
            print "Device = ",devicecode
            os.system("usbreset /dev/bus/usb/%s/%s"%(bus,devicecode))
resetResult = os.popen("hciconfig hci0 reset").read()
if 'hci0' in resetResult:
    resetBluetoothDongle()
os.system("killall servod")
os.system("servod")
bluetoothClients=[]
tcpClients=[]
myServo = ServoController(2)
servers=[]
runServer=True

def BluetoothServer():
    server=bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    server.bind(("",1))
    server.listen(2)
    servers.append(server)
    print "Bluetooth Server Started"
    try:
        while runServer :
            client,addr = server.accept()
            print "Bluetooth Client Address = "+str(addr)
            newClient = socketclient.SocketClient(client,myServo,"bluetooth")
            newClient.start()
            bluetoothClients.append(newClient)
        print "Closing Bluetooth Server"
    except KeyboardInterrupt:
        server.close()
def TCPServer():
    server = socket.socket()
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(("",8439))
    server.listen(5)
    servers.append(server)
    print "TCP Server Started"
    try:
        while runServer:
            client,addr = server.accept()
            print "TCP Client Connected from address "+str(addr)
            newClient=socketclient.SocketClient(client,myServo,"tcp")
            newClient.start()
            tcpClients.append(newClient)
        print "Closing TCP Server"
    except KeyboardInterrupt:
        server.close()
print "Starting Server"
bluetoothServerThread=Thread(target=BluetoothServer)
TCPServerThread=Thread(target=TCPServer)
bluetoothServerThread.daemon=False
TCPServerThread.daemon=False
bluetoothServerThread.start()
TCPServerThread.start()
def sigterm_handler(_signo, _stack_frame):
    runServer=False
    print "Going to Kill"
    for server in servers:
        server.close()
    sys.exit(0)
#signal.signal(signal.SIGTERM, sigterm_handler)
