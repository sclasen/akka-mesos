package akka.mesos.util

import org.apache.mesos.Protos

case class SlaveID(value: String)
    extends WrappedProtocolBuffer[Protos.SlaveID] {
  def toProto(): Protos.SlaveID =
    Protos.SlaveID.newBuilder.setValue(value).build
}

object SlaveID {
  def apply(slaveId: Protos.SlaveID): SlaveID = SlaveID(slaveId.getValue)
}