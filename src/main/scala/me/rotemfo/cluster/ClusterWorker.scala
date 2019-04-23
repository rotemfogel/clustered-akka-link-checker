package me.rotemfo.cluster

import akka.actor.{Actor, ActorIdentity, ActorLogging, Address, Identify, RootActorPath, Terminated}
import akka.cluster.{Cluster, ClusterEvent}
import akka.event.LoggingReceive
import me.rotemfo.linkchecker.AsyncWebClient

/**
  * project: clustered-link-checker
  * package: me.rotemfo.cluster
  * file:    ClusterWorker
  * created: 2019-04-20
  * author:  rotem
  */
class ClusterWorker extends Actor with ActorLogging {
  val cluster: Cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[ClusterEvent.MemberRemoved])
  val main: Address = cluster.selfAddress.copy(port = Some(2552))
  cluster.join(main)

  override def receive: Receive = LoggingReceive {
    case ClusterEvent.MemberRemoved(m, _) =>
      if (m.address == main) {
        val path = RootActorPath(main) / "user" / "app" / "receptionist"
        context.actorSelection(path) ! Identify("receptionist")
      }
    case ActorIdentity("receptionist", None) => context.stop(self)
    case ActorIdentity("receptionist", Some(ref)) => context.watch(ref)
    case Terminated(_) => context.stop(self)
  }

  override def postStop(): Unit = AsyncWebClient.shutdown()
}