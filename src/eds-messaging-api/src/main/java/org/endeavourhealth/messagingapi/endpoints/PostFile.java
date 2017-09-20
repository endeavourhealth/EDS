package org.endeavourhealth.messagingapi.endpoints;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.endeavourhealth.common.config.ConfigManager;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("/")
public class PostFile extends AbstractEndpoint {
	private static String organisationId;

	@POST
	@Path("/PostFile")
	@RolesAllowed({"tpp-bulk-extract-provider", "homerton-bulk-extract-provider"})
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFiles(@Context final HttpServletRequest request) {
		// check publisher has a DPA with Discovery
		organisationId = request.getQueryString().replaceAll("organisationId=","");
		if (publisherHasDPA(request)) {
			// Check that we have a multi-part file upload request
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				// Create a factory for temp disk file items
				DiskFileItemFactory factory = new DiskFileItemFactory();
				// Configure a repository (to ensure a secure temp location is used)
				ServletContext servletContext = request.getServletContext();
				File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
				factory.setRepository(repository);
				// setup temp disk clearing tracker
				FileCleaningTracker fileCleaningTracker = FileCleanerCleanup.getFileCleaningTracker(servletContext);
				factory.setFileCleaningTracker(fileCleaningTracker);

				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);
				try {
					// Parse and upload the files into AWS
					List<FileItem> items = upload.parseRequest(request);
					MoveDataFilesToAWS(items);

				} catch (Exception e) {
					return Response.serverError().status(Response.Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
				}
				return Response.ok().status(Response.Status.OK).entity("Data file upload complete").build();
			}
			return Response.serverError().status(Response.Status.METHOD_NOT_ALLOWED).entity("Unexpected content. Multi-part expected").build();
		}
		return Response.serverError().status(Response.Status.METHOD_NOT_ALLOWED).entity(String.format("No DPA for publishing organisation: %s",organisationId)).build();
	}

	private static void MoveDataFilesToAWS(List<FileItem> fileItems) throws Exception {
		{
			JsonNode apiAWSConfig = null;
			try {
				apiAWSConfig = ConfigManager.getConfigurationAsJson("aws", "messaging-api");
			}
			catch (Exception ex)
			{
				throw new Exception("Failed to get messaging-api configuration for aws",ex);
			}

			String awsBucketName = apiAWSConfig.findValue("s3-bucket").textValue();
			String awsAccessKeyId = apiAWSConfig.findValue("access-key-id").textValue();
			String awsSecretKey = apiAWSConfig.findValue("secret-access-key").textValue();
			String awsRegion = apiAWSConfig.findValue("region").textValue();
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey);
			ClientConfiguration clientConfig = new ClientConfiguration();
			clientConfig.setConnectionTimeout(7200000); clientConfig.setSocketTimeout(7200000); clientConfig.setRequestTimeout(7200000);
			clientConfig.setRetryPolicy(PredefinedRetryPolicies.getDefaultRetryPolicyWithCustomMaxRetries(5));

			AmazonS3ClientBuilder s3 = AmazonS3ClientBuilder.standard()
					.withClientConfiguration(clientConfig)
					.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
					.withRegion(awsRegion);
			TransferManagerBuilder tx = TransferManagerBuilder.standard().withS3Client(s3.build());
			try {
				for (FileItem fileItem: fileItems) {
					if (!fileItem.isFormField()) {
						// get the file stream from the temp file on disk
						InputStream uploadedInputStream = fileItem.getInputStream();
						ObjectMetadata metaData = new ObjectMetadata();
						metaData.setContentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType());
						long sizeInBytes = fileItem.getSize();
						metaData.setContentLength(sizeInBytes);

						// create the AWS object key from the inbound filename
						String keyPath = createFileKey(fileItem.getName());
						System.out.println("keyPath: " + keyPath + " available received bytes: " + sizeInBytes);

						// upload the file stream to AWS
						try {
							Upload upload = tx.build().upload(awsBucketName, keyPath, uploadedInputStream, metaData);

							long megaBytes = -1;
							while (!upload.isDone()) {
								TransferProgress progress = upload.getProgress();
								double pct = progress.getPercentTransferred();
								long mBytes = progress.getBytesTransferred() / 1000000;
								if (megaBytes != mBytes) {
									megaBytes = mBytes;
									if (pct > 0)
										System.out.format(keyPath+": %.2f percent: ("+mBytes+" mb)",pct).println();
								}

								if (upload.getState() == Transfer.TransferState.Canceled)
								{
									uploadedInputStream.close();
									throw new SdkBaseException("Upload cancelled");
								}
							}

							uploadedInputStream.close();
							System.out.println("File: " + keyPath + " received and uploaded to AWS bucket: " + awsBucketName);
						}
						catch (Exception ex) {
							uploadedInputStream.close();
							ex.printStackTrace();
							throw ex;
						}
					}
				}
			} finally {
				tx.build().shutdownNow();
			}
		}
	}

	private static String createFileKey(String filePath) {
		return organisationId.concat("/data/" + filePath.replace("\\", "/"));
	}

	private static boolean publisherHasDPA(HttpServletRequest request)  {
		// check DPA in place for data publishing org
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			JsonNode dsaAppConfig = ConfigManager.getConfigurationAsJson("application","data-sharing-manager");
			String url = dsaAppConfig.findValue("appUrl").textValue()+"/api/dpa/checkOrganisationWithCount?odsCode="+organisationId;
			HttpGet httpGet = new HttpGet(url);
			// authenticated already from calling client, so re-use the header
			String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			httpGet.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader));
			HttpResponse response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300)
			{
				String responseValue = "0";
				if (response.getEntity() != null) {
					if (response.getEntity().getContent() != null) {
						responseValue = IOUtils.toString(response.getEntity().getContent());
					}
				}
				return Integer.parseInt(responseValue)>0;
			}
			else
				return false;
		} catch (IOException e) {
			return false;
		}
	}
}