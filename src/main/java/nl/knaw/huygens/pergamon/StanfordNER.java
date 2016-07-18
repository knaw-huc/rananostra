package nl.knaw.huygens.pergamon;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import opennlp.tools.util.Span;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StanfordNER implements Tagger {
  private static class SpanHavingWord extends Span implements HasWord {
    private String word;

    public SpanHavingWord(String text, Span span) {
      super(span.getStart(), span.getEnd());
      // Since word() needs to return String, we need to use substring() here
      // even though since Java 7 it copies. Oh well.
      word = text.substring(span.getStart(), span.getEnd());
    }

    @Override
    public String word() {
      return word;
    }

    @Override
    public void setWord(String word) {
      this.word = word;
    }
  }

  private final CRFClassifier<? extends CoreLabel> crf;

  /**
   * Wrap Stanford NER's default classifier (3-class English NER at the moment of writing).
   */
  public StanfordNER() {
    crf = CRFClassifier.getDefaultClassifier();
  }

  /**
   * Construct from existing CRFClassifier.
   */
  public StanfordNER(CRFClassifier<? extends CoreLabel> crf) {
    this.crf = crf;
  }

  @Override
  public List<Span> apply(String text, List<Span> tokens) throws Exception {
    List<? extends CoreLabel> labeled = crf.classifySentence(tokens.stream()
      .map(span -> new SpanHavingWord(text, span))
      .collect(Collectors.toList()));

    if (labeled.size() != tokens.size()) {
      throw new RuntimeException(String.format("sentence length mismatch: expected %d, got %d",
        labeled.size(), tokens.size()));
    }

    // Left-to-right longest match reconstruction of the name spans.
    List<Span> result = new ArrayList<>();
    Span current = null;  // Current name; null if not inside a name.
    for (int i = 0; i < labeled.size(); i++) {
      String label = labeled.get(i).get(CoreAnnotations.AnswerAnnotation.class);
      if ("O".equals(label)) {
        if (current != null) {
          result.add(current);
          current = null;
        }
      } else {
        if (current != null && !label.equals(current.getType())) {
          result.add(current);
          current = null;
        }
        int start = current == null ? tokens.get(i).getStart() : current.getStart();
        current = new Span(start, tokens.get(i).getEnd(), label);
      }
    }
    if (current != null) {
      result.add(current);
    }

    return result;
  }
}
