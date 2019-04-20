package me.rotemfo.linkchecker

/**
  * project: clustered-akka-link-checker
  * package: me.rotemfo.linkchecker
  * file:    BadStatus
  * created: 2019-04-20
  * author:  rotem
  */
final case class BadStatus(private val statusCode: Int = 500,
                           private val cause: Throwable = None.orNull)
  extends Exception(s"an http error $statusCode has occurred", cause)
