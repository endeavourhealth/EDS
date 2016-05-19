package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.slf4j.*;

import java.io.InputStream;
import java.util.Properties;
import java.util.TimerTask;
import java.util.Vector;

public class SftpConsumer extends TimerTask {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpConsumer.class);

	private PipelineProcessor pipeline;
	private SftpReaderConfiguration configuration;
	private JSch jSch;

	public SftpConsumer(SftpReaderConfiguration configuration) {
		this.configuration = configuration;
		this.pipeline = new PipelineProcessor(configuration.getPipeline());
		this.jSch = new JSch();
	}

	@Override
	public void run() {
		Session session = null;
		Channel channel = null;

		try {
			session = getSession();
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp channelSftp = (ChannelSftp)channel;
			channelSftp.cd(configuration.getPath());
			Vector fileList = channelSftp.ls(configuration.getFilename());
			for (int i = 0; i<fileList.size(); i++) {
				ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry)fileList.get(i);
				InputStream stream = channelSftp.get(entry.getFilename());
				String messageData = IOUtils.toString(stream);
				Exchange exchange = new Exchange(messageData);
				pipeline.execute(exchange);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		} finally {
			if (channel != null && channel.isConnected())
				channel.disconnect();
			if (session != null && session.isConnected())
				session.disconnect();
		}
	}

	private Session getSession() throws JSchException {
			Session session = jSch.getSession(
					configuration.getCredentials().getUsername(),
					configuration.getHost(),
					configuration.getPort());
			session.setPassword(configuration.getCredentials().getPassword());
			Properties properties = new Properties();
			properties.put("StrictHostKeyChecking", "no");
			session.setConfig(properties);
			return session;
	}
}
