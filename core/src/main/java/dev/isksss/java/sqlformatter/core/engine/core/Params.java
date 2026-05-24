package dev.isksss.java.sqlformatter.core.engine.core;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/** Handles placeholder replacement with given params. */
public interface Params {

  /** Empty parameter set. */
  public static final Params EMPTY = new Empty();

  /**
   * Returns whether no parameters are available.
   *
   * @return whether this parameter set is empty
   */
  boolean isEmpty();

  /**
   * Returns the next positional parameter value.
   *
   * @return positional parameter value
   */
  Object get();

  /**
   * Returns a named parameter value.
   *
   * @param key parameter name
   * @return named parameter value
   */
  Object getByName(String key);

  /**
   * Creates named parameters from a map.
   *
   * @param params query param
   * @return named parameters
   */
  public static Params of(Map<String, ?> params) {
    return new NamedParams(params);
  }

  /**
   * Creates indexed parameters from a list.
   *
   * @param params query param
   * @return indexed parameters
   */
  public static Params of(List<?> params) {
    return new IndexedParams(params);
  }

  /**
   * Returns param value that matches given placeholder with param key.
   *
   * @param token token.key Placeholder key token.value Placeholder value
   * @return param or token.value when params are missing
   */
  default Object get(Token token) {
    if (this.isEmpty()) {
      return token.value;
    }
    if (!(token.key == null || token.key.isEmpty())) {
      return this.getByName(token.key);
    } else {
      return this.get();
    }
  }

  /** Named parameter implementation. */
  public static class NamedParams implements Params {
    private final Map<String, ?> params;

    /**
     * Creates named parameters.
     *
     * @param params parameter map
     */
    NamedParams(Map<String, ?> params) {
      this.params = params;
    }

    /**
     * Returns whether the parameter map is empty.
     *
     * @return whether the parameter map is empty
     */
    public boolean isEmpty() {
      return this.params.isEmpty();
    }

    @Override
    public Object get() {
      return null;
    }

    @Override
    public Object getByName(String key) {
      return this.params.get(key);
    }

    @Override
    public String toString() {
      return this.params.toString();
    }
  }

  /** Indexed parameter implementation. */
  public static class IndexedParams implements Params {
    private final Queue<?> params;

    /**
     * Creates indexed parameters.
     *
     * @param params parameter list
     */
    IndexedParams(List<?> params) {
      this.params = new ArrayDeque<>(params);
    }

    /**
     * Returns whether the parameter queue is empty.
     *
     * @return whether the parameter queue is empty
     */
    public boolean isEmpty() {
      return this.params.isEmpty();
    }

    @Override
    public Object get() {
      return this.params.poll();
    }

    @Override
    public Object getByName(String key) {
      return null;
    }

    @Override
    public String toString() {
      return this.params.toString();
    }
  }

  /** Empty parameter implementation. */
  public static class Empty implements Params {
    /** Creates an empty parameter set. */
    Empty() {}

    /**
     * Always returns {@code true}.
     *
     * @return {@code true}
     */
    public boolean isEmpty() {
      return true;
    }

    @Override
    public Object get() {
      return null;
    }

    @Override
    public Object getByName(String key) {
      return null;
    }

    @Override
    public String toString() {
      return "[]";
    }
  }
}
