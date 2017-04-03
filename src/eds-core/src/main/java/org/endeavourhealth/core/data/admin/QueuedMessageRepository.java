package org.endeavourhealth.core.data.admin;

import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.admin.models.QueuedMessage;

import java.util.UUID;

public class QueuedMessageRepository extends Repository {
	public void save(UUID messageId, String messageBody) {
		Mapper<QueuedMessage> mapper = getMappingManager().mapper(QueuedMessage.class);

		QueuedMessage queuedMessage = new QueuedMessage();
		queuedMessage.setId(messageId);
		queuedMessage.setMessageBody(messageBody);

		mapper.save(queuedMessage);
	}


	public QueuedMessage getById(UUID id) {
		Mapper<QueuedMessage> mapper = getMappingManager().mapper(QueuedMessage.class);
		return mapper.get(id);
	}
}

