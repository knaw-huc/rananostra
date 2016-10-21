package nl.knaw.huygens.pergamon;

import opennlp.tools.util.Span;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class FrogSocketClientTest {
  // These tests need a running Frog TCP server.
  // TODO start Frog automatically?
  //private static final int port = Integer.parseInt(System.getenv("FROG_PORT"));
  private static final int port = 9999;

  private final FrogSocketClient frog;

  public FrogSocketClientTest() throws IOException {
    frog = new FrogSocketClient("localhost", port);
  }

  private static final String NOSERVER = "needs running Frog server";

  @Ignore(NOSERVER)
  @Test
  public void testFrog() throws Exception {
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

  @Ignore(NOSERVER)
  @Test
  public void testFrogAndOpenNLP() throws Exception {
    String text = "Henk en Gerard zijn namen van personen.";
    List<Span> names = frog.apply(text);
    assertEquals(2, names.size());
    assertEquals("Henk", names.get(0).getCoveredText(text));
    assertEquals("Gerard", names.get(1).getCoveredText(text));
  }

  @Ignore(NOSERVER)
  @Test(expected = IllegalArgumentException.class)
  public void testEmptyToken() throws Exception {
    frog.apply("", asList(new Span(0, 0)));
  }

  @Ignore(NOSERVER)
  @Test(expected = IllegalArgumentException.class)
  public void testCrossingSpans() throws Exception {
    frog.apply("Hallo!", asList(new Span(0, 4), new Span(3, 6)));
  }

  @Ignore(NOSERVER)
  @Test(expected = IllegalArgumentException.class)
  public void testUnsortedSpans() throws Exception {
    frog.apply("Hallo wereld!", asList(new Span(6, 13), new Span(0, 5)));
  }

  @Ignore(NOSERVER)
  @Test(expected = IllegalArgumentException.class)
  public void testEOTAsToken() throws Exception {
    frog.apply("Wat is een EOT ?");
  }
}
