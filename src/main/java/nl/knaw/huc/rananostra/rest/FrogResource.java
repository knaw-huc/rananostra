package nl.knaw.huc.rananostra.rest;

import nl.knaw.huc.rananostra.FrogSocketClient;
import nu.xom.ParsingException;
import nu.xom.XPathException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
public class FrogResource {
  private final FrogSocketClient frog;

  FrogResource(String host, int port) {
    frog = new FrogSocketClient(host, port);
  }

  @Path("xml")
  @Produces(MediaType.APPLICATION_XML)
  @POST
  public String applyXML(FrogSocketClient.XMLOptions args) throws Exception {
    try {
      return frog.applyXML(args);
    } catch (ParsingException e) {
      throw new WebApplicationException("XML parsing error: " + e.getMessage(), BAD_REQUEST);
    } catch (XPathException e) {
      throw new WebApplicationException("XPath error: " + e.getMessage(), BAD_REQUEST);
    }
  }
}
