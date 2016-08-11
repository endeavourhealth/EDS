package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Audit;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.endeavourhealth.ui.framework.config.ConfigSerializer;
import org.endeavourhealth.ui.framework.config.models.RabbitmqManagement;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.ui.json.*;
import org.endeavourhealth.core.security.SecurityUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.*;

@Path("/dashboard")
public final class DashboardEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(DashboardEndpoint.class);
	private static final Random rnd = new Random();

	private final HttpAuthenticationFeature rabbitAuth;
	{
		RabbitmqManagement authConfig = ConfigSerializer.getConfig().getRabbitmqManagement();
		rabbitAuth = HttpAuthenticationFeature.basic(authConfig.getUsername(), authConfig.getPassword());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/getRecentDocuments")
	public Response getRecentDocuments(@Context SecurityContext sc, @QueryParam("count") int count) throws Exception {
		super.setLogbackMarkers(sc);

		UUID userUuid = SecurityUtils.getCurrentUserId(sc);
		UUID orgUuid = getOrganisationUuidFromToken(sc);

		LOG.trace("getRecentDocuments {}", count);

		List<JsonFolderContent> ret = new ArrayList<>();

		LibraryRepository repository = new LibraryRepository();

		Iterable<Audit> audit = repository.getAuditByOrgAndDateDesc(orgUuid);
		for (Audit auditItem: audit) {
			Iterable<ActiveItem> activeItems = repository.getActiveItemByAuditId(auditItem.getId());
			for (ActiveItem activeItem: activeItems) {
				if (activeItem.getIsDeleted()!=null && activeItem.getIsDeleted()==false) {
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


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/rabbitNodes")
	public Response getRabbitNodes(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);

		ConfigurationResource rabbitNodes = new ConfigurationRepository().getByKey(ConfigurationRepository.RABBIT_NODES);

		List<JsonRabbitNode> ret = new ArrayList<>();
		for (String node : rabbitNodes.getConfigurationData().split(",", -1))
			ret.add(new JsonRabbitNode(node, 0));

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/rabbitNode/ping")
	public Response pingRabbitNode(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);

		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		WebTarget resource = client.target("http://"+address+"/api/cluster-name");
		Invocation.Builder request = resource.request();

		int ping = -1;
		try {
			long startTime = System.currentTimeMillis();
			Response response = request.get();
			long endTime = System.currentTimeMillis();

			if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL)
				ping = Math.toIntExact(endTime - startTime);
			response.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		client.close();

		JsonRabbitNode ret = new JsonRabbitNode(address, ping);
		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/rabbitNode/queues")
	public Response getRabbitQueues(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);

		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		WebTarget resource = client.target("http://"+address+"/api/queues");
		Invocation.Builder request = resource.request();

		Response response = request.get();
		String ret = null;
		if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
			ret = response.readEntity(String.class);
		}

		response.close();
		client.close();

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/rabbitNode/exchanges")
	public Response getRabbitExchanges(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);

		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		WebTarget resource = client.target("http://"+address+"/api/exchanges");
		Invocation.Builder request = resource.request();

		Response response = request.get();
		String ret = null;
		if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
			ret = response.readEntity(String.class);
		}

		response.close();
		client.close();

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/rabbitNode/bindings")
	public Response getRabbitBindings(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);

		String ret = getRabbitBindingsJson(address);

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}


	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/rabbitNode/synchronize")
	@RequiresAdmin
	public Response synchronizeRabbit(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		String[] pipelines = {"EdsInbound", "EdsProtocol", "EdsTransform", "EdsResponse", "EdsSubscriber"};

		super.setLogbackMarkers(sc);

		// Load current bindings
		List<JsonRabbitBinding> currentBindings = getCurrentRabbitBindings(address);

		// Load config
		List<JsonRouteGroup> configuredBindings = getConfiguredBindings();

		for (String pipeline : pipelines) {
			// Declare (config) queues
			declareAllQueues(address, pipeline, configuredBindings);

			// Bind (config) queues to DLE
			bindQueuesToExchange(address, pipeline + "-DLE", pipeline, configuredBindings);

			// Remove all bindings from main exchange (DLE now routes to queues based on new config)
			removeBindingsFromMainExchange(address, pipeline, currentBindings);

			// Bind (config) to main exchange (main exchange now routes to queues based on new config)
			bindQueuesToExchange(address, pipeline, pipeline, configuredBindings);

			// Remove (config) bindings from DLE
			removeBindingsFromDLEExchange(address, pipeline + "-DLE", pipeline, configuredBindings);

			// Determine queues to remove (unbound)

			// Wait for any unbound queues to drain

			// Remove (now empty) unbound queues

			// (Shutdown readers of unbound queues????)

			// (Startup readers of queues without readers????)
		}

		String ret = getRabbitBindingsJson(address);

		clearLogbackMarkers();

		return Response
				.ok(ret)
				.build();
	}

	private String getRabbitBindingsJson(String address) {
		String json = null;

		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("guest", "guest");
		Client client = ClientBuilder.newClient();
		client.register(feature);

		WebTarget resource = client.target("http://"+address+"/api/bindings");
		Invocation.Builder request = resource.request();

		Response response = request.get();
		if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
			json = response.readEntity(String.class);
		}

		response.close();
		client.close();

		return json;
	}

	private List<JsonRabbitBinding> getCurrentRabbitBindings(String address) throws IOException {
		List<JsonRabbitBinding> bindings = new ArrayList<>();
		String json = getRabbitBindingsJson(address);
		if (json != null) {
			bindings = new ObjectMapper().readValue(json, new TypeReference<List<JsonRabbitBinding>>(){});
		}
		return bindings;
	}

	private List<JsonRouteGroup> getConfiguredBindings() throws IOException {
		List<JsonRouteGroup> bindings = new ArrayList<>();
		ConfigurationResource configurationResource = new ConfigurationRepository().getByKey(UUID.fromString("b9b14e26-5a52-4f36-ad89-f01e465c1361"));
		if (configurationResource != null) {
			bindings = new ObjectMapper().readValue(configurationResource.getConfigurationData(), new TypeReference<List<JsonRouteGroup>>(){});
		}

		return bindings;
	}

	private void declareAllQueues(String address, String queuePrefix, List<JsonRouteGroup> routeGroups) throws Exception {
		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		for (JsonRouteGroup routeGroup : routeGroups) {
			WebTarget resource = client.target("http://" + address + "/api/queues/%2f/"+queuePrefix + "-" + routeGroup.getRouteKey());
			Invocation.Builder request = resource.request();

			JsonRabbitQueueOptions optionsJson = new JsonRabbitQueueOptions();
			optionsJson.setAuto_delete(false);
			optionsJson.setDurable(true);

			Response response = request.put(Entity.json(optionsJson));
			if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
				throw new Exception("Unable do declare the queue");
			}
			response.close();
		}
		client.close();
	}

	private void bindQueuesToExchange(String address, String exchange, String queuePrefix, List<JsonRouteGroup> routeGroups) throws Exception {
		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		for (JsonRouteGroup routeGroup : routeGroups) {
			WebTarget resource = client.target("http://" + address + "/api/bindings/%2f/e/"+exchange+"/q/"+queuePrefix + "-" + routeGroup.getRouteKey());
			Invocation.Builder request = resource.request();

			JsonRabbitBindingOptions optionsJson = new JsonRabbitBindingOptions();
			optionsJson.setRouting_key(routeGroup.getRouteKey());

			Response response = request.post(Entity.json(optionsJson));
			if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
				throw new Exception("Unable do declare the queue");
			}
			response.close();
		}
		client.close();
	}

	private void removeBindingsFromMainExchange(String address, String exchange, List<JsonRabbitBinding> currentBindings) throws Exception {
		for (JsonRabbitBinding rabbitBinding : currentBindings) {
			if (exchange.equals(rabbitBinding.getSource())) {
				removeBindingFromExchange(address, exchange, rabbitBinding.getDestination(), rabbitBinding.getRouting_key());
			}
		}
	}

	private void removeBindingsFromDLEExchange(String address, String exchange, String queuePrefix, List<JsonRouteGroup> routeGroups) throws Exception {
		for (JsonRouteGroup routeGroup : routeGroups) {
			removeBindingFromExchange(address, exchange, queuePrefix+"-"+routeGroup.getRouteKey(), routeGroup.getRouteKey());
		}
	}

	private void removeBindingFromExchange(String address, String exchange, String queue, String routingKey) throws Exception {
		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		WebTarget resource = client.target("http://" + address + "/api/bindings/%2f/e/" + exchange + "/q/" + queue + "/"+routingKey);
		Invocation.Builder request = resource.request();

		Response response = request.delete();
		if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
			throw new Exception("Unable do declare the queue");
		}
		response.close();
	}
}
