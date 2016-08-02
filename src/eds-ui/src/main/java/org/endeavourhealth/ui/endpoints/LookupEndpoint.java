package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.ui.json.JsonSourceOrganisation;
import org.endeavourhealth.ui.json.JsonSourceOrganisationSet;
import org.endeavourhealth.ui.database.TableSaveMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/lookup")
public final class LookupEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(LookupEndpoint.class);

    private static final String ODS_CODE_DELIMITER = "|";




}
