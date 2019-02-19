package nl.knaw.huygens.pergamon;

/**
 * Constant-memory and constant-time replacement for String.substring.
 * <p>
 * In Java 7, String.substring changed to a linear-memory implementation (a copy). This class replaces that method
 * for applications where such copying behavior is not acceptable.
 */
final class StringSlice implements CharSequence {
  private final String str;
  private final int offset;
  private final int len;

  public StringSlice(String s) {
    this(s, 0, s.length());
  }

  public StringSlice(String s, int from, int to) {
    this(from, to, s);
    check(from, to, s);
  }

  // No-check constructor. Arguments swapped to change the signature.
  private StringSlice(int from, int to, String str) {
    this.str = str;
    offset = from;
    len = to - from;
  }

  @Override
  public final char charAt(int index) {
    return str.charAt(offset + index);
  }

  private static void check(int from, int to, CharSequence s) {
    if (from > to || from < 0 || to > s.length()) {
      throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public final int length() {
    return len;
  }

  @Override
  public final CharSequence subSequence(int from, int to) {
    check(from, to, this);
    return new StringSlice(offset + from, offset + to, str);
  }

  public final String toString() {
    return str.substring(offset, offset + len);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof StringSlice)) {
      return false;
    }
    StringSlice that = (StringSlice) other;
    if (this.len != that.len) {
      return false;
    }
    for (int i = 0; i < len; i++) {
      if (this.charAt(i) != that.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    // 32-bit FNV1a, https://tools.ietf.org/html/draft-eastlake-fnv-13
    int h = 0x811c9dc5;
    for (int i = 0; i < len; i++) {
      h ^= charAt(i);
      h *= 0x01000193;
    }
    return h;
  }
}
