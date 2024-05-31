package ibm.qa.rest;

import ibm.qa.service.PlanetService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/universe")
public class UniverseResource {

    @Inject
    PlanetService planetService;

    @GET
    @Path("/planets")
    @Produces(MediaType.APPLICATION_JSON)
    public Response planets() {
        return Response.ok(planetService.planets()).build();
    }

    @GET
    @Path("/planets/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlanet(@PathParam(value = "name") String name) {
        return Response.ok(planetService.getPlanet(name)).build();
    }

    @GET
    @Path("/planets/find/{distance}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findPlanet(@PathParam(value = "distance") Double distance) {
        return Response.ok(planetService.findPlanet(distance)).build();
    }

}
