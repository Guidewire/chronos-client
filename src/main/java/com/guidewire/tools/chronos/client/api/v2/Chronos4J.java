package com.guidewire.tools.chronos.client.api.v2;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 *
 */
public final class Chronos4J {
  public static final Scheduler scheduler = Scheduler.INSTANCE;
  public static final NonScalaApi.DebugApi$ debug = NonScalaApi.debug();
  public static final NonScalaApi.MetricsApi$ metrics = NonScalaApi.metrics();

  public static final class Scheduler implements Serializable {
    public static final NonScalaApi$SchedulerApi$JobsApi$ jobs = NonScalaApi.scheduler_jobs();
    public static final NonScalaApi$SchedulerApi$TasksApi$ tasks = NonScalaApi.scheduler_tasks();
    public static final NonScalaApi$SchedulerApi$GraphsApi$ graphs = NonScalaApi.scheduler_graphs();

    public static final Scheduler INSTANCE = new Scheduler();

    private Scheduler() { }
    private Object readResolve() throws ObjectStreamException { return INSTANCE; }
  }

  private static final Chronos4J INSTANCE = new Chronos4J();

  private Chronos4J() { }
  private Object readResolve() throws ObjectStreamException { return INSTANCE; }
}
