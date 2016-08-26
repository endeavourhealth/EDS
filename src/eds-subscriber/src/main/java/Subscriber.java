import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Subscriber {
	private JPanel main;
	private JList list1;
	private JLabel lblStatus;
	private DefaultListModel<String> listModel = new DefaultListModel<>();

	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame("Subscriber");
		frame.setContentPane(new Subscriber().main);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public Subscriber() throws IOException {
		list1.setModel(listModel);
		initializeHttpListener();
		lblStatus.setText("Listening...");
	}

	private void initializeHttpListener() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/notify", new NotificationHandler(listModel));
		server.setExecutor(null);
		server.start();
	}
}
