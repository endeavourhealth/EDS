package org.endeavourhealth.core.cache;

import org.w3c.dom.Document;

import javax.xml.bind.*;
import javax.xml.validation.Schema;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Stack;

public class MarshallerPool implements ICacheable {
	private static MarshallerPool instance;

	public static MarshallerPool getInstance() {
		if (instance == null) {
			instance = new MarshallerPool();
			CacheManager.registerCache(instance);
		}
		return instance;
	}

	private final HashMap<Class,Stack<Marshaller>> marshallerPool = new HashMap<>();
	private final HashMap<Class,Stack<Unmarshaller>> unmarshallerPool = new HashMap<>();


	public void marshal(Class cls, JAXBElement element, Schema schema, StringWriter sw) throws JAXBException {
		Marshaller marshaller = marshallerPop(cls);
		marshaller.setSchema(schema);

		marshaller.marshal(element, sw);
		push(cls, marshaller);
	}

	public <T> JAXBElement<T> unmarshal(Class cls, Document doc, Schema schema) throws JAXBException {
		Unmarshaller unmarshaller = unmarshallerPop(cls);
		unmarshaller.setSchema(schema);

		JAXBElement<T> result = unmarshaller.unmarshal(doc, cls);
		push(cls, unmarshaller);
		return result;
	}

	@Override
	public synchronized void clearCache() {
		marshallerPool.values().forEach(Stack<Marshaller>::clear);
		marshallerPool.clear();
		unmarshallerPool.values().forEach(Stack<Unmarshaller>::clear);
		unmarshallerPool.clear();
	}

	private synchronized Stack<Marshaller> marshallerStack(Class cls) {
		Stack<Marshaller> stack = marshallerPool.get(cls);
		if (stack == null) {
			stack = new Stack<>();
			marshallerPool.put(cls, stack);
		}
		return stack;
	}

	private synchronized Marshaller marshallerPop(Class cls) throws JAXBException {
		Stack<Marshaller> stack = marshallerStack(cls);
		if (stack.size() > 0)
			return stack.pop();
		else {
			JAXBContext context = JAXBContext.newInstance(cls);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); //just makes output easier to read
			return marshaller;
		}
	}

	private synchronized void push(Class cls, Marshaller marshaller) {
		marshallerStack(cls).push(marshaller);
	}

	private synchronized Stack<Unmarshaller> unmarshallerStack(Class cls) {
		Stack<Unmarshaller> stack = unmarshallerPool.get(cls);
		if (stack == null) {
			stack = new Stack<>();
			unmarshallerPool.put(cls, stack);
		}
		return stack;
	}

	private synchronized void push(Class cls, Unmarshaller unmarshaller) {
		unmarshallerStack(cls).push(unmarshaller);
	}

	private synchronized Unmarshaller unmarshallerPop(Class cls) throws JAXBException {
		Stack<Unmarshaller> stack = unmarshallerStack(cls);
		if (stack.size() > 0)
			return stack.pop();
		else {
			JAXBContext context = JAXBContext.newInstance(cls);
			return context.createUnmarshaller();
		}
	}
}
