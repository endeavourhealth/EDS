package org.endeavourhealth.core.data.audit;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.cassandra.Repository;
import org.endeavourhealth.core.data.audit.models.*;

import java.util.*;
import java.util.stream.Collectors;

public class UserAuditRepository extends Repository {
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
        save(userId, organisationUuid, action, null);
    }

    public void save(UUID userId, UUID organisationUuid, AuditAction action, String title) {
        save(userId, organisationUuid, action, title, (Object[])null);
    }

    public void save(UUID userId, UUID organisationUuid, AuditAction action, String title, Object... paramValuePairs) {

        StringBuffer buf = new StringBuffer();
        buf.append((title == null ? "" : title));

        if (paramValuePairs != null) {
            for (Object object : paramValuePairs) {
                buf.append(System.lineSeparator());
                try {
                    buf.append(ObjectMapperPool.getInstance().writeValueAsString(object));
                } catch (JsonProcessingException e) {
                    buf.append(object.toString());
                }
            }
        }


        Mapper<UserEvent> userEventMapper = getMappingManager().mapper(UserEvent.class);

        UserEvent userEvent = new UserEvent(
            userId,
            module,
            subModule,
            action.name(),
            organisationUuid,
            buf.toString()
        );

        userEventMapper.save(userEvent);
    }

    public List<UserEvent> load(String module, UUID userId, Date month, UUID organisationId) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        cal.set(Calendar.DATE,1);
        Date startDate = cal.getTime();
        cal.add(Calendar.MONTH,1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date endDate = cal.getTime();

        Select.Where statement;
        if (organisationId == null)
            statement = QueryBuilder.select()
                .from("audit", "user_event_by_module_user_timestamp")
                .where(QueryBuilder.eq("module", module))
                .and(QueryBuilder.eq("user_id", userId));
        else {
            statement = QueryBuilder.select()
                .from("audit", "user_event_by_module_user_organisation_timestamp")
                .where(QueryBuilder.eq("module", module))
                .and(QueryBuilder.eq("user_id", userId))
                .and(QueryBuilder.eq("organisation_id", organisationId));

        }

        statement = statement
            .and(QueryBuilder.gte("timestamp", startDate))
            .and(QueryBuilder.lte("timestamp", endDate));

        Session session = getSession();
        ResultSet resultSet = session.execute(statement);

        List<UserEvent> userEvents = new ArrayList<>();
        int remaining = resultSet.getAvailableWithoutFetching();
        for (Row row : resultSet) {
            userEvents.add(new UserEvent(row));
            if (--remaining == 0) break;
        }

        return userEvents;
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
