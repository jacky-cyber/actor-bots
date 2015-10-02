package im.actor.bots

import akka.actor.ActorSystem
import akka.actor.Props
import im.actor.botkit.RemoteBot

fun main(args: Array<String>) {

    println("Creating Actor System")
    val system = ActorSystem.create()

    println("Creating Bots")

    // Jenny bot
    system.actorOf(Props.create(JennyBot::class.java, "0d0b4af280ea470d03b4cea11f55e8082bc53986", RemoteBot.DefaultEndpoint()), "JennyBot")

    println("Bots are created")
    system.awaitTermination()
}