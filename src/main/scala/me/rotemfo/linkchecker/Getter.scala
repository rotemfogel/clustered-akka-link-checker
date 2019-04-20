package me.rotemfo.linkchecker

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.event.LoggingReceive
import akka.pattern.pipe
import org.jsoup.Jsoup

import scala.concurrent.ExecutionContextExecutor

/**
  * project: clustered-link-checker
  * package: me.rotemfo.linkchecker
  * file:    Getter
  * created: 2019-03-03
  * author:  rotem
  */
object Getter {
  case object Abort
  case object Done

  def getterProps(url: String, depth: Int): Props = {
    Props(new Getter(url, depth))
  }
}

class Getter(url: String, depth: Int) extends Actor with ActorLogging {

  implicit val executor: ExecutionContextExecutor = context.dispatcher

  protected def webClient: WebClient = new AsyncWebClient

  webClient.get(url).pipeTo(self)

  import scala.collection.JavaConverters._

  private def findLinks(body: String): Iterator[String] = {
    val document = Jsoup.parse(body)
    val links = document.select("a[href]")
    for {
      link <- links.iterator.asScala
    } yield link.absUrl("href")
  }

  private def stop(): Unit = {
    context.parent ! Getter.Done
    context.stop(self)
  }

  override def postStop(): Unit = webClient.shutdown()

  override def receive: Receive = LoggingReceive {
    case body: String =>
      for (link <- findLinks(body)) {
        log.debug("Getter::receive ==> {}", link)
        context.parent ! Controller.Check(link, depth)
      }
      stop()
    case Getter.Abort => stop()
    case _: Status.Failure => stop()
  }
}
