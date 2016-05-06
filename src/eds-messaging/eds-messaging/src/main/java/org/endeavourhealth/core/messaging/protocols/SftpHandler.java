package org.endeavourhealth.core.messaging.protocols;

import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.endeavourhealth.core.messaging.MessagePipeline;
import org.endeavourhealth.core.messaging.configuration.Configuration;
import org.endeavourhealth.core.messaging.configuration.schema.serviceConfiguration.SftpReceiver;
import org.endeavourhealth.core.messaging.model.IReceiver;
import org.endeavourhealth.core.messaging.model.MessageIdentity;
import org.endeavourhealth.core.messaging.model.SftpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class SftpHandler extends TimerTask {
	private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
	private Session session;
	private SftpReceiver receiver;
	private Timer timer;

	public SftpHandler(String username, String password, String host, Integer port, SftpReceiver receiver) {
		JSch jSch = new JSch();
		try {
			this.receiver = receiver;
			session = jSch.getSession(username, host, port);
			session.setPassword(password);
			Properties properties = new Properties();
			properties.put("StrictHostKeyChecking", "no");
			session.setConfig(properties);
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {
		timer = new Timer(true);
		timer.scheduleAtFixedRate(this, 0, receiver.getPollTime()*100);
	}

	public void stop() {
		timer.cancel();
		timer = null;
	}

	@Override
	public void run() {
		Channel channel = null;

		try {
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp channelSft = (ChannelSftp)channel;
			channelSft.cd(receiver.getPath());
			Vector fileList = channelSft.ls(receiver.getPath());
			for (int i = 0; i<fileList.size(); i++) {
				readFileAndProcess(channelSft, fileList.get(i).toString());
			}
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} finally {
			if (channel != null && channel.isConnected())
				channel.disconnect();
			if (session.isConnected())
				session.disconnect();
		}
	}

	private void readFileAndProcess(ChannelSftp channelSftp, String filename) {
		InputStream stream = null;
		try {
			stream = channelSftp.get(filename);
			String messageData = IOUtils.toString(stream);

			IReceiver receivePortHandler = Configuration.getInstance().getReceivePortHandler(receiver.getReceiverClass());
			try
			{
				SftpMessage message = SftpMessage.fromMessageData(messageData);
				MessageIdentity messageIdentity = receivePortHandler.identifyMessage(message);
				message.setMessageIdentity(messageIdentity);

				MessagePipeline pipeline = new MessagePipeline();
				pipeline.Process(message);
			}
			catch (Exception e)
			{
				receivePortHandler.handleError(e);
			}


		} catch (SftpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}