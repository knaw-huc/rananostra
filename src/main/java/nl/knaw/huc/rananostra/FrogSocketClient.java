package nl.knaw.huc.rananostra;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import nl.knaw.huygens.algomas.concurrent.TransientLazy;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Text;
import nu.xom.XPathContext;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Wrapper for the Frog named-entity recognizer.
 * <p>
 * Instances of this class act as clients for a Frog running in TCP server mode
 * producing XML. To start such a server, issue
 * <pre>
 *     frog --skip=mptcla -S ${port} -X
 * </pre>
 * where ${port} is some number. The --skip is optional but prevents Frog from wasting
 * time to produce analyses that we don't use.
 * <p>
 * To use the LaMachine Docker image for Frog, issue
 * <pre>
 *   docker pull proycon/lamachine
 *   docker run -p ${port}:9999 proycon/lamachine frog --skip=mptcla -S 9999 -X
 * </pre>
 */
public class FrogSocketClient {
  private final String host;
  private final int port;

  private static final String foliaNS = "http://ilk.uvt.nl/folia";

  private TransientLazy<TokenizerModel> tokModel = new TransientLazy<>(() -> {
    try {
      return new TokenizerModel(FrogSocketClient.class.getResourceAsStream("/nl-token.bin"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  });

  private static final XPathContext foliaCtxt = new XPathContext("folia", foliaNS);

  /**
   * Construct Frog client. Does not initiate any socket connections; Frog requires
   * us to open a new connection for every sentence.
   *
   * @param host Host name.
   * @param port Port number.
   */
  public FrogSocketClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Construct client for Frog running on localhost.
   */
  public FrogSocketClient(int port) {
    this("localhost", port);
  }

  /**
   * Apply Frog's NER to the given sentence, after tokenizing it.
   * <p>
   * Tokenization is performed by OpenNLP, because letting Frog do it makes it
   * nearly impossible to construct the correct spans.
   */
  public List<Span> apply(String sentence) throws Exception {
    // Tokenizer (unlike TokenizerModel) is stateful, so construct it here.
    // Its constructor is cheap.
    Tokenizer tok = new TokenizerME(tokModel.get());
    return apply(sentence, asList(tok.tokenizePos(sentence)));
  }

  /**
   * Apply Frog's NER to the given pre-tokenized text.
   *
   * @param text   Text to be fed to NER.
   * @param tokens Tokens spans within text.
   * @return A list of spans, the type (getType) of which is the entity class assigned to them by Frog.
   * @throws IOException
   * @throws ParsingException
   */
  public List<Span> apply(String text, List<Span> tokens) throws Exception {
    StringBuilder sb = new StringBuilder();

    try (Socket conn = new Socket(host, port)) {
      writeTokens(text, tokens, new OutputStreamWriter(conn.getOutputStream(), UTF_8));

      BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF_8));

      // Frog output is: an XML document, followed by "READY" on a line by itself, repeated
      // ad infinitum.
      for (String line; (line = r.readLine()) != null && !line.equals("READY"); ) {
        sb.append(line);
      }
      r.close();
    }

    Document doc = XmlParser.fromString(sb.toString());
    Nodes tokenNodes = doc.query("//folia:w", foliaCtxt);

    // Record an id->token index mapping to resolve entity spans, which Frog represents
    // by offset annotations.
    Map<String, Integer> idToIndex =
      IntStream.range(0, tokenNodes.size()).boxed()
               .collect(Collectors.toMap(i -> {
                   Element elem = (Element) tokenNodes.get(i);
                   return elem.getAttribute("id", "http://www.w3.org/XML/1998/namespace")
                              .getValue();
                 },
                 Function.identity()
               ));

    // A folia:entity contains a list of folia:wref elements pointing back to the tokens, e.g.,
    // <entity xml:id="untitled.p.1.s.1.entities.1.entity.1" class="per" confidence = "0" >
    //   <wref id="untitled.p.1.s.1.w.3" t="Ben"/>
    //   <wref id="untitled.p.1.s.1.w.4" t="Hur/>
    // </entity>
    //
    // We need the check for @class, because when the MWU detector is not skipped,
    // Frog produces entities without a class (which are not really named entities).
    Nodes entities = doc.query("//folia:entity[@class]", foliaCtxt);
    return IntStream.range(0, entities.size()).mapToObj(i -> {
      Element elem = (Element) entities.get(i);
      Nodes wrefs = elem.query(".//folia:wref", foliaCtxt);

      // Let's assume each entity mention is a contiguous span of tokens, served to us in
      // textual order, so we only need to look at the first and the last.
      String id = ((Element) wrefs.get(0)).getAttribute("id").getValue();
      int start = tokens.get(idToIndex.get(id)).getStart();
      id = ((Element) wrefs.get(wrefs.size() - 1)).getAttribute("id").getValue();
      int end = tokens.get(idToIndex.get(id)).getEnd();

      return new Span(start, end, elem.getAttribute("class").getValue());
    }).collect(Collectors.toList());
  }

  public static class XMLOptions {
    @JsonProperty
    @NotEmpty
    public String xml;

    @JsonProperty
    @NotEmpty
    public String xpath;

    @JsonProperty
    public Map<String, String> namespaces;

    @JsonProperty("starttag")
    @NotEmpty
    public String startTag;

    @JsonProperty("endtag")
    @NotEmpty
    public String endTag;

    @JsonProperty("classattr")
    public String classAttr;

    public XMLOptions() {
    }

    public XMLOptions(String xml, String xpath, Map<String, String> namespaces, String startTag,
                      String endTag, String classAttr) {
      this.xml = requireNonNull(xml);
      this.xpath = requireNonNull(xpath);
      this.namespaces = namespaces;
      this.startTag = startTag;
      this.endTag = endTag;
      this.classAttr = classAttr;
    }

    public XMLOptions(XMLOptions other) {
      this.xml = other.xml;
      this.xpath = other.xpath;
      this.namespaces = other.namespaces;
      this.startTag = other.startTag;
      this.endTag = other.endTag;
      this.classAttr = other.classAttr;
    }
  }

  /**
   * Add NE annotations to an XML document.
   * <p>
   * For each element matching the given XPath query, the string yield is passed to Frog as {@link #apply(String)} does.
   * The resulting entities are marked in the XML tree by inserting milestone tags with the given start and end types.
   *
   * @return The modified XML document.
   */
  public String applyXML(XMLOptions options) throws Exception {
    Document doc = XmlParser.fromString(options.xml);

    XPathContext ctx = new XPathContext();
    if (options.namespaces != null) {
      options.namespaces.forEach(ctx::addNamespace);
    }

    Nodes nodes = doc.query(options.xpath, ctx);

    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.get(i);
      String text = node.getValue();
      List<Span> spans = apply(text);
      if (node instanceof Text) {
        node = node.getParent();
      }
      new PutMilestones(options, spans).traverse(node);
    }

    return doc.toXML();
  }

  /*
   * Inserts milestones into the text of elem at the startTag and end positions of the spans.
   */
  private static class PutMilestones {
    private boolean atSpanEnd; // Are we at the end of the current span?
    private List<Span> spans;
    private int spanpos; // spans.get(spanpos) is the current span.
    private int textpos; // Position in XML document's text.

    private final XMLOptions options;

    private PutMilestones(XMLOptions options, List<Span> spans) {
      this.options = options;
      this.spans = spans;
    }

    private List<Node> traverse(Node node) {
      if (spanpos == spans.size()) {
        return Collections.singletonList(node);
      }
      if (node instanceof Text) {
        return splitText((Text) node);
      }
      if (node instanceof Element) {
        traverseElement((Element) node);
      }
      return Collections.singletonList(node);
    }

    private void traverseElement(Element elem) {
      for (int i = 0; i < elem.getChildCount(); ) {
        Node child = elem.getChild(i);
        List<Node> newnodes = traverse(child);

        elem.removeChild(i);
        for (Node newnode : newnodes) {
          elem.insertChild(newnode, i);
          i++;
        }
      }
    }

    private List<Node> splitText(Text node) {
      CharSequence text = StringSlice.of(node.getValue());

      List<Node> newnodes = new ArrayList<>();
      while (true) {
        int pos = peekSpanPos();
        if (pos == -1) {
          break;
        }
        pos -= textpos;

        if (pos > text.length()) {
          textpos += text.length();
          break;
        }

        newnodes.add(new Text(text.subSequence(0, pos).toString()));
        text = text.subSequence(pos, text.length());
        textpos += pos;

        if (atSpanEnd) {
          newnodes.add(new Element(options.endTag));
        } else {
          Element elem = new Element(options.startTag);
          if (!Strings.isNullOrEmpty(options.classAttr)) {
            elem.addAttribute(new Attribute(options.classAttr, spans.get(spanpos).getType()));
          }
          newnodes.add(elem);
        }
        nextSpanPos();
      }
      if (text.length() > 0) {
        newnodes.add(new Text(text.toString()));
      }
      return newnodes;
    }

    // Increment position in spans.
    private void nextSpanPos() {
      if (atSpanEnd) {
        spanpos++;
      }
      atSpanEnd = !atSpanEnd;
    }

    // Returns the position of the next span start or end.
    private int peekSpanPos() {
      if (spanpos == spans.size()) {
        return -1;
      } else if (atSpanEnd) {
        return spans.get(spanpos).getEnd();
      } else {
        return spans.get(spanpos).getStart();
      }
    }
  }

  private void writeTokens(String text, List<Span> tokens, Writer w) throws IOException {
    Span prev = new Span(0, 0);
    for (Span span : tokens) {
      Span trimmed = span.trim(text);
      String token = trimmed.getCoveredText(text).toString();

      if (token.length() == 0) {
        throw new IllegalArgumentException(
          String.format("Empty or all-whitespace token '%s' at %s",
            span.getCoveredText(text).toString(), span.toString()));
      } else if ("EOT".equals(token)) {
        throw new IllegalArgumentException("'EOT' not allowed as a token");
      } else if (span.crosses(prev)) {
        throw new IllegalArgumentException(
          String.format("crossing spans %s and %s", prev, span));
      } else if (span.getStart() < prev.getEnd()) {
        throw new IllegalArgumentException(
          String.format("unsorted spans: %s, then %s", prev, span));
      }
      w.write(trimmed.getCoveredText(text).toString());
      w.write('\n');
      prev = span;
    }
    w.write("EOT\n");
    w.flush();
  }
}
