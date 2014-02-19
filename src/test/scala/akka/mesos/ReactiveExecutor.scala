package akka.mesos

import akka.actor._
import akka.event.LoggingReceive

/**
  * A test implementation of AkkaMesosExecutor
  */
class ReactiveExecutor extends Actor with AkkaMesosExecutor {

  import AkkaMesosExecutor._ // event messages

  override def preStart(): Unit = register()

  override def preRestart(
    reason: Throwable,
    message: Option[Any]): Unit = this.register()

  override def postStop(): Unit = driver.stop

  def receive = LoggingReceive {

    case Registered(executorInfo, frameworkInfo, slaveInfo) => {
      log info "Successfully registered as a Mesos executor"
    }

    case Reregistered(slaveInfo) =>
      log info "Reregistered as a Mesos executor"

    case message: ExecutorMessage =>
      log info "Received scheduler message [%s]".format(message)

  }
}
