package nl.knaw.huc.rananostra.rest;

import nl.knaw.huc.rananostra.FrogSocketClient;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrogResourceTest {
  @Test
  void xmlInputValidation() {
    FrogResource resource = new FrogResource("", 12345); // host and port not used

    FrogSocketClient.XMLOptions working = new FrogSocketClient.XMLOptions("<p/>", "//p", null, "start", "end", null, null);

    WebApplicationException e = assertThrows(WebApplicationException.class, () -> {
      FrogSocketClient.XMLOptions args = new FrogSocketClient.XMLOptions(working);
      args.xml = "";
      resource.applyXML(args);
    });
    assertEquals(BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());

    assertThrows(WebApplicationException.class, () -> {
      FrogSocketClient.XMLOptions args = new FrogSocketClient.XMLOptions(working);
      args.xpath = "";
      resource.applyXML(args);
    });
    assertEquals(BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
  }
}
