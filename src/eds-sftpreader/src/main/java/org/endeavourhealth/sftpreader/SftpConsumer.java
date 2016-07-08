package org.endeavourhealth.sftpreader;

import com.google.common.io.Resources;
import com.jcraft.jsch.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.slf4j.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TimerTask;
import java.util.Vector;

public class SftpConsumer extends TimerTask
{
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpConsumer.class);

	private PipelineProcessor pipeline;
	private SftpReaderConfiguration configuration;
	private JSch jSch;

	public SftpConsumer(SftpReaderConfiguration configuration)
	{
		this.configuration = configuration;
		this.pipeline = new PipelineProcessor(configuration.getPipeline());
		this.jSch = new JSch();
	}

	@Override
	public void run()
	{
		Session session = null;
		ChannelSftp channel = null;

		try
		{
			session = getSession();
			channel = getChannel(session);

			channel.cd(configuration.getPath());
			Vector<ChannelSftp.LsEntry> fileList = channel.ls(configuration.getFilename());

			for (ChannelSftp.LsEntry file : fileList)
				processFile(file.getFilename(), channel.get(file.getFilename()));
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage());
		}
		finally
		{
			if (channel != null && channel.isConnected())
				channel.disconnect();

			if (session != null && session.isConnected())
				session.disconnect();
		}
	}

	private void processFile(String filename, InputStream fileStream) throws IOException
	{
		String messageData = IOUtils.toString(fileStream);
		Exchange exchange = new Exchange(messageData);
		pipeline.execute(exchange);
	}

	private ChannelSftp getChannel(Session session) throws JSchException
	{
		Channel channel = session.openChannel("sftp");
		channel.connect();

		return (ChannelSftp)channel;
	}

	private Session getSession() throws JSchException, IOException
	{
		jSch.addIdentity(getClientPrivateKeyPath());

		jSch.setKnownHosts(new ByteArrayInputStream(createKnownHostsString().getBytes()));

		Session session = jSch.getSession(
				configuration.getCredentials().getUsername(),
				configuration.getHost(),
				configuration.getPort());

		session.connect();

		return session;
	}

	public String createKnownHostsString() throws IOException
	{
		String hostPublicKey = FileUtils.readFileToString(new File(getHostPublicKeyPath()));
		return configuration.getHost() + " " + hostPublicKey;
	}

	private String getHostPublicKeyPath()
	{
		return resolveFilePath(configuration.getCredentials().getHostPublicKeyFilePath());
	}

	private String getClientPrivateKeyPath() throws IOException
	{
		return resolveFilePath(configuration.getCredentials().getClientPrivateKeyFilePath());
	}

	private String resolveFilePath(String filePath)
	{
		if (!Files.exists(Paths.get(filePath)))
			return Resources.getResource(filePath).getPath();

		return filePath;
	}
}
