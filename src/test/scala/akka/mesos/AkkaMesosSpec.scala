package akka.mesos

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import akka.actor._
import org.slf4j.LoggerFactory

import com.typesafe.config.ConfigFactory

class AkkaMesosSchedulerSpec extends FlatSpec with ShouldMatchers {

  val log = LoggerFactory.getLogger(getClass.getName)

  val system = ActorSystem("TestSystem", ConfigFactory.load)

  "The Mesos Extension" should "be loaded" in {
    system hasExtension Mesos should equal (true)
  }

  "A ReactiveScheduler" should "start up" in {
    import ReactiveScheduler.{ CallMeBack, Done }
    import akka.pattern.ask
    import akka.util.Timeout
    import scala.concurrent.Await
    import scala.concurrent.duration.Duration
    import java.util.concurrent.TimeUnit.SECONDS

    val waitDuration = Duration(10, SECONDS)
    implicit val _: Timeout = Timeout(waitDuration)

    val scheduler = system.actorOf(Props[ReactiveScheduler], "scheduler")
    val complete = (scheduler ? CallMeBack).mapTo[Done]
    Await.result(complete, waitDuration)
    system.stop(scheduler)
  }

  "A ReactiveExecutor" should "start up" in {
    val executor = system.actorOf(Props[ReactiveExecutor], "executor")
    system.stop(executor)
  }

  "The test actor system" should "shut down" in {
    import scala.util.{ Try, Success, Failure }

    Try {
      system.shutdown()
    } match {
      case Success(_) => println("shut down complete")
      case Failure(t) => {
        println("Shut down threw an exception:\n%s" format t)
      }
    }
  }

}
