package org.endeavourhealth.messaging;

import org.eclipse.jetty.http.HttpStatus;
import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.exceptions.ReceiverMethodNotSupportedException;
import org.endeavourhealth.messaging.utilities.html.Html;
import org.endeavourhealth.messaging.model.HttpMessage;
import org.endeavourhealth.messaging.model.IReceiver;
import org.endeavourhealth.messaging.exceptions.ReceiverNotFoundException;
import org.endeavourhealth.messaging.model.MessageIdentity;
import org.endeavourhealth.messaging.utilities.HttpContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

public class HttpHandler extends HttpServlet
{
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    public static final String SERVICEID_KEY = "ServiceId";
    public static final String RECEIVEPORTID_KEY = "ReceivePortId";

    private void HandleRequest(HttpServletRequest request, HttpServletResponse response)
    {
        String serviceId = getInitParameter(HttpHandler.SERVICEID_KEY);
        String receivePortId = getInitParameter(HttpHandler.RECEIVEPORTID_KEY);

        try
        {
            Configuration configuration = Configuration.getInstance();

            HttpMessage message = HttpMessage.fromServletRequest(request);

            IReceiver receivePortHandler = configuration.getReceivePortHandler(receivePortId);

            try
            {
                MessageIdentity messageIdentity = receivePortHandler.identifyMessage(message);
                message.setMessageIdentity(messageIdentity);

                MessagePipeline pipeline = new MessagePipeline();
                pipeline.Process(message);
            }
            catch (Exception e)
            {
                receivePortHandler.handleError(e);
            }

        }
        catch (Exception e)
        {
            // http responses

            try
            {
                if (ReceiverMethodNotSupportedException.class.isInstance(e))
                    WriteResponse(response, HttpStatus.METHOD_NOT_ALLOWED_405);
                else if (ReceiverNotFoundException.class.isInstance(e) && request.getPathInfo().equals("/"))
                    WriteResponse(response, HttpStatus.OK_200, Html.GetIndexPage());
                else if (ReceiverNotFoundException.class.isInstance(e))
                    WriteResponse(response, HttpStatus.NOT_FOUND_404);
                else
                    WriteResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
            catch (Exception e2)
            {
                // argh?!
                LOG.error("Error occurred handling exception in HttpHandler", e2);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HandleRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HandleRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HandleRequest(request, response);
    }

    private void WriteResponse(HttpServletResponse response, int status) throws IOException
    {
        String content = Html.GetErrorPage(status);

        WriteResponse(response, status, content);
    }

    private void WriteResponse(HttpServletResponse response, int status, String content) throws IOException
    {
        response.setStatus(status);
        response.setContentType(HttpContentType.TEXT_HTML);
        response.getWriter().write(content);
    }

    private void PrintInfo(HttpServletRequest request, HttpServletResponse response, IReceiver receiver) throws IOException
    {
        response.setContentType(HttpContentType.TEXT_HTML);
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter writer = response.getWriter();

        writer.println("<h3>Info</h3>");

        writer.println("<p>" + request.getMethod() + "</p>");
        writer.println("<p>" + request.getPathInfo() + "</p>");
        writer.println("<p>" + request.getQueryString() + "</p>");

        writer.println("<p>Headers:</p>");

        writer.println("<ul>");

        for (String headerName : Collections.list(request.getHeaderNames()))
            writer.println("<li>" + headerName + " = " + request.getHeader(headerName) + "</li>");

        writer.println("</ul>");

        String receiverName = "";

        if (receiver != null)
            receiverName = receiver.getClass().getCanonicalName();

        writer.println("<p>Receiver = " + receiverName + "</p>");
    }
}