package akka.mesos

import akka.actor.Actor
import akka.event.Logging

import org.apache.mesos.{ Executor, ExecutorDriver, MesosExecutorDriver }
import org.apache.mesos.Protos._

import scala.concurrent.Future

trait AkkaMesosExecutor { _: Actor =>

  import AkkaMesosExecutor._ // for event messages

  // Access to global Mesos configuration values
  private[this] val mesos: MesosImpl = Mesos(context.system)

  val log = Logging(context.system, this)

  /**
    * This executor's associated driver.
    */
  protected[this] lazy val driver: ExecutorDriver =
    new MesosExecutorDriver(proxy)

  /**
    * Returns the eventual exit status of the executor driver.
    */
  def register(): Future[Status] = {
    import context.dispatcher

    val exitStatus: Future[Status] = Future { driver.run }

    exitStatus.onFailure {
      case t: Throwable => log.error(
        "Driver threw an exception: [%s]\n%s".format(
          t.getClass.getName,
          t.getStackTrace.mkString("\n")
        )
      )
    }

    exitStatus.onSuccess {
      case status =>
        log.info("Driver exited with status [%s]" format status)
    }

    exitStatus
  }

  /**
    * Concrete implementations should handle subclasses of [ExecutorMessage].
    */
  def receive: PartialFunction[Any, Unit]

  private[this] val proxy: Executor = new ExecutorProxy

  /**
    * This concrete executor proxies Mesos callbacks to the enclosing Actor.
    */
  protected class ExecutorProxy extends Executor {

    def registered(
      driver: ExecutorDriver,
      executorInfo: ExecutorInfo,
      frameworkInfo: FrameworkInfo,
      slaveInfo: SlaveInfo): Unit =
      self ! Registered(executorInfo, frameworkInfo, slaveInfo)

    def reregistered(
      driver: ExecutorDriver,
      slaveInfo: SlaveInfo): Unit =
      self ! Reregistered(slaveInfo)

    def disconnected(driver: ExecutorDriver): Unit =
      self ! Disconnected()

    def launchTask(driver: ExecutorDriver, task: TaskInfo): Unit =
      self ! LaunchTask(task)

    def killTask(driver: ExecutorDriver, taskId: TaskID): Unit =
      self ! KillTask(taskId)

    def frameworkMessage(driver: ExecutorDriver, data: Array[Byte]): Unit =
      self ! FrameworkMessage(data.toSeq)

    def shutdown(driver: ExecutorDriver): Unit =
      self ! Shutdown()

    def error(driver: ExecutorDriver, message: String): Unit =
      self ! Error(message)

  }
}

/**
  * Companion object for [AkkaMesosExecutor].
  */
object AkkaMesosExecutor {

  /**
    * Common parent type of all [AkkaMesosExecutor] event messages.
    */
  sealed trait ExecutorMessage

  /**
    * Sent once the executor driver has been able to successfully
    * connect with Mesos. In particular, a scheduler can pass some
    * data to it's executors through the ExecutorInfo.data
    * field.
    */
  case class Registered(
    executorInfo: ExecutorInfo,
    frameworkInfo: FrameworkInfo,
    slaveInfo: SlaveInfo) extends ExecutorMessage

  /**
    * Sent when the executor re-registers with a restarted slave.
    */
  case class Reregistered(slaveInfo: SlaveInfo) extends ExecutorMessage

  /**
    * Sent when the executor becomes "disconnected" from the slave
    * (e.g., the slave is being restarted due to an upgrade).
    */
  case class Disconnected() extends ExecutorMessage

  /**
    * Sent when a task has been launched on this executor (initiated
    * via Scheduler.launchTasks. Note that this task can be
    * realized with a thread, a process, or some simple computation.
    */
  case class LaunchTask(task: TaskInfo) extends ExecutorMessage

  /**
    * Sent when a task running within this executor has been killed
    * (via SchedulerDriver.killTask). Note that no status
    * update will be sent on behalf of the executor, the executor is
    * responsible for creating a new TaskStatus (i.e., with
    * TASK_KILLED) and invoking ExecutorDriver.sendStatusUpdate.
    */
  case class KillTask(taskId: TaskID) extends ExecutorMessage

  /**
    * Sent when a framework message has arrived for this
    * executor. These messages are best effort; do not expect a
    * framework message to be retransmitted in any reliable fashion.
    */
  case class FrameworkMessage(data: Seq[Byte])

  /**
    * Sent when the executor should terminate all of it's currently
    * running tasks. Note that after a Mesos has determined that an
    * executor has terminated any tasks that the executor did not send
    * terminal status updates for (e.g., TASK_KILLED, TASK_FINISHED,
    * TASK_FAILED, etc) a TASK_LOST status update will be created.
    */
  case class Shutdown() extends ExecutorMessage

  /**
    * Sent when a fatal error has occured with the executor and/or
    * executor driver. The driver will be aborted BEFORE this
    * message is received.
    */
  case class Error(message: String) extends ExecutorMessage

}