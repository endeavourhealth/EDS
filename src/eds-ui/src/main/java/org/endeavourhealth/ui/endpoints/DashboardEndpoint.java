package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Audit;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.JsonFolderContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/dashboard")
@Metrics(registry = "EdsRegistry")
public final class DashboardEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(DashboardEndpoint.class);
	private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Dashboard);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(absolute = true, name="EDS-UI.DashboardEndpoint.GetRecentDocuments")
	@Path("/getRecentDocuments")
	public Response getRecentDocuments(@Context SecurityContext sc, @QueryParam("count") int count) throws Exception {
		super.setLogbackMarkers(sc);

		UUID userUuid = SecurityUtils.getCurrentUserId(sc);
		UUID orgUuid = getOrganisationUuidFromToken(sc);
		userAudit.save(userUuid, orgUuid, AuditAction.Load,	"Recent Documents",
				"Count", count);

		LOG.trace("getRecentDocuments {}", count);

		List<JsonFolderContent> ret = new ArrayList<>();

		LibraryDalI repository = DalProvider.factoryLibraryDal();

		Iterable<Audit> audit = repository.getAuditByOrgAndDateDesc(orgUuid);
		for (Audit auditItem: audit) {
			Iterable<ActiveItem> activeItems = repository.getActiveItemByAuditId(auditItem.getId());
			for (ActiveItem activeItem: activeItems) {
				if (!activeItem.isDeleted()) {
					Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());

					JsonFolderContent content = new JsonFolderContent(activeItem, item, auditItem);
					ret.add(content);
				}
			}
		}


		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}
}
