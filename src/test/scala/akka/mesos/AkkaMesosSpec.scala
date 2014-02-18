package akka.mesos

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import akka.actor._
import org.slf4j.LoggerFactory

import com.typesafe.config.ConfigFactory

class AkkaMesosSchedulerSpec extends FlatSpec with ShouldMatchers {

  val log = LoggerFactory.getLogger(getClass.getName)

  val system: ActorSystem = ActorSystem("TestSystem", ConfigFactory.load)

  "The Mesos Extension" should "be loaded" in {
    system hasExtension Mesos should equal (true)
  }

  "A ReactiveScheduler" should "start up, receive offers, and stop its containing ActorSystem" in {
    val scheduler = system.actorOf(Props[ReactiveScheduler], "scheduler")
    system.awaitTermination
  }

}
