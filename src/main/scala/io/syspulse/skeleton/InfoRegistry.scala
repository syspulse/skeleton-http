package io.syspulse.skeleton

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import collection.JavaConverters._

final case class Info(name:String, version:String, jvm:Jvm, environment:Environment,health:Health)
final case class Health(status:String)
final case class Var(n:String,v:String)
final case class Jvm(version:String,properties:Set[Var])
final case class Environment(variables:Set[Var])

object InfoRegistry {
  
  sealed trait Command extends io.syspulse.skeleton.Command

  final case class GetInfo(replyTo: ActorRef[Info]) extends Command
  final case class SetHealth(health:Health,replyTo: ActorRef[Info]) extends Command
  

  def apply(): Behavior[Command] = registry(Health("OK"))

  private def registry(health:Health): Behavior[Command] =
    Behaviors.receiveMessage {
      
      case GetInfo(replyTo) =>
        val p = getClass.getPackage
        val name = p.getImplementationTitle
        val version = p.getImplementationVersion
        val jvmVersion = {
          val v1 = Runtime.getRuntime.getClass.getPackage().getImplementationVersion()
          if(v1 != null)
            v1
          else Runtime.version.toString
        }

        val jvmProperties = System.getProperties.entrySet.asScala.map(es => 
          Var(es.getKey.toString,es.getValue.toString)
        ).toSet

        val envs = System.getenv.entrySet.asScala.map(es => Var(es.getKey,es.getValue)).toSet

        replyTo ! Info(name,version,Jvm(jvmVersion,jvmProperties),Environment(envs),health)
        Behaviors.same
  
      case SetHealth(health, replyTo) =>
        registry(health)
    }
}
