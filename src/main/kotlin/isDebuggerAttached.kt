import java.lang.management.ManagementFactory

fun isDebuggerAttached(): Boolean {
    val runtimeMxBean = ManagementFactory.getRuntimeMXBean()
    val inputArguments = runtimeMxBean.inputArguments
    return inputArguments.any { it.contains("jdwp") }
}
