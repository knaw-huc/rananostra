package nl.knaw.huygens.pergamon;

import opennlp.tools.util.Span;

import java.util.List;

/**
 * NER tagger.
 */
public interface Tagger {
  /**
   * Apply NER to pre-tokenized text.
   *
   * @param text   Text to process.
   * @param tokens Tokenization of text.
   * @return A list of spans of entity mentions, with their type set to the entity class.
   * @throws Exception
   */
  List<Span> apply(String text, List<Span> tokens) throws Exception;

  // TODO: add apply(String) method?
  // What should the default tokenization be?
}
