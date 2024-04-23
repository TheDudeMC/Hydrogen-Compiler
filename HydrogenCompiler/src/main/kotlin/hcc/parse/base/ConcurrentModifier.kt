package hcc.parse.base

class ConcurrentModifier<T>(val list: MutableList<T> = mutableListOf()) {

    var i = 0

    fun forEach(it: (MutableList<T>, T) -> Unit) {
        for (i in 0 until list.size) {
            this.i = i
            it(list, list[i])
        }
    }

    fun forEachIndexed(it: (Int, MutableList<T>, T) -> Unit) {
        for (i in 0 until list.size) {
            this.i = i
            it(i, list, list[i])
        }
    }

    fun removeAt(index: Int = 0) {
        list.removeAt(index)
    }

    fun replaceAt(index: Int = 0, newVal: T) {
        list[index] = newVal
    }
}

fun List<Any?>.concurrentModifier(): ConcurrentModifier<Any?> = ConcurrentModifier(this.toMutableList())