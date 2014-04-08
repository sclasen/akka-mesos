package akka.mesos.util

import com.google.protobuf.ByteString
import org.apache.mesos.Protos
import scala.collection.JavaConverters._

case class TaskInfo(
    name: String,
    taskId: TaskID,
    slaveId: SlaveID,
    resources: Seq[Resource],
    executor: Option[Protos.ExecutorInfo],
    command: Option[Protos.CommandInfo],
    data: Option[Seq[Byte]]) extends WrappedProtocolBuffer[Protos.TaskInfo] {

  def toProto(): Protos.TaskInfo = {
    val builder = Protos.TaskInfo.newBuilder
      .setName(name)
      .setTaskId(taskId.toProto)
      .setSlaveId(slaveId.toProto)
      .addAllResources(resources.map(_.toProto).asJava)
    for (e <- executor) builder.setExecutor(e)
    for (c <- command) builder.setCommand(c)
    for (d <- data) builder.setData(ByteString.copyFrom(d.toArray))
    builder.build
  }

}

object TaskInfo {
  def apply(taskInfo: Protos.TaskInfo): TaskInfo = TaskInfo(
    taskInfo.getName,
    TaskID(taskInfo.getTaskId),
    SlaveID(taskInfo.getSlaveId),
    taskInfo.getResourcesList.asScala.map(Resource.apply).toSeq,
    Option(taskInfo.getExecutor),
    Option(taskInfo.getCommand),
    Option(taskInfo.getData.toByteArray)
  )
}