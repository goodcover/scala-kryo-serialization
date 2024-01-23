package io.altoo.serialization.kryo.scala.serializer

import com.esotericsoftware.kryo.kryo5.io.{Input, Output}
import com.esotericsoftware.kryo.kryo5.{Kryo, Serializer}

import scala.runtime.EnumValue

/** Serializes enums using the enum's name. This prevents invalidating previously serialized bytes when the enum order changes */
class ScalaEnumNameSerializer[T <: EnumValue] extends Serializer[T] {

  def read(kryo: Kryo, input: Input, typ: Class[? <: T]): T = {
    val clazz = kryo.readClass(input).getType
    val name = input.readString()
    // using value instead of ordinal to make serialization more stable, e.g. allowing reordering without breaking compatibility
    clazz.getDeclaredMethod("valueOf", classOf[String]).invoke(null, name).asInstanceOf[T]
  }

  def write(kryo: Kryo, output: Output, obj: T): Unit = {
    val enumClass = obj.getClass.getSuperclass
    val productPrefixMethod = obj.getClass.getDeclaredMethod("productPrefix")
    if !productPrefixMethod.canAccess(obj) then productPrefixMethod.setAccessible(true)
    val name = productPrefixMethod.invoke(obj).asInstanceOf[String]
    kryo.writeClass(output, enumClass)
    output.writeString(name)
  }
}
