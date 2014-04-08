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

  var callback: Option[ActorRef] = None

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

      callback.foreach { ref =>
        ref ! ReactiveScheduler.Done()
        driver.stop()
      }
    }

    case ReactiveScheduler.CallMeBack =>
      this.callback = Some(sender)

    case message: SchedulerMessage =>
      log info "Received scheduler message [%s]".format(message)

  }
}

object ReactiveScheduler {
  case class CallMeBack()
  case class Done()
}