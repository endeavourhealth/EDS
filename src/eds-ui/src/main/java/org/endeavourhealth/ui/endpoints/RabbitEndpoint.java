package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.queueing.RabbitConfig;
import org.endeavourhealth.core.queueing.RoutingOverride;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rabbit")
public final class RabbitEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitEndpoint.class);

	private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Rabbit);

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
	@Timed(absolute = true, name="RabbitEndpoint.GetRoutings")
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
	@Timed(absolute = true, name="RabbitEndpoint.PostRoutings")
	@Path("/routings")
	public Response saveRoutings(@Context SecurityContext sc, String routings) throws Exception {
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
	@Timed(absolute = true, name="RabbitEndpoint.GetRabbitNodes")
	@Path("/nodes")
	public Response getRabbitNodes(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Nodes");

		/*String rabbitConfigJson = ConfigManager.getConfiguration("rabbit");
		JsonNode rabbitConfig = ObjectMapperPool.getInstance().readTree(rabbitConfigJson);*/

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
	@Timed(absolute = true, name="RabbitEndpoint.PingRabbitNodes")
	@Path("/ping")
	public Response pingRabbitNode(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Ping time",
				"Address", address);

		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		WebTarget resource = client.target(RabbitConfig.getInstance().getManagementProtocol() + "://"+address+"/api/cluster-name");
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
			LOG.error("", e);
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
	@Timed(absolute = true, name="RabbitEndpoint.GetRabbitQueues")
	@Path("/queues")
	public Response getRabbitQueues(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Queues",
				"Address", address);

		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		WebTarget resource = client.target(RabbitConfig.getInstance().getManagementProtocol() + "://"+address+"/api/queues");
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
	@Timed(absolute = true, name="RabbitEndpoint.GetRabbitExchanges")
	@Path("/exchanges")
	public Response getRabbitExchanges(@Context SecurityContext sc, @QueryParam("address") String address) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Exchanges",
				"Address", address);

		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		WebTarget resource = client.target(RabbitConfig.getInstance().getManagementProtocol() + "://"+address+"/api/exchanges");
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
	@Timed(absolute = true, name="RabbitEndpoint.GetRabbitBindings")
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
	@Timed(absolute = true, name="RabbitEndpoint.SynchronizeRabbit")
	@Path("/synchronize")
	@RequiresAdmin
	public Response synchronizeRabbit(@Context SecurityContext sc, String address) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
				"Bindings (Sync)",
				"Address", address);

		//String[] pipelines = {"EdsInbound", "EdsProtocol", "EdsTransform", "EdsResponse", "EdsSubscriber"};

		super.setLogbackMarkers(sc);

		// Load current bindings
		List<JsonRabbitBinding> currentBindings = getCurrentRabbitBindings(address);

		// Load config
		List<JsonRouteGroup> allRoutings = getConfiguredBindings();

		//hash by exchange name
		Map<String, List<JsonRouteGroup>> map = new HashMap<>();
		for (JsonRouteGroup r: allRoutings) {
			List<JsonRouteGroup> list = map.get(r.getExchangeName());
			if (list == null) {
				list = new ArrayList<>();
				map.put(r.getExchangeName(), list);
			}
			list.add(r);
		}

		for (String pipeline: map.keySet()) {
			List<JsonRouteGroup> routings = map.get(pipeline);

			// Declare exchanges
			declareAllExchanges(address, pipeline);

			// Declare (config) queues
			declareAllQueues(address, pipeline, routings);

			// Bind (config) queues to DLE
			bindQueuesToExchange(address, pipeline + "-DLE", pipeline, routings);

			// Remove all bindings from main exchange (DLE now routes to queues based on new config)
			removeBindingsFromMainExchange(address, pipeline, currentBindings);

			// Bind (config) to main exchange (main exchange now routes to queues based on new config)
			bindQueuesToExchange(address, pipeline, pipeline, routings);

			// Remove (config) bindings from DLE
			removeBindingsFromDLEExchange(address, pipeline + "-DLE", pipeline, routings);

			// Determine queues to remove (unbound)

			// Wait for any unbound queues to drain

			// Remove (now empty) unbound queues

			// (Shutdown readers of unbound queues????)

			// (Startup readers of queues without readers????)
		}

		/*for (String pipeline : pipelines) {
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
		}*/

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

		WebTarget resource = client.target(RabbitConfig.getInstance().getManagementProtocol() + "://"+address+"/api/bindings");
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
		WebTarget resource = client.target(RabbitConfig.getInstance().getManagementProtocol() + "://" + address + "/api/exchanges/%2f/"+pipeline+"-DLE");
		Invocation.Builder request = resource.request();

		JsonRabbitExchangeOptions optionsJson = new JsonRabbitExchangeOptions();
		optionsJson.setType("fanout");
		optionsJson.setAuto_delete(false);
		optionsJson.setDurable(true);

		Response response = request.put(Entity.json(optionsJson));
		Response.StatusType status = response.getStatusInfo();
		if (status.getFamily() != Response.Status.Family.SUCCESSFUL) {
			throw new Exception("Unable to declare the dead letter exchange, URI: " + resource.getUri().toString() +
                    ", HTTP code: " + status.getStatusCode() + " " + status.getReasonPhrase());
		}
		response.close();

		// Exchange
		resource = client.target(RabbitConfig.getInstance().getManagementProtocol() + "://" + address + "/api/exchanges/%2f/"+pipeline);
		request = resource.request();

		optionsJson = new JsonRabbitExchangeOptions();
		optionsJson.setType("topic");
		optionsJson.setAuto_delete(false);
		optionsJson.setDurable(true);
		optionsJson.getArguments().put("alternate-exchange", pipeline+"-DLE");

		response = request.put(Entity.json(optionsJson));
		status = response.getStatusInfo();
		if (status.getFamily() != Response.Status.Family.SUCCESSFUL) {
			throw new Exception("Unable to declare the " + pipeline + " exchange, URI: " + resource.getUri().toString() +
                    ", HTTP code: " + status.getStatusCode() + " " + status.getReasonPhrase());
		}
		response.close();
		client.close();
	}

	private void declareAllQueues(String address, String queuePrefix, List<JsonRouteGroup> routeGroups) throws Exception {
		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		//declare a queue for each
		for (JsonRouteGroup routeGroup : routeGroups) {
			String queueName = queuePrefix + "-" + routeGroup.getRouteKey();
			declareQueue(client, address, queueName);
		}
		client.close();
	}

	private static void declareQueue(Client client, String address, String queueName) throws Exception {
		WebTarget resource = client.target(RabbitConfig.getInstance().getManagementProtocol() + "://" + address + "/api/queues/%2f/" + queueName);
		Invocation.Builder request = resource.request();

		JsonRabbitQueueOptions optionsJson = new JsonRabbitQueueOptions();
		optionsJson.setAuto_delete(false);
		optionsJson.setDurable(true);

		Response response = request.put(Entity.json(optionsJson));
		Response.StatusType status = response.getStatusInfo();
		if (status.getFamily() != Response.Status.Family.SUCCESSFUL) {
			throw new Exception("Unable do declare the queue, HTTP code " + status.getStatusCode() + " " + status.getReasonPhrase());
		}
		response.close();
	}

	private void bindQueuesToExchange(String address, String exchange, String queuePrefix, List<JsonRouteGroup> routeGroups) throws Exception {
		Client client = ClientBuilder.newClient();
		client.register(rabbitAuth);

		for (JsonRouteGroup routeGroup : routeGroups) {
			WebTarget resource = client.target(RabbitConfig.getInstance().getManagementProtocol() + "://" + address + "/api/bindings/%2f/e/"+exchange+"/q/"+queuePrefix + "-" + routeGroup.getRouteKey());
			Invocation.Builder request = resource.request();

			JsonRabbitBindingOptions optionsJson = new JsonRabbitBindingOptions();
			optionsJson.setRouting_key(routeGroup.getRouteKey());

			Response response = request.post(Entity.json(optionsJson));
			Response.StatusType status = response.getStatusInfo();
			if (status.getFamily() != Response.Status.Family.SUCCESSFUL) {
				throw new Exception("Unable do declare the queue, HTTP code " + status.getStatusCode() + " " + status.getReasonPhrase());
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

		WebTarget resource = client.target(RabbitConfig.getInstance().getManagementProtocol() + "://" + address + "/api/bindings/%2f/e/" + exchange + "/q/" + queue + "/"+routingKey);
		Invocation.Builder request = resource.request();

		Response response = request.delete();
		Response.StatusType status = response.getStatusInfo();
		if (status.getFamily() != Response.Status.Family.SUCCESSFUL) {
			throw new Exception("Unable do declare the queue, HTTP code " + status.getStatusCode() + " " + status.getReasonPhrase());
		}
		response.close();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	/*@Consumes(MediaType.APPLICATION_JSON)*/
	@Timed(absolute = true, name="RabbitEndpoint.GetRoutingOverrides")
	@Path("/overrides")
	public Response getRoutingOverrides(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "GetRoutingOverrides");

		List<RoutingOverride> ret = new ArrayList<>();

		String json = ConfigManager.getConfiguration("routing_overrides");
		if (!Strings.isNullOrEmpty(json)) {
			RoutingOverride[] arr = ObjectMapperPool.getInstance().readValue(json, RoutingOverride[].class);
			for (RoutingOverride o: arr) {
				ret.add(o);
			}
		}

		return Response
				.ok()
				.entity(ret)
				.build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(absolute = true, name="RabbitEndpoint.SaveRoutingOverrides")
	@Path("/overrides")
	public Response saveRoutingOverrides(@Context SecurityContext sc, String overridesJson) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "SaveRoutingOverrides");

		ConfigManager.setConfiguration("routing_overrides", overridesJson);

		return Response
				.ok()
				.build();
	}
}
