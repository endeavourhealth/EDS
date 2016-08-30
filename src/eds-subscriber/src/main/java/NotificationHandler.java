import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NotificationHandler implements HttpHandler{
	private static final Logger LOG = LoggerFactory.getLogger(NotificationHandler.class);
	private DefaultListModel<String> _log;

	public NotificationHandler(DefaultListModel<String> log) {
		this._log = log;
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		InputStream is = httpExchange.getRequestBody();
		String requestBody = IOUtils.toString(is);

		_log.addElement(requestBody);
		LOG.trace(requestBody);

		String response = "OK";
		httpExchange.sendResponseHeaders(200,response.length());
		OutputStream os = httpExchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
