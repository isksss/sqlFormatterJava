package dev.isksss.java.sqlformatter.core.engine.core.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** General utility methods used by the formatter engine. */
public class Util {

  /** Creates a utility instance. */
  public Util() {}

  /**
   * Returns an empty list when the input list is {@code null}.
   *
   * @param ts input list
   * @param <T> element type
   * @return input list or an empty list
   */
  public static <T> List<T> nullToEmpty(List<T> ts) {
    if (ts == null) {
      return Collections.emptyList();
    } else {
      return ts;
    }
  }

  /**
   * Removes trailing spaces and tabs.
   *
   * @param s input string
   * @return string without trailing spaces and tabs
   */
  public static String trimSpacesEnd(String s) {
    int endIndex = s.length();
    char[] chars = s.toCharArray();
    while (endIndex > 0 && (chars[endIndex - 1] == ' ' || chars[endIndex - 1] == '\t')) {
      endIndex--;
    }
    return new String(chars, 0, endIndex);
    // return s.replaceAll("[ \t]+$", "");
  }

  /**
   * Returns the first non-null supplier result.
   *
   * @param sups value suppliers
   * @param <R> result type
   * @return first non-null value, or {@code null}
   */
  @SafeVarargs
  public static <R> R firstNotnull(Supplier<R>... sups) {
    for (Supplier<R> sup : sups) {
      R ret = sup.get();
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  /**
   * Returns the first present optional supplier result.
   *
   * @param sups optional suppliers
   * @param <R> result type
   * @return first present optional, or an empty optional
   */
  @SafeVarargs
  public static <R> Optional<R> firstPresent(Supplier<Optional<R>>... sups) {
    for (Supplier<Optional<R>> sup : sups) {
      Optional<R> ret = sup.get();
      if (ret.isPresent()) {
        return ret;
      }
    }
    return Optional.empty();
  }

  /**
   * Repeats a string.
   *
   * @param s string to repeat
   * @param n repeat count
   * @return repeated string
   */
  public static String repeat(String s, int n) {
    return Stream.generate(() -> s).limit(n).collect(Collectors.joining());
  }

  /**
   * Concatenates two lists.
   *
   * @param l1 first list
   * @param l2 second list
   * @param <T> element type
   * @return concatenated list
   */
  public static <T> List<T> concat(List<T> l1, List<T> l2) {
    return Stream.of(l1, l2).flatMap(List::stream).collect(Collectors.toList());
  }

  /**
   * Sorts strings by descending length.
   *
   * @param strings strings to sort
   * @return sorted strings
   */
  public static JSLikeList<String> sortByLengthDesc(JSLikeList<String> strings) {
    return new JSLikeList<>(
        strings.stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .collect(Collectors.toList()));
  }
}
