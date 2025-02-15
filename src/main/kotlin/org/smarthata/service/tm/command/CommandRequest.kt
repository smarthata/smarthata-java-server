package org.smarthata.service.tm.command


data class CommandRequest(val path: List<String>, val chatId: String, val messageId: Int?) : Iterator<String> {

    private var read = 0

    override fun next(): String {
        if (hasNext()) {
            return path[read++]
        }
        throw NoSuchElementException()
    }

    override fun hasNext() = path.size > read

    fun createPathRemoving(vararg removing: String): List<String> {
        val set = setOf(*removing)
        return path.filter { s: String -> !set.contains(s) }
    }
}
