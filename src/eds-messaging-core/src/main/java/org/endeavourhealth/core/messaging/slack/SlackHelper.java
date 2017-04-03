package org.endeavourhealth.core.messaging.slack;

import com.google.common.base.Strings;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackException;
import net.gpedro.integrations.slack.SlackMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SlackHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SlackHelper.class);

    private static String slackUrl = null;


    public static void setSlackUrl(String slackUrl) {
        SlackHelper.slackUrl = slackUrl;
    }

    public static void sendSlackMessage(String message) {
        sendSlackMessage(message, (String)null);
    }

    public static void sendSlackMessage(String message, Exception ex) {

        String attachmentStr = null;
        if (ex != null) {

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            attachmentStr = sw.toString();
        }

        sendSlackMessage(message, attachmentStr);
    }

    public static void sendSlackMessage(String message, String attachment) {

        if (Strings.isNullOrEmpty(slackUrl)) {
            LOG.info("No Slack URL set for alerting");
            return;
        }

        SlackMessage slackMessage = new SlackMessage(message);

        if (!Strings.isNullOrEmpty(attachment)) {
            SlackAttachment slackAttachment = new SlackAttachment();
            slackAttachment.setFallback("Exception cannot be displayed");
            slackAttachment.setText("```" + attachment + "```");
            slackAttachment.addMarkdownAttribute("text"); //this tells Slack to apply the formatting to the text

            slackMessage.addAttachments(slackAttachment);
        }

        try {
            SlackApi slackApi = new SlackApi(slackUrl);
            slackApi.call(slackMessage);

        } catch (SlackException se) {
            LOG.error("Error sending Slack notification to " + slackUrl, se);
        }
    }
}
