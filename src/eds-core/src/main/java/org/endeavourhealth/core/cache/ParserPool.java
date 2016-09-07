package org.endeavourhealth.core.cache;

import org.hl7.fhir.instance.formats.IParser;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.model.Resource;

import java.util.Stack;

public class ParserPool implements ICacheable {
	private static ParserPool instance;

	public static ParserPool getInstance() {
		if (instance == null) {
			instance = new ParserPool();
			CacheManager.registerCache(instance);
		}
		return instance;
	}

	private final Stack<IParser> jsonPool = new Stack<>();
	private final Stack<IParser> xmlPool = new Stack<>();

	@Override
	public synchronized void clearCache() {
		jsonPool.clear();
		xmlPool.clear();
	}

	public String composeString(String contentType, Resource resource) throws Exception {
		IParser parser = pop(contentType);
		try {
			return parser.composeString(resource);
		} finally {
			push(parser);
		}
	}

	public Resource parse(String contentType, String data) throws Exception {
		IParser parser = pop(contentType);
		try {
			return parser.parse(data);
		} finally {
			push(parser);
		}
	}

	private IParser pop(String contentType) {
		if (contentType == null || contentType.isEmpty())
			return jsonPop();

		if ("text/xml".equals(contentType) || "application/xml".equals(contentType))
			return xmlPop();

		return jsonPop();
	}

	private synchronized void push(IParser parser) {
		if (parser instanceof JsonParser)
			jsonPool.push(parser);
		else
			xmlPool.push(parser);
	}

	private synchronized IParser jsonPop() {
		if (jsonPool.size() > 0)
			return jsonPool.pop();
		else
			return new JsonParser();
	}

	private synchronized IParser xmlPop() {
		if (xmlPool.size() > 0)
			return xmlPool.pop();
		else
			return new XmlParser();
	}
}
