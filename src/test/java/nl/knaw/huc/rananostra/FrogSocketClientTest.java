package nl.knaw.huc.rananostra;

import opennlp.tools.util.Span;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// These tests need a running Frog TCP server on localhost.
// Configure one with the environment variables RANA_TEST_PORT.
@EnabledIfEnvironmentVariable(named = "RANA_TEST_PORT", matches = "[0-9]+")
class FrogSocketClientTest {
  private final FrogSocketClient frog;

  FrogSocketClientTest() {
    int port = Integer.parseInt(System.getenv("RANA_TEST_PORT"));
    frog = new FrogSocketClient("localhost", port);
  }

  @Test
  void testFrog() throws Exception {
    String text = "Henk staat aan het begin van de zin.";
    int[] bounds = {0, 4, 5, 10, 11, 15, 15, 18, 19, 24, 25, 28, 29, 31, 32, 35, 35, 36};
    List<Span> tokens =
      IntStream.range(0, bounds.length / 2)
               .mapToObj(i -> new Span(bounds[i * 2], bounds[i * 2 + 1]).trim(text))
               .collect(Collectors.toList());

    List<Span> names = frog.apply(text, tokens);
    assertEquals(1, names.size());
    assertEquals("Henk", names.get(0).getCoveredText(text));
  }

  @Test
  void testFrogAndOpenNLP() throws Exception {
    String text = "Henk en Gerard zijn namen van personen.";
    List<Span> names = frog.apply(text);
    assertEquals(2, names.size());
    assertEquals("Henk", names.get(0).getCoveredText(text));
    assertEquals("Gerard", names.get(1).getCoveredText(text));
  }

  @Test
  void testInvalidInput() {
    // Empty token.
    assertThrows(IllegalArgumentException.class, () ->
      frog.apply("", asList(new Span(0, 0))));

    // Crossing spans.
    assertThrows(IllegalArgumentException.class, () ->
      frog.apply("Hallo!", asList(new Span(0, 4), new Span(3, 6))));

    // Unsorted spans.
    assertThrows(IllegalArgumentException.class, () ->
      frog.apply("Hallo wereld!", asList(new Span(6, 13), new Span(0, 5))));

    // EOT as token.
    assertThrows(IllegalArgumentException.class, () ->
      frog.apply("Wat is een EOT ?"));
  }

  // Assert that we can run apply(String) in parallel.
  @Test
  void parallel() throws Exception {
    List<String> list = asList(
      "Dit is een korte zin.",
      "Dit is een zin over Henk en Fatima.",
      "Dit is nog een zin, met de naam van Bert erin.",
      "Dit is een heel andere zin");

    list.parallelStream().forEach(s -> {
      try {
        List<Span> result = frog.apply(s);
        if (!s.contains("Henk") && !s.contains("Bert")) {
          assertTrue(result.isEmpty());
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private String applyXML(String doc) throws Exception {
    doc = frog.applyXML(new FrogSocketClient.XMLOptions(
      doc, "//p", null, "start", "end", "class"));
    // Strip off XML declaration.
    String[] parts = doc.split("\n");
    assertEquals(2, parts.length);
    assertEquals("<?xml version=\"1.0\"?>", parts[0]);
    doc = parts[1];
    return doc;
  }

  @Test
  void xml() throws Exception {
    String doc = applyXML("<p>Bert <br/>Haanstra</p>");
    assertEquals("<p><start class=\"per\" />Bert <br />Haanstra<end /></p>", doc);

    doc = applyXML("<p>Een film van Bert <br/>Haan<!--comment-->stra.</p>");
    assertEquals("<p>Een film van <start class=\"per\" />Bert <br />Haan<!--comment-->stra<end />.</p>", doc);

    doc = applyXML("<p>Een film van Bert <br/>Haanstra.</p>");
    assertEquals("<p>Een film van <start class=\"per\" />Bert <br />Haanstra<end />.</p>", doc);
  }
}
