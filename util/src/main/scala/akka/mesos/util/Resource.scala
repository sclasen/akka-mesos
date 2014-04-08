package akka.mesos.util

import org.apache.mesos.Protos
import scala.collection.JavaConverters._

// required string name = 1;
// required Value.Type type = 2;
// optional Value.Scalar scalar = 3;
// optional Value.Ranges ranges = 4;
// optional Value.Set set = 5;
// optional string role = 6 [default = "*"];

trait Resource extends WrappedProtocolBuffer[Protos.Resource]

object Resource {
  def apply(resource: Protos.Resource): Resource = resource.getType match {
    case Protos.Value.Type.SCALAR => ScalarResource(
      resource.getName,
      resource.getScalar.getValue,
      Some(resource.getRole)
    )

    case Protos.Value.Type.RANGES => RangeResource(
      resource.getName,
      resource.getRanges.getRangeList.asScala.map { r =>
        r.getBegin.toInt to r.getEnd.toInt
      }.toSet,
      Some(resource.getRole)
    )

    case Protos.Value.Type.SET => SetResource(
      resource.getName,
      resource.getSet.getItemList.asScala.toSet,
      Some(resource.getRole)
    )
  }
}

case class ScalarResource(
    name: String,
    value: Double,
    role: Option[String]) extends Resource {
  def toProto(): Protos.Resource = {
    val scalarBuilder = Protos.Value.Scalar.newBuilder.setValue(value)
    val builder = Protos.Resource.newBuilder
      .setName(name)
      .setType(Protos.Value.Type.SCALAR)
      .setScalar(scalarBuilder)
    for (r <- role) builder.setRole(r)
    builder.build
  }
}

case class RangeResource(
    name: String,
    ranges: Set[Range],
    role: Option[String]) extends Resource {
  def toProto(): Protos.Resource = {
    val rangesBuilder = Protos.Value.Ranges.newBuilder
      .addAllRange(ranges.map(rangeToProto).asJava)

    val builder = Protos.Resource.newBuilder
      .setName(name)
      .setType(Protos.Value.Type.RANGES)
      .setRanges(rangesBuilder)
    for (r <- role) builder.setRole(r)
    builder.build
  }

  private def rangeToProto(range: Range): Protos.Value.Range =
    Protos.Value.Range.newBuilder
      .setBegin(range.start)
      .setEnd(range.end)
      .build
}

case class SetResource(
    name: String,
    items: Set[String],
    role: Option[String]) extends Resource {
  def toProto(): Protos.Resource = {
    val setBuilder = Protos.Value.Set.newBuilder
      .addAllItem(items.asJava).build
    val builder = Protos.Resource.newBuilder
      .setName(name)
      .setType(Protos.Value.Type.SET)
      .setSet(setBuilder)
    for (r <- role) builder.setRole(r)
    builder.build
  }
}
