package de.frosner.spark.mtf

import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.sql.types.{DataType, DoubleType, FloatType}
import org.scalatest.{FlatSpec, Matchers}
import scodec.bits.ByteOrdering
import scodec.{codecs => Codecs}

class MtfCubeRelationSpec extends FlatSpec with Matchers {

  "Decoding bytes" should "work" in {
    val codec = SerializableCodec.Float
    val value = 4f
    val valueBytes = Codecs.float.encode(value).toOption.get.toByteArray
    MtfCubeRelation.decodeBytes[Float](codec)(valueBytes) shouldBe value
  }

  it should "fail if it tries to decode something which is not correct" in {
    val codec = SerializableCodec.Float
    val valueBytes = Array(2.toByte)
    intercept[DecodingFailedException] {
      MtfCubeRelation.decodeBytes[Float](codec)(valueBytes)
    }
  }

  it should "fail if it tries to decode something which has a remainder" in {
    val codec = SerializableCodec.Float
    val valueBytes = Array.fill(5)(2.toByte)
    intercept[NonEmptyRemainderException] {
      MtfCubeRelation.decodeBytes[Float](codec)(valueBytes)
    }
  }

  "Build scan" should "work" in {
    val relation = MtfCubeRelation(
      location = "src/test/resources/example.dat.0",
      numTime = 1,
      numInstruments = 1,
      numScenarious = 1,
      endianType = ByteOrdering.LittleEndian,
      valueType = FloatType
    )(SparkSession.builder().master("local").getOrCreate().sqlContext)
    relation.buildScan().foreach(println)
  }

}