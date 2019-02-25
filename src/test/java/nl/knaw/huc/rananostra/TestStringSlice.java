package nl.knaw.huc.rananostra;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestStringSlice {
  @Test
  void sliceString() {
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
  void subSequence() {
    String s = "hello_world";
    CharSequence sub = new StringSlice(s, 0, 6);
    sub = sub.subSequence(5, 6);
    assertEquals("_", sub.toString());
    sub = sub.subSequence(0, 0);
    assertEquals("", sub.toString());
  }

  @Test
  void outOfBounds() {
    assertThrows(IndexOutOfBoundsException.class, () ->
      new StringSlice("", 1, 1));
    assertThrows(IndexOutOfBoundsException.class, () ->
      new StringSlice("foobar", 1, 1).subSequence(2, 3));
  }
}
