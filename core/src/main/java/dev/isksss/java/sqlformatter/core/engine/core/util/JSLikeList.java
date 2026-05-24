package dev.isksss.java.sqlformatter.core.engine.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Small immutable-style list wrapper with helpers mirroring JavaScript arrays.
 *
 * @param <T> element type
 */
public class JSLikeList<T> implements Iterable<T> {

  private List<T> tList;

  /**
   * Creates a list wrapper.
   *
   * @param tList source list
   */
  public JSLikeList(List<T> tList) {
    this.tList = tList == null ? Collections.emptyList() : new ArrayList<>(tList);
  }

  /**
   * Returns the wrapped list.
   *
   * @return wrapped list
   */
  public List<T> toList() {
    return this.tList;
  }

  /**
   * Maps values to a new wrapped list.
   *
   * @param mapper mapping function
   * @param <R> mapped element type
   * @return mapped list wrapper
   */
  public <R> JSLikeList<R> map(Function<T, R> mapper) {
    return new JSLikeList<>(this.tList.stream().map(mapper).collect(Collectors.toList()));
  }

  /**
   * Joins values using a delimiter.
   *
   * @param delimiter delimiter
   * @return joined string
   */
  public String join(CharSequence delimiter) {
    return this.tList.stream()
        .map(Optional::ofNullable)
        .map(x -> x.map(String::valueOf).orElse(""))
        .collect(Collectors.joining(delimiter));
  }

  /**
   * Returns a new wrapper with another list appended.
   *
   * @param other list to append
   * @return combined list wrapper
   */
  public JSLikeList<T> with(List<T> other) {
    List<T> list = new ArrayList<>();
    list.addAll(this.toList());
    list.addAll(other);
    return new JSLikeList<>(list);
  }

  /**
   * Joins values using a comma.
   *
   * @return joined string
   */
  public String join() {
    return join(",");
  }

  /**
   * Returns whether this list has no elements.
   *
   * @return whether this list is empty
   */
  public boolean isEmpty() {
    return this.tList == null || this.tList.isEmpty();
  }

  /**
   * Returns the element at the given index.
   *
   * @param index zero-based index
   * @return element, or {@code null} when out of bounds
   */
  public T get(int index) {
    if (index < 0) {
      return null;
    }
    if (tList.size() <= index) {
      return null;
    }
    return this.tList.get(index);
  }

  /**
   * Returns an iterator for the wrapped list.
   *
   * @return iterator
   */
  @Override
  public Iterator<T> iterator() {
    return this.tList.iterator();
  }

  /**
   * Returns a stream for the wrapped list.
   *
   * @return stream
   */
  public Stream<T> stream() {
    return this.tList.stream();
  }

  /**
   * Returns the list size.
   *
   * @return list size
   */
  public int size() {
    return this.tList.size();
  }
}
