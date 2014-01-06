package com.guidewire.tools.chronos.client;

import org.junit.Assume;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Future;

import com.guidewire.tools.chronos.client.api.v2.*;

import static com.guidewire.tools.chronos.client.ClientScalaTest.*;
import static org.junit.Assert.*;
import static com.guidewire.tools.chronos.client.Utils.*;


@SuppressWarnings("all")
public class BasicJavaFunctionalitySuite {
  @Test
  public void javaQueryJobs() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));

    final Future<List<Job>> future = Chronos4J.scheduler.jobs.list(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_SECURE);
    assertNotNull(future);

    final List<Job> jobs = future.get();
    for(final Job job : jobs) {
      assertNotNull(job.name());

      //System.out.println(job.toString());
    }
  }

  @Test
  public void javaPing() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));

    final Future<Boolean> future = Chronos4J.debug.ping(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_SECURE);
    assertNotNull(future);
    assertTrue(future.get());
  }

  @Test
  public void javaMetrics() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));

    final Future<ServerMetrics> future = Chronos4J.metrics.full(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_SECURE);
    assertNotNull(future);
    final ServerMetrics metrics = future.get();
    assertNotNull(metrics.version());

    //System.out.println(metrics);
  }
}
