# akka-mesos

An Akka extension to simplify building reactive Mesos frameworks.

## Getting Started

Add the akka-mesos dependency to your project _(TODO: mirror on Maven central)_

SBT:

```scala
libraryDependencies += "io.mesosphere" %% "akka-mesos" % "0.1.0"
```

Maven:

```xml
<dependency>
  <groupId>io.mesosphere</groupId>
  <artifactId>akka-mesos</artifactId>
  <version>0.1.0</version>
</dependency>
```

Akka-mesos declares its dependency on Akka with "provided" scope, so your project needs to specify an Akka dependency as well.  Akka-mesos is built against Akka version `2.2.3`.

Tell Akka to load the akka-mesos extension by modifying your application's config

```
akka {
  extensions = ["akka.mesos.Mesos"]
}
```

Add configuration values to specify the location of your mesos master

```
akka {
  mesos {
    master = "mesos-master.mycompany.com:5050"
  }
}
```

## Usage

### AkkaMesosScheduler

To implement a Mesos scheduler as an Akka actor, import and mix in the supplied `AkkaMesosScheduler` trait to an `Actor` subclass.  The `preStart` and `preRestart` behaviors illustrated below are significant.  The `receive` partial function should be defined for the subtypes of `AkkaMesosScheduler.SchedulerMessage`.

```scala
import akka.actor._
import akka.mesos.AkkaMesosScheduler

class ReactiveScheduler extends Actor with AkkaMesosScheduler {

  import AkkaMesosScheduler._ // scheduler messages

  override def preStart(): Unit = register()

  override def preRestart(
    reason: Throwable,
    message: Option[Any]): Unit = register()

  override def postStop(): Unit = driver.stop

  def receive = {

    case Registered(frameworkId, masterInfo) => // ...

    case Reregistered(masterInfo) => // ...

    case ResourceOffers(offers) => // ...

    case OfferRescinded(offerId) => // ...

    case StatusUpdate(taskStatus) => // ...

    case FrameworkMessage(executorId, slaveId, data) => // ...

    case Disconnected() => // ...

    case SlaveLost(slaveId) => // ...

    case ExecutorLost(executorId, slaveId, status) => // ...

    case Error(message) => // ...
  }
}
```

### AkkaMesosExecutor

To implement a Mesos scheduler as an Akka actor, import and mix in the supplied `AkkaMesosExecutor` trait to an `Actor` subclass.  The `preStart` and `preRestart` behaviors illustrated below are significant.  The `receive` partial function should be defined for the subtypes of `AkkaMesosScheduler.SchedulerMessage`.

```scala
// TODO
```
