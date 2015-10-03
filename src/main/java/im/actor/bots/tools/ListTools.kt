package im.actor.bots.tools

import java.util.*

val random = Random()

fun <T> List<T>.any(): T {
    return this.get(random.nextInt(this.size()))
}