package nl.knaw.huc.rananostra;

import opennlp.tools.util.Span;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class TagsTest {
  @Test
  public void basic() {
    List<Span> entities = asList(new Span(5, 10, "per"), new Span(12, 14, "ORG"));
    List<Span> tokens = asList(
      new Span(0, 5), new Span(5, 8), new Span(8, 10),
      new Span(10, 12), new Span(12, 14), new Span(14, 20));

    List<Span> bio = Tags.toBIO(entities, tokens);
    List<Span> expected = asList(
      new Span(0, 5, "O"), new Span(5, 8, "B-per"), new Span(8, 10, "I-per"),
      new Span(10, 12, "O"), new Span(12, 14, "B-ORG"), new Span(14, 20, "O"));
    assertEquals(expected, bio);
  }

  @Test(expected = IllegalStateException.class)
  public void outofrange() {
    List<Span> entities = asList(new Span(0, 3, "foo"), new Span(1, 4, "foo"));
    Tags.toBIO(entities, singletonList(new Span(0, 4)));
  }
}
