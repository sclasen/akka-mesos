package akka.mesos

import akka.actor.{ ExtensionId, ExtensionIdProvider, ExtendedActorSystem }

object Mesos extends ExtensionId[MesosImpl] with ExtensionIdProvider {

  override def lookup = Mesos

  override def createExtension(system: ExtendedActorSystem) =
    new MesosImpl(system.settings.config)

}
