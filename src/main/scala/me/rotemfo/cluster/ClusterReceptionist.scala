package me.rotemfo.cluster


import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.event.LoggingReceive
import me.rotemfo.linkchecker.Receptionist

import scala.util.Random

/**
  * project: clustered-link-checker
  * package: me.rotemfo.cluster
  * file:    ClusterReceptionist
  * created: 2019-04-20
  * author:  rotem
  */
class ClusterReceptionist extends Actor with ActorLogging {
  private val random = new Random()
  private val cluster = Cluster(context.system)
  cluster.subscribe(self, classOf[MemberUp])
  cluster.subscribe(self, classOf[MemberRemoved])

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = awaitingMembers

  private val awaitingMembers: Receive = LoggingReceive {
    case current: CurrentClusterState =>
      val addresses = current.members.toSeq.map(_.address)
      val notMe = addresses.filterNot(_ == cluster.selfAddress)
      if (notMe.isEmpty) context.become(active(notMe))
    case MemberUp(member) if member.address != cluster.selfAddress =>
      context.become(active(Seq(member.address)))
  }

  private def pick(addresses: Seq[Address]): Address = {
    val next = {
      val n = random.nextInt(addresses.length)
      if (n > 0) n - 1 else n
    }
    addresses(next)
  }

  private def active(addresses: Seq[Address]): Receive = LoggingReceive {
    case MemberUp(member)
      if member.address != cluster.selfAddress =>
      context.become(active(addresses :+ member.address))
    case MemberRemoved(member, _) =>
      val next = addresses.filterNot(_ == member.address)
      if (next.isEmpty) context.become(awaitingMembers)
      else context.become(active(next))
    case Receptionist.Get(url)
      if context.children.size < addresses.size =>
      val client = sender
      val address = pick(addresses)
      context.actorOf(Props(new Customer(client, url, address)))
    case Receptionist.Get(url) =>
      sender ! Receptionist.Failed(url, "too many parallel queries")
  }
}
