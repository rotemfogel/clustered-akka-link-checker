package me.rotemfo.cluster

import akka.actor.{Actor, ActorLogging, Address}
import akka.cluster.{Cluster, ClusterEvent}
import akka.event.LoggingReceive

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
      if (m.address == main) context.stop(self)
  }
}
