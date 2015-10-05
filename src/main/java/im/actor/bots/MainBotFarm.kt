package im.actor.bots

import akka.actor.ActorSystem
import akka.actor.Props
import im.actor.botkit.RemoteBot
import im.actor.bots.magic.MagicBot

fun main(args: Array<String>) {

    println("Creating Actor System")
    val system = ActorSystem.create()

    println("Creating Bots")

    // Bot Father
    system.actorOf(Props.create(MagicBot::class.java, BotFather::class.java, "4e2d3c8c83e14584e46ba949a8ea7d1839b6992e", RemoteBot.DefaultEndpoint()), "BotFather")

    // Translator
    // system.actorOf(Props.create(MagicBot::class.java, Translator::class.java, "edd82ccb77a9ddc592ff742bd45f278ec978050f", RemoteBot.DefaultEndpoint()), "Translator")

    // Jenny bot
    // system.actorOf(Props.create(JennyBot::class.java, "0d0b4af280ea470d03b4cea11f55e8082bc53986", RemoteBot.DefaultEndpoint()), "JennyBot")

    // Acto Bot
    system.actorOf(Props.create(MagicBot::class.java, ActoBot::class.java, "dda037f5d3cacd2bd5cad5a2a82cbb4b53041489", RemoteBot.DefaultEndpoint()), "ActoBot")

    println("Bots are created")
    system.awaitTermination()
}