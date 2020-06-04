package com.riemers

package object data {

  class MissingFieldException(field: String) extends RuntimeException(s"Field $field is missing!")

}
