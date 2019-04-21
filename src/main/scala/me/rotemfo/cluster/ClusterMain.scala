package me.rotemfo.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import akka.event.LoggingReceive
import me.rotemfo.linkchecker.Receptionist
import me.rotemfo.linkchecker.Receptionist.Get

import scala.concurrent.duration.{Duration, FiniteDuration, _}

/**
  * project: akka-cluster-example
  * package: me.rotemfo.akkacluster
  * file:    AppMain
  * created: 2019-04-19
  * author:  rotem
  */
class ClusterMain extends Actor with ActorLogging {
  private val cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[MemberUp])
  cluster.subscribe(self, classOf[MemberRemoved])
  cluster.join(cluster.selfAddress)

  val receptionist: ActorRef = context.actorOf(Props[ClusterReceptionist], "receptionist")
  context.watch(receptionist)

  def later(url: String, d: FiniteDuration): Unit = {
    import context.dispatcher
    context.system.scheduler.scheduleOnce(d, receptionist, Get(url))
    log.info("scheduled check for url: [{}]", url)
  }

  later("https://www.amazon.com", Duration.Zero)

  override def receive: Receive = LoggingReceive {
    case MemberUp(m) =>
      if (m.address != cluster.selfAddress) {
        later("https://www.google.com", 1.seconds)
        later("https://www.google.com/2", 2.seconds)
        later("https://www.google.com/2", 2.seconds)
        later("https://www.google.com/3", 3.seconds)
        later("https://www.google.com/4", 4.seconds)
        later("https://www.google.com/5", 5.seconds)
        context.setReceiveTimeout(3.seconds)
      }
    case Receptionist.Failed(url, reason) =>
      log.error("Failed to fetch {}, reason: {}", url, reason)
    case Receptionist.Result(url, set) =>
      log.info("{}", set.mkString(s"Results for '$url':\n", "\n", "\n"))
    case ReceiveTimeout =>
      cluster.leave(cluster.selfAddress)
    case MemberRemoved(m, _) =>
      if (m.address == cluster.selfAddress)
        context.stop(self)
  }
}