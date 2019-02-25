package nl.knaw.huc.rananostra;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestStringSlice {
  @Test
  public void sliceString() {
    String s = "abbcccdddd";
    assertEquals("a", slice(s, 0, 1));
    assertEquals("bb", slice(s, 1, 3));
    assertEquals("ccc", slice(s, 3, 6));
    assertEquals("dddd", slice(s, 6, s.length()));
  }

  private static String slice(String s, int from, int to) {
    return new StringSlice(s, from, to).toString();
  }

  @Test
  public void subSequence() {
    String s = "hello_world";
    CharSequence sub = new StringSlice(s, 0, 6);
    sub = sub.subSequence(5, 6);
    assertEquals("_", sub.toString());
    sub = sub.subSequence(0, 0);
    assertEquals("", sub.toString());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void ofEmpty() {
    new StringSlice("", 1, 1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void ofShortSlice() {
    new StringSlice("foobar", 1, 1).subSequence(2, 3);
  }
}
