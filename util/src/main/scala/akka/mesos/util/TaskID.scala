package akka.mesos.util

import org.apache.mesos.Protos

case class TaskID(value: String) extends WrappedProtocolBuffer[Protos.TaskID] {
  def toProto(): Protos.TaskID =
    Protos.TaskID.newBuilder.setValue(value).build
}

object TaskID {
  def apply(taskId: Protos.TaskID): TaskID = TaskID(taskId.getValue)
}