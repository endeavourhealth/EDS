import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Subscriber {
	private static JFrame app;

	private JPanel main;
	private JList list1;
	private JLabel lblStatus;
	private DefaultListModel<String> listModel = new DefaultListModel<>();

	public static void main(String[] args) throws IOException {
		app = new JFrame("Subscriber");
		app.setContentPane(new Subscriber().main);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		app.pack();
		app.setVisible(true);
	}

	public Subscriber() throws IOException {
		list1.setModel(listModel);
		initializeHttpListener();
		lblStatus.setText("Listening...");
		list1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getClickCount() == 2) {
					int index = list1.locationToIndex(e.getPoint());
					if (index >= 0) {
						displayMessage(index);
					}
				}
			}
		});
	}

	private void displayMessage(int index) {
		String message = listModel.get(index);
		MessageBody messageBodyForm = new MessageBody();
		messageBodyForm.setText(message);

		final JDialog frame = new JDialog(app, "Message Body", true);
		frame.setMaximumSize(new Dimension(1280,1024));
		frame.setContentPane(messageBodyForm.main);
		frame.pack();
		frame.setVisible(true);
	}

	private void initializeHttpListener() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/notify", new NotificationHandler(listModel));
		server.setExecutor(null);
		server.start();
	}
}
