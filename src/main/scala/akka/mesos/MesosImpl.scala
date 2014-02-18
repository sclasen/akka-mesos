package akka.mesos

import akka.actor.Extension
import com.typesafe.config.Config
import org.apache.mesos.Protos._

import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

class MesosImpl(config: Config) extends Extension {

  val master: String = config.getString("akka.mesos.master")

  val schedulerFailoverTimeout: Duration = Duration(
    config.getMilliseconds("akka.mesos.schedulerFailoverTimeout"),
    TimeUnit.MILLISECONDS
  )

}
