package core.framework.scheduler;

/**
 * @author neo
 */
@FunctionalInterface
public interface Job {
    void execute(JobContext context) throws Exception;
}
