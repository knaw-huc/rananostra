package nl.knaw.huc.rananostra.rest;

import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrogResourceTest {
  @Test
  void xmlInputValidation() {
    FrogResource resource = new FrogResource("", 12345); // host and port not used

    FrogResource.Args working = new FrogResource.Args();
    working.xml = "<p/>";
    working.xpath = "//p";
    working.starttag = "start";
    working.endtag = "end";

    WebApplicationException e = assertThrows(WebApplicationException.class, () -> {
      FrogResource.Args args = copy(working);
      args.xml = "";
      resource.applyXML(args);
    });
    assertEquals(BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());

    assertThrows(WebApplicationException.class, () -> {
      FrogResource.Args args = copy(working);
      args.xpath = "";
      resource.applyXML(args);
    });
    assertEquals(BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
  }

  private FrogResource.Args copy(FrogResource.Args args) {
    FrogResource.Args copy = new FrogResource.Args();
    copy.endtag = args.endtag;
    copy.namespaces = args.namespaces;
    copy.starttag = args.starttag;
    copy.xml = args.xml;
    copy.xpath = args.xpath;
    return copy;
  }
}
