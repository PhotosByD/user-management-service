package si.photos.by.d.user.api.v1.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("demo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class DemoResource {
    @GET
    @Path("info")
    public Response info() {
        JsonObject json = Json.createObjectBuilder()
                .add("clani", Json.createArrayBuilder().add("ds3105").add("df4255"))
                .add("opis_projekta", "Projekt implementira aplikacijo, ki uporabnikom dovoljuje nalaganje" +
                        "fotografij na splet, z njimi tvoriti albume, dodajati komentarje pod slike, jih deliti z drugimi" +
                        " ...")
                .add("mikrostoritve", Json.createArrayBuilder().add(""))
                .add("github", Json.createArrayBuilder().add("https://github.com/PhotosByD/user-management-service").add("https://github.com/PhotosByD/photo-management-service"))
                .add("travis", Json.createArrayBuilder().add("https://travis-ci.org/PhotosByD/user-management-service").add("https://travis-ci.org/PhotosByD/photo-management-service"))
                .add("dockerhub", Json.createArrayBuilder().add("https://hub.docker.com/r/photosbyd/user-service/").add("https://hub.docker.com/r/photosbyd/photo-service/"))
                .build();

        return Response.ok(json.toString()).build();
    }
}
