package nl.knaw.huc.rananostra;

import opennlp.tools.util.Span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * Static utilities for working with tags.
 */
public class Tags {
  /**
   * Converts a list of entity spans to a list spans with BIO tags.
   *
   * @param entities Spans denoting entity locations, sorted by start index.
   * @param tokens   Spans denoting token boundaries, sorted by start index.
   * @return A list of spans, of length {@code tokens.size()}, with BIO tags indicating entity locations.
   */
  public static List<Span> toBIO(List<Span> entities, List<Span> tokens) {
    List<Span> bio = new ArrayList<>(tokens.size());
    for (int i = 0; i < tokens.size(); i++) {
      bio.add(null);
    }

    for (Span e : entities) {
      int pos = Collections.binarySearch(tokens, e, comparing(Span::getStart));
      if (pos < 0) {
        pos = -pos - 1;
      }
      if (pos >= tokens.size()) {
        throw new IllegalStateException("entity span out of range");
      }

      boolean begin = true;
      while (true) {
        Span tok = tokens.get(pos);
        if (tok.getStart() >= e.getEnd()) {
          break;
        }
        if (bio.get(pos) != null) {
          throw new IllegalStateException("overlapping entities");
        }
        String type = (begin ? "B-" : "I-") + e.getType();
        bio.set(pos, new Span(tok.getStart(), tok.getEnd(), type));
        begin = false;
        if (++pos >= tokens.size()) {
          break;
        }
      }
    }

    for (int i = 0; i < bio.size(); i++) {
      if (bio.get(i) == null) {
        Span tok = tokens.get(i);
        bio.set(i, new Span(tok.getStart(), tok.getEnd(), "O"));
      }
    }

    return bio;
  }
}
