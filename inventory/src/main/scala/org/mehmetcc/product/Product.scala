package org.mehmetcc.product

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.LocalDateTime
import java.util.UUID

case class Product(
  sku: String = UUID.randomUUID().toString,
  initialStock: Int,
  currentStock: Int,
  price: Double,
  isAvailable: Boolean = true,
  createdAt: LocalDateTime = LocalDateTime.now(),
  updatedAt: LocalDateTime = LocalDateTime.now()
)

object Product {
  implicit val localDateTimeEncoder: JsonEncoder[LocalDateTime] =
    JsonEncoder.string.contramap(_.toString)

  implicit val localDateTimeDecoder: JsonDecoder[LocalDateTime] =
    JsonDecoder.string.map(LocalDateTime.parse)

  implicit val productEncoder: JsonEncoder[Product] = DeriveJsonEncoder.gen[Product]

  implicit val productDecoder: JsonDecoder[Product] = DeriveJsonDecoder.gen[Product]
}
