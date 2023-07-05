package io.altoo.serialization.kryo.scala.serializer

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}

import java.lang.reflect.Field

/**
 * Serializes enumeration by name.
 */
class EnumerationNameSerializer extends Serializer[Enumeration#Value] {

  def read(kryo: Kryo, input: Input, typ: Class[? <: Enumeration#Value]): Enumeration#Value = {
    val clazz = kryo.readClass(input).getType
    val name = input.readString()
    clazz.getDeclaredField("MODULE$").get(null).asInstanceOf[Enumeration].withName(name)
  }

  def write(kryo: Kryo, output: Output, obj: Enumeration#Value): Unit = {
    val parentEnum = parent(obj.getClass.getSuperclass)
      .getOrElse(throw new NoSuchElementException(s"Enumeration not found for $obj"))
    val enumClass = parentEnum.get(obj).getClass
    kryo.writeClass(output, enumClass)
    output.writeString(obj.toString)
  }

  private def parent(typ: Class[?]): Option[Field] =
    if (typ == null) None
    else typ.getDeclaredFields.find(_.getName == "$outer").orElse(parent(typ.getSuperclass))
}
