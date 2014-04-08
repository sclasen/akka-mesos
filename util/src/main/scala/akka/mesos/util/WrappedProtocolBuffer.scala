package akka.mesos.util

import com.google.protobuf.Message

trait WrappedProtocolBuffer[T <: Message] {
  def toProto(): T
}
