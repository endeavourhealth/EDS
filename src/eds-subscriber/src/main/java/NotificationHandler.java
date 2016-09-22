import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NotificationHandler implements HttpHandler{
	private static final Logger LOG = LoggerFactory.getLogger(NotificationHandler.class);
	private Subscriber subscriber;

	public NotificationHandler(Subscriber subscriber) {
		this.subscriber = subscriber;
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		InputStream is = httpExchange.getRequestBody();
		String requestBody = IOUtils.toString(is);

		String messageId = httpExchange.getRequestHeaders().getFirst("MessageId");
		subscriber.listModel.addElement("Message Received : Id ["+messageId+"]");
		try {
			CegUnzipper.unzip(requestBody, new File(subscriber.unzipPath.getText()));
		} catch (Exception e) {
			LOG.error("Error unzipping data", e);
		}

		String response = "OK";
		httpExchange.sendResponseHeaders(200,response.length());
		OutputStream os = httpExchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
