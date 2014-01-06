package com.guidewire.tools.chronos.client;

import org.junit.Assume;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 *
 */
public class Utils {
  public static final String  DEFAULT_HOST = ClientDefaults.DEFAULT_HOST();
  public static final int     DEFAULT_PORT = ClientDefaults.DEFAULT_PORT();
  public static final boolean DEFAULT_SECURE = ClientDefaults.DEFAULT_SECURE();

  public static interface DeferredAction<T> {
    public Future<T> apply();
  }

  public static class DoICare<T> {
    public final boolean doesThisCare;
    public final T value;
    public DoICare(final boolean i_do_care, final T value) {
      this.doesThisCare = i_do_care;
      this.value = value;
    }
  }

  public static <T> DoICare<DeferredAction<T>> doNotCheck(final DeferredAction<T> action) {
    return check(false, action);
  }

  public static <T> DoICare<DeferredAction<T>> check(final DeferredAction<T> action) {
    return check(true, action);
  }

  public static <T> DoICare<DeferredAction<T>> check(final boolean pleaseCheck, final DeferredAction<T> action) {
    return new DoICare<DeferredAction<T>>(pleaseCheck, action);
  }

  @SafeVarargs
  public static <T> List<DoICare<T>> sequential(final DoICare<DeferredAction<T>>...actions) throws InterruptedException, TimeoutException, ExecutionException {
    final ArrayList<DoICare<T>> list = new ArrayList<DoICare<T>>(actions.length);
    for(final DoICare<DeferredAction<T>> care : actions) {
      assertNotNull(care);
      assertNotNull(care.value);

      final T result = care.value.apply().get(4L, TimeUnit.SECONDS);
      assertNotNull(result);

      list.add(new DoICare<T>(care.doesThisCare, result));
    }
    return list;
  }

  private static <T extends ServerResponse> boolean allSuccess(final List<DoICare<T>> results) {
    for(final DoICare<T> result : results)
      if (result.doesThisCare && !result.value.isSuccess()) {
        fail("[CLEANUP REQUIRED] Unable to fully process test on <" + DEFAULT_HOST + ":" + DEFAULT_PORT + "> for " + result.value);
        return false;
      }
    return true;
  }
}
