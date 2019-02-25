package nl.knaw.huc.rananostra;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import java.io.IOException;
import java.io.StringReader;

// Wraps thread-local nu.xom.Builders.
// http://xom.nu/designprinciples.xhtml#d680e169
final class XmlParser {
  private XmlParser() {
    throw new AssertionError("Class cannot be instantiated, static use only.");
  }

  private static final ThreadLocal<Builder> BUILDER = ThreadLocal.withInitial(Builder::new);

  static Document fromString(String s) throws IOException, ParsingException {
    return BUILDER.get().build(new StringReader(s));
  }
}
