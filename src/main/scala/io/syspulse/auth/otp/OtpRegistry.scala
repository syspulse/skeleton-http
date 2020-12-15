package io.syspulse.auth.otp

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable

import io.jvm.uuid._
//import java.util.UUID

import nl.grons.metrics4.scala.DefaultInstrumented
import nl.grons.metrics4.scala.MetricName


final case class OtpCode(id:UUID,code: String)
final case class Otp(id:UUID, secret: String,name:String, uri:String, period:Int)
final case class Otps(otps: immutable.Seq[Otp])

// create Otp Parameters
final case class OtpCreate(secret: String,name:String, uri:String, period:Option[Int])

object OtpRegistry extends DefaultInstrumented  {
  
  sealed trait Command extends io.syspulse.skeleton.Command

  final case class GetOtps(replyTo: ActorRef[Otps]) extends Command
  final case class GetOtp(id:UUID,replyTo: ActorRef[GetOtpResponse]) extends Command
  final case class GetOtpCode(id:UUID,replyTo: ActorRef[GetOtpCodeResponse]) extends Command
  final case class CreateOtp(otpCreate: OtpCreate, replyTo: ActorRef[OtpActionPerformed]) extends Command
  final case class DeleteOtp(id: UUID, replyTo: ActorRef[OtpActionPerformed]) extends Command

  final case class GetOtpResponse(otp: Option[Otp])
  final case class GetOtpCodeResponse(otpCode: Option[OtpCode])
  final case class OtpActionPerformed(description: String)

  // this reference is used for Metrics access
  var otps: Set[Otp] = Set()
  def apply(): Behavior[io.syspulse.skeleton.Command] = registry(otps)

  override lazy val metricBaseName = MetricName("")
  metrics.gauge("otp-count") { otps.size }

  private def registry(otps: Set[Otp]): Behavior[io.syspulse.skeleton.Command] = {
    this.otps = otps
    Behaviors.receiveMessage {
      case GetOtps(replyTo) =>
        replyTo ! Otps(otps.toSeq)
        Behaviors.same
      case CreateOtp(otpCreate, replyTo) =>
        val id = UUID.randomUUID()
        val otp = Otp(id,otpCreate.secret,otpCreate.name,otpCreate.uri,otpCreate.period.getOrElse(30))
        replyTo ! OtpActionPerformed(s"OTP: ${otp} created.")
        registry(otps + otp)
      case GetOtp(id, replyTo) =>
        replyTo ! GetOtpResponse(otps.find(_.id == id))
        Behaviors.same
      case DeleteOtp(id, replyTo) =>
        val otp = otps.find(_.id == id)
        replyTo ! OtpActionPerformed(s"OTP: $otp deleted")
        registry(otps.filterNot(_.id == id))
    }
  }
}
