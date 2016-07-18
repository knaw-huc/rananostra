package nl.knaw.huygens.pergamon;

import opennlp.tools.util.Span;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StanfordNERTest {
  private final String text = "Hello, John!";

  @Test
  public void getModel() {
    String model = "edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz";
    InputStream res = this.getClass().getClassLoader().getResourceAsStream(model);
    System.out.println(res);
  }

  @Test
  public void smokeTest() throws Exception {
    Tagger ner = new StanfordNER();

    // Entity in the middle of text
    List<Span> names = ner.apply(text, Arrays.asList(
      new Span(0, 5), new Span(5, 6), new Span(7, 11), new Span(11, 12)
    ));
    hasJohn(names);

    // Entity at the start of text
    names = ner.apply(text, Arrays.asList(
      new Span(7, 11), new Span(11, 12)
    ));
    hasJohn(names);

    // Entity at the end of text
    names = ner.apply(text, Arrays.asList(
      new Span(0, 5), new Span(5, 6), new Span(7, 11)
    ));
    hasJohn(names);

    // Entity only
    names = ner.apply(text, Arrays.asList(new Span(7, 11)));
  }

  private void hasJohn(List<Span> names) {
    assertEquals(1, names.size());
    assertEquals("PERSON", names.get(0).getType());
    assertEquals("John", names.get(0).getCoveredText(text).toString());
  }
}
