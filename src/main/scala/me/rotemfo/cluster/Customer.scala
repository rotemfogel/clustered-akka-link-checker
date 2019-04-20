package me.rotemfo.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Address, Deploy, Props, ReceiveTimeout, SupervisorStrategy, Terminated}
import akka.remote.RemoteScope
import me.rotemfo.linkchecker.{Controller, Receptionist}

import scala.concurrent.duration._

/**
  * project: akka-cluster-example
  * package: me.rotemfo.akkacluster
  * file:    Customer
  * created: 2019-04-19
  * author:  rotem
  */
class Customer(client: ActorRef, url: String, address: Address) extends Actor with ActorLogging {
  implicit val s: ActorRef = context.parent

  override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy
  val props: Props = Props[Controller].withDeploy(Deploy(scope = RemoteScope(address)))
  val controller: ActorRef = context.actorOf(props, "controller")
  context.watch(controller)
  context.setReceiveTimeout(5.seconds)
  controller ! Controller.Check(url, 2)

  override def receive: PartialFunction[Any, Unit] = ({
    case ReceiveTimeout =>
      context.unwatch(controller)
      client ! Receptionist.Failed(url, "controller timed out")
    case Terminated(_) =>
      client ! Receptionist.Failed(url, "controller died")
    case Controller.Result(links) =>
      context.unwatch(controller)
      client ! Receptionist.Result(url, links)
  }: Receive) andThen (_ => context.stop(self))
}
