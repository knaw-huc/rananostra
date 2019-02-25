package nl.knaw.huc.rananostra;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import java.io.IOException;
import java.io.StringReader;

// Wraps thread-local nu.xom.Builders.
// http://xom.nu/designprinciples.xhtml#d680e169
class XmlParser {
  private XmlParser() {
  }

  private static final ThreadLocal<Builder> BUILDER =
    new ThreadLocal<Builder>() {
      @Override
      protected Builder initialValue() {
        return new Builder();
      }
    };

  static Document fromString(String s) throws IOException, ParsingException {
    return BUILDER.get().build(new StringReader(s));
  }
}
