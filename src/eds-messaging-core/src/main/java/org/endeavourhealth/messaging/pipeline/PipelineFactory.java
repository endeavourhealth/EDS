package org.endeavourhealth.messaging.pipeline;

public class PipelineFactory {
	public static MessagePipeline create(String messagePipelineClass) {
		Class<?> c = null;
		try {
			c = Class.forName(messagePipelineClass);
			MessagePipeline pipeline = (MessagePipeline) c.newInstance();
			return pipeline;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
