package nl.knaw.huc.rananostra.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huc.rananostra.FrogSocketClient;
import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.Map;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
public class FrogResource {
  private final FrogSocketClient frog;

  public FrogResource(String host, int port) {
    frog = new FrogSocketClient(host, port);
  }

  static class Args {
    @JsonProperty
    Map<String, String> namespaces;

    @JsonProperty
    @NotEmpty
    String xml;

    @JsonProperty
    String xpath;

    @JsonProperty
    String starttag;

    @JsonProperty
    String endtag;
  }

  @Path("xml")
  @Produces(MediaType.APPLICATION_XML)
  @POST
  public String applyXML(Args args) throws Exception {
    return frog.applyXML(args.xml, args.xpath, args.namespaces, args.starttag, args.endtag);
  }
}
