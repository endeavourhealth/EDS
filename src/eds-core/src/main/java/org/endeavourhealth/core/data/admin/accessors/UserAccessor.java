package org.endeavourhealth.core.data.admin.accessors;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.endeavourhealth.core.data.admin.models.*;

import java.util.UUID;

@Accessor
public interface UserAccessor {
    @Query("SELECT * FROM admin.end_user_by_email where email = :email")
    EndUser getEndUserByEmail(@Param("email") String email);

    @Query("SELECT * FROM admin.end_user_pwd_by_user_id WHERE end_user_id = :end_user_id")
    EndUserPwd getEndUserPwdById(@Param("end_user_id") UUID endUserId);

}
