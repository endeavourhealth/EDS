package org.endeavourhealth.core.data.admin;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.admin.accessors.UserAccessor;
import org.endeavourhealth.core.data.admin.models.*;
import java.util.UUID;

public class UserRepository extends Repository {

    public EndUser getById(UUID id) {
        Mapper<EndUser> mapper = getMappingManager().mapper(EndUser.class);
        return mapper.get(id);
    }

    public EndUser getEndUserByEmail(String email) {
        UserAccessor accessor = getMappingManager().createAccessor(UserAccessor.class);
        return accessor.getEndUserByEmail(email);
    }

    public EndUserPwd getEndUserPwdById(UUID endUserId) {
        UserAccessor accessor = getMappingManager().createAccessor(UserAccessor.class);
        return accessor.getEndUserPwdById(endUserId);
    }

    public void updateEndUserPwd(EndUserPwd row) {
        Mapper<EndUserPwd> mapperEndUserPwd = getMappingManager().mapper(EndUserPwd.class);

        BatchStatement batch = new BatchStatement()
                .add(mapperEndUserPwd.saveQuery(row));

        getSession().execute(batch);
    }


}

