package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeGeneralErrorDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeProtocolErrorDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeGeneralError;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeProtocolError;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.dashboardinformation.enums.HealthStatus;
import org.endeavourhealth.dashboardinformation.json.JsonApplicationInformation;
import org.endeavourhealth.dashboardinformation.json.JsonDashboardInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

@Path("/externalDashboard")
@Metrics(registry = "EdsRegistry")
public class ExternalDashboardEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalDashboardEndpoint.class);
    private static final ExchangeProtocolErrorDalI protocolRepository = DalProvider.factoryExchangeProtocolErrorDal();
    private static final ExchangeGeneralErrorDalI generalRepository = DalProvider.factoryExchangeGeneralErrorDal();
    private static final ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
    private static final ExchangeDalI exchangeRepository = DalProvider.factoryExchangeDal();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ExternalDashboardEndpoint.GetProtocolErrors")
    @Path("/getProtocolErrors")
    public Response getProtocolErrors(@Context SecurityContext sc) throws Exception {
        JsonDashboardInformation dashboardInformation = new JsonDashboardInformation();
        super.setLogbackMarkers(sc);

        LOG.trace("getProtocolErrors");

        List<ExchangeProtocolError> protocolErrors = protocolRepository.getProtocolErrors();

        String protocolHealth = HealthStatus.DANGER.getHealthStatus();
        if (protocolErrors.size() == 0) {
            protocolHealth = HealthStatus.SUCCESS.getHealthStatus();
        } else if (protocolErrors.size() < 5) {
            protocolHealth = HealthStatus.WARNING.getHealthStatus();
        }

        dashboardInformation.setAppHealth(protocolHealth);

        List<JsonApplicationInformation> appInfo = new ArrayList<>();

        for (ExchangeProtocolError err : protocolErrors) {
            Exchange exchange = exchangeRepository.getExchange(err.getExchangeId());
            Service service = serviceRepository.getById(exchange.getServiceId());
            String systemName = ExchangeAuditEndpoint.getSystemNameForId(exchange.getSystemId());

            appInfo.add(getGenericCountInformation("Service : " + service.getName() + " (" + systemName
                    + ")", null, HealthStatus.DANGER.getHealthStatus()));
        }

        if (protocolErrors.size() == 0) {
            appInfo.add(getGenericCountInformation("No protocol errors", null,HealthStatus.SUCCESS.getHealthStatus()) );
        }

        dashboardInformation.setApplicationInformation(appInfo);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(dashboardInformation)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ExternalDashboardEndpoint.GetGeneralErrors")
    @Path("/getGeneralErrors")
    public Response getGeneralErrors(@Context SecurityContext sc) throws Exception {
        JsonDashboardInformation dashboardInformation = new JsonDashboardInformation();
        super.setLogbackMarkers(sc);

        LOG.trace("getGeneralErrors");

        List<ExchangeGeneralError> generalErrors = generalRepository.getGeneralErrors();

        String protocolHealth = HealthStatus.DANGER.getHealthStatus();
        if (generalErrors.size() == 0) {
            protocolHealth = HealthStatus.SUCCESS.getHealthStatus();
        } else if (generalErrors.size() < 5) {
            protocolHealth = HealthStatus.WARNING.getHealthStatus();
        }

        dashboardInformation.setAppHealth(protocolHealth);

        List<JsonApplicationInformation> appInfo = new ArrayList<>();

        for (ExchangeGeneralError err : generalErrors) {
            Exchange exchange = exchangeRepository.getExchange(err.getExchangeId());
            Service service = serviceRepository.getById(exchange.getServiceId());
            String systemName = ExchangeAuditEndpoint.getSystemNameForId(exchange.getSystemId());

            appInfo.add(getGenericCountInformation("Service : " + service.getName() + " (" + systemName
                    + "). Error : " + err.getErrorMessage(), null, HealthStatus.PRIMARY.getHealthStatus()));
        }

        if (generalErrors.size() == 0) {
            appInfo.add(getGenericCountInformation("No general errors", null,HealthStatus.SUCCESS.getHealthStatus()) );
        }

        dashboardInformation.setApplicationInformation(appInfo);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(dashboardInformation)
                .build();
    }

    private JsonApplicationInformation getGenericCountInformation(String title, Long value, String status) throws Exception {
        JsonApplicationInformation information = new JsonApplicationInformation();
        information.setLabelText(title);
        if (value != null) {
            information.setCount(value);
        }
        information.setStatus(status);

        return information;
    }
}
