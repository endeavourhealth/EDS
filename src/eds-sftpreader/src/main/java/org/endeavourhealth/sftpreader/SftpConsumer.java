package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TimerTask;
import java.util.Vector;

public class SftpConsumer extends TimerTask {
	private PipelineProcessor pipeline;
	private SftpReaderConfiguration configuration;
	private Session session;

	public SftpConsumer(SftpReaderConfiguration configuration) {
		this.configuration = configuration;
		pipeline = new PipelineProcessor(configuration.getPipeline());
		JSch jSch = new JSch();
		try {
			session = jSch.getSession(
					configuration.getCredentials().getUsername(),
					configuration.getHost(),
					configuration.getPort());
			session.setPassword(configuration.getCredentials().getPassword());
			Properties properties = new Properties();
			properties.put("StrictHostKeyChecking", "no");
			session.setConfig(properties);

		} catch (JSchException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		Channel channel = null;

		try {
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp channelSftp = (ChannelSftp)channel;
			channelSftp.cd(configuration.getPath());
			Vector fileList = channelSftp.ls(configuration.getPath());
			for (int i = 0; i<fileList.size(); i++) {
				InputStream stream = channelSftp.get(fileList.get(i).toString());
				String messageData = IOUtils.toString(stream);
				Exchange exchange = new Exchange(messageData);
				pipeline.execute(exchange);
			}
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (channel != null && channel.isConnected())
				channel.disconnect();
			if (session.isConnected())
				session.disconnect();
		}
	}
}
