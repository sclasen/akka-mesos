package akka.mesos

import akka.actor._
import akka.event.LoggingReceive

/**
  * A test implementation of AkkaMesosScheduler
  */
class ReactiveScheduler extends Actor with AkkaMesosScheduler {

  import AkkaMesosScheduler._ // event messages

  override def preStart(): Unit = register()

  override def preRestart(
    reason: Throwable,
    message: Option[Any]): Unit = this.register()

  override def postStop(): Unit = driver.stop

  def receive = LoggingReceive {

    case Registered(frameworkId, masterInfo) =>
      log info "Successfully registered as a Mesos scheduler"

    case Reregistered(masterInfo) =>
      log info "Reregistered as a Mesos scheduler"

    case ResourceOffers(offers) => {
      log info "Declining resource offers"
      offers.foreach { offer =>
        log.info(offer.toString)
        driver declineOffer offer.getId
      }

      log info "Stopping the [%s] actor system...".format(context.system.name)
      context.system.shutdown()
    }

    case message: SchedulerMessage =>
      log info "Received scheduler message [%s]".format(message)

  }
}
