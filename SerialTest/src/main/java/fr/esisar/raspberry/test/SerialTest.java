package fr.esisar.raspberry.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class SerialTest implements SerialPortEventListener {

	/**
	 * A BufferedReader which will be fed by a InputStreamReader converting the
	 * bytes into characters making the displayed results codepage independent.
	 */
	private BufferedReader input;

	/**
	 * The output stream to the port.
	 */
	// private OutputStream output;

	private static final String PORT_NAMES[] = { "/dev/tty.usbserial-A9007UX1", // MAC
			"/dev/ttyACM0", // Raspberry Pi
			"/dev/ttyUSB0", // Linux
			"/COM3" // Windows
	};

	private SerialPort serialPort;

	/**
	 * Milliseconds to block while waiting for port open.
	 */
	private static final int TIME_OUT = 2000;

	/**
	 * Default bits per second for COM port.
	 */
	private static final int DATA_RATE = 9600;

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public void serialEvent(SerialPortEvent serialPortEvent) {

		if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			String inputLine;
			try {
				inputLine = input.readLine();
				System.out.println(inputLine);
				System.out.println(AjouterDateHeure(inputLine));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	public String AjouterDateHeure(String mess_input) {
		String mess_output = mess_input;
		mess_output = mess_output + " 20180117";
		
		SimpleDateFormat formater = null;
		Date aujourdhui = new Date();
		formater = new SimpleDateFormat("HHmmss");
		System.out.println(formater.format(aujourdhui));
		
		mess_output = mess_output + formater.format(aujourdhui);
		
		System.out.println(mess_output);
		
		return mess_output;
	}

	public void initialize() {
		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();

		// Find an instance of serial port as set in PORT_NAMES
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}

		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// Open serial port, and use class name for the appName
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// Set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// Open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			// output = serialPort.getOutputStream();

			// Add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);

		} catch (PortInUseException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TooManyListenersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This should be called when you stop using the port. This will prevent port
	 * locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public static void main(String[] args) throws Exception {

		SerialTest serialTest = new SerialTest();

		serialTest.initialize();

		Thread thread = new Thread() {
			public void run() {
				// Keep alive this app for 1000 seconds, waiting for events to occur
				// and responding to them (printing incoming messages to console).
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		thread.start();

		System.out.println("Started");
	}

}
