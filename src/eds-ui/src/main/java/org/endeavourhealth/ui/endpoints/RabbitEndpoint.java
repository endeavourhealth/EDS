package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.queueing.RabbitConfig;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.*;
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
import java.util.ArrayList;
import java.util.List;

@Path("/rabbit")
public final class RabbitEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitEndpoint.class);

	private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Rabbit);

	private final HttpAuthenticationFeature rabbitAuth;
	{
		try {
			String user = RabbitConfig.getInstance().getUsername();
			String pass = RabbitConfig.getInstance().getPassword();

			rabbitAuth = HttpAuthenticationFeature.basic(user, pass);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to load rabbit config", e);
		}

		/*try {
			String rabbitConfigJson = ConfigManager.getConfiguration("rabbit");
			JsonNode rabbitConfig = ObjectMapperPool.getInstance().readTree(rabbitConfigJson);

			rabbitAuth = HttpAuthenticationFeature.basic(rabbitConfig.get("username").asText(), rabbitConfig.get("password").asText());
		} catch (Exception e) {
			throw new IllegalStateException("Unable to load rabbit config", e);
		}*/

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/routings")
	public Response getRoutings(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Routings");

		String routings = ConfigManager.getConfiguration("routings");

		return Response
				.ok()
				.entity(routings)
				.build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/routings")
	public Response getRoutings(@Context SecurityContext sc, String routings) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Routings");

		ConfigManager.setConfiguration("routings", routings);

		return Response
				.ok()
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/nodes")
	public Response getRabbitNodes(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Nodes");

		String rabbitConfigJson = ConfigManager.getConfiguration("rabbit");
		JsonNode rabbitConfig = ObjectMapperPool.getInstance().readTree(rabbitConfigJson);

		List<JsonRabbitNode> ret = new ArrayList<>();

		String nodes = RabbitConfig.getInstance().getManagementNodes();
		for (String node : nodes.split(" *, *")) {
			ret.add(new JsonRabbitNode(node, 0));
		}

		/*String rabbitConfigJson = ConfigManager.getConfiguration("rabbit");
		JsonNode rabbitConfig = ObjectMapperPool.getInstance().readTree(rabbitConfigJson);

		List<JsonRabbitNode> ret = new ArrayList<>();
		for (String node : rabbitConfig.get("nodes").asText().split(",", -1))
			ret.add(new JsonRabbitNode(node, 0));*/


		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/ping")
	public Response pingRabbitNode(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Ping time",
				"Address", address);

		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		WebTarget resource = client.target("http://"+address+"/api/cluster-name");
		Invocation.Builder request = resource.request();

		int ping = -1;
		try {
			long startTime = System.currentTimeMillis();
			Response response = request.get();
			long endTime = System.currentTimeMillis();

			if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
				ping = Math.toIntExact(endTime - startTime);
			}
			response.close();
		}
		catch (Exception e) {
			// TODO : Cleanly handle downed rabbit node
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
	@Path("/queues")
	public Response getRabbitQueues(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Queues",
				"Address", address);

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
	@Path("/exchanges")
	public Response getRabbitExchanges(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Exchanges",
				"Address", address);

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
	@Path("/bindings")
	public Response getRabbitBindings(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Bindings",
				"Address", address);

		String ret = getRabbitBindingsJson(address);


		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}


	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	@Path("/synchronize")
	@RequiresAdmin
	public Response synchronizeRabbit(@Context SecurityContext sc, String address) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
				"Bindings (Sync)",
				"Address", address);

		String[] pipelines = {"EdsInbound", "EdsProtocol", "EdsTransform", "EdsResponse", "EdsSubscriber"};

		super.setLogbackMarkers(sc);

		// Load current bindings
		List<JsonRabbitBinding> currentBindings = getCurrentRabbitBindings(address);

		// Load config
		List<JsonRouteGroup> configuredBindings = getConfiguredBindings();

		for (String pipeline : pipelines) {
			// Declare exchanges
			declareAllExchanges(address, pipeline);

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

		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

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
			bindings = ObjectMapperPool.getInstance().readValue(json, new TypeReference<List<JsonRabbitBinding>>(){});
		}
		return bindings;
	}

	private List<JsonRouteGroup> getConfiguredBindings() throws IOException {
		List<JsonRouteGroup> bindings = new ArrayList<>();
		String routingsJson = ConfigManager.getConfiguration("routings");
		if (routingsJson != null) {
			bindings = ObjectMapperPool.getInstance().readValue(routingsJson, new TypeReference<List<JsonRouteGroup>>(){});
		}

		return bindings;
	}

	private void declareAllExchanges(String address, String pipeline) throws Exception {
		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		// DLE
		WebTarget resource = client.target("http://" + address + "/api/exchanges/%2f/"+pipeline+"-DLE");
		Invocation.Builder request = resource.request();

		JsonRabbitExchangeOptions optionsJson = new JsonRabbitExchangeOptions();
		optionsJson.setType("fanout");
		optionsJson.setAuto_delete(false);
		optionsJson.setDurable(true);

		Response response = request.put(Entity.json(optionsJson));
		if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
			throw new Exception("Unable do declare the dead letter exchange");
		}
		response.close();

		// Exchange
		resource = client.target("http://" + address + "/api/exchanges/%2f/"+pipeline);
		request = resource.request();

		optionsJson = new JsonRabbitExchangeOptions();
		optionsJson.setType("topic");
		optionsJson.setAuto_delete(false);
		optionsJson.setDurable(true);
		optionsJson.getArguments().put("alternate-exchange", pipeline+"-DLE");

		response = request.put(Entity.json(optionsJson));
		if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
			throw new Exception("Unable do declare the dead letter exchange");
		}
		response.close();
		client.close();
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
