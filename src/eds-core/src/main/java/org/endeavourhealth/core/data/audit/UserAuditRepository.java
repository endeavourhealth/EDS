package org.endeavourhealth.core.data.audit;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.data.audit.models.IAuditModule;
import org.endeavourhealth.core.data.audit.models.UserEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserAuditRepository extends Repository{
    private IAuditModule module, subModule;

    public UserAuditRepository(IAuditModule auditModule) {
        super();
        if (auditModule.getParent() == null) {
            module = auditModule;
            subModule = null;
        } else {
            module = auditModule.getParent();
            subModule = auditModule;
        }
    }

    public void save(UUID userId, UUID organisationUuid, AuditAction action) {
        save(userId, organisationUuid, action, (Object[])null);
    }

    public void save(UUID userId, UUID organisationUuid, AuditAction action, Object... objects) {
        String data = "";

        if (objects != null) {
            for (Object object : objects) {
                try {
                    data += ObjectMapperPool.getInstance().writeValueAsString(object);
                } catch (JsonProcessingException e) {
                    data += object.toString();
                }
                data += System.lineSeparator();
            }
        }

        Mapper<UserEvent> userEventMapper = getMappingManager().mapper(UserEvent.class);

        UserEvent userEvent = new UserEvent(
            userId,
            module,
            subModule,
            action.name(),
            organisationUuid,
            data
        );

        userEventMapper.save(userEvent);
    }

    public Iterable<UserEvent> load(UUID userId, UUID serviceId, String module, String subModule, String action) {
        Select.Where statement;
        if (serviceId == null)
            statement = QueryBuilder.select()
                .from("audit", "user_event")
                .where(QueryBuilder.eq("user_id", userId));
        else {
            statement = QueryBuilder.select()
                .from("audit", "audit.user_event_by_service_id")
                .where(QueryBuilder.eq("user_id", userId))
                .and(QueryBuilder.eq("service_id", serviceId));
        }
        if (module!=null && !module.isEmpty()) {
            statement = statement.and(QueryBuilder.eq("module", module));
            if (subModule != null && !subModule.isEmpty()) {
                statement = statement.and(QueryBuilder.eq("submodule", subModule));
                if (action != null && !action.isEmpty())
                    statement = statement.and(QueryBuilder.eq("action", action));
            }
        }
        Session session = getSession();
        ResultSet resultSet = session.execute(statement);
        List<UserEvent> audit = resultSet.all().stream()
            .map(UserEvent::new)
            .collect(Collectors.toList());
        return audit;
    }

    public Iterable<String> getModuleList() {
        List<String> modules = new ArrayList<>();
        for (AuditModule module : AuditModule.values()) {
            modules.add(module.name());
        }
        return modules;
    }

    public Iterable<String> getSubModuleList(String module) {
        List<String> submodules = new ArrayList<>();
        submodules.addAll(AuditModule.allSubModules().stream()
            .filter(subModule -> ((Enum) subModule.getParent()).name().equals(module))
            .map(subModule -> ((Enum) subModule).name())
            .collect(Collectors.toList()));
        return submodules;
    }

    public Iterable<String> getActionList() {
        List<String> actions = new ArrayList<>();
        for (AuditAction action : AuditAction.values()) {
            actions.add(action.name());
        }
        return actions;
    }
}
