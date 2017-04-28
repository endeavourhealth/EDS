package org.endeavourhealth.core.slack;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackException;
import net.gpedro.integrations.slack.SlackMessage;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SlackHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SlackHelper.class);

    //enum to define the channels we can send to
    public enum Channel {
        ProductionAlerts("Production-Alerts");

        private String channelName = null;

        Channel(String channelName) {
            this.channelName = channelName;
        }

        public String getChannelName() {
            return channelName;
        }
    };

    private static Map<String, String> cachedUrls = null;

    /*private static String slackUrl = null;

    public static void setSlackUrl(String slackUrl) {
        SlackHelper.slackUrl = slackUrl;
    }*/

    public static void sendSlackMessage(Channel channel, String message) {
        sendSlackMessage(channel, message, (String)null);
    }

    public static void sendSlackMessage(Channel channel, String message, Exception ex) {

        String attachmentStr = null;
        if (ex != null) {

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            attachmentStr = sw.toString();
        }

        sendSlackMessage(channel, message, attachmentStr);
    }

    public static void sendSlackMessage(Channel channel, String message, String attachment) {

        String slackUrl = findUrl(channel);
        if (Strings.isNullOrEmpty(slackUrl)) {
            LOG.info("No Slack URL set for alerting to channel " + channel);
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

    private static synchronized String findUrl(Channel channel) {
        if (cachedUrls == null) {
            cachedUrls = new HashMap<>();

            try {
                JsonNode node = ConfigManager.getConfigurationAsJson("slack");
                Iterator<String> it = node.fieldNames();
                while (it.hasNext()) {
                    String field = it.next();
                    JsonNode child = node.get(field);
                    String url = child.asText();

                    cachedUrls.put(field, url);
                }

            } catch (Exception ex) {
                LOG.error("Error reading in slack config", ex);
            }
        }

        return cachedUrls.get(channel.getChannelName());
    }
}
