import uniffi.arithmetic.add
import uniffi.arithmetic.sub
import uniffi.arithmetic.div
import uniffi.arithmetic.equal
import uniffi.arithmetic.parseMarkdown
import uniffi.arithmetic.ArithmeticException
import java.nio.file.Paths

fun main() {
    // Compute absolute path to the Rust compiled library directory (target/release) relative to project root.
    // This lets us avoid passing -Djna.library.path on the command line.
    // If running from the Gradle subproject directory, ascend one level to reach the Rust crate root.
    val working = Paths.get("").toAbsolutePath()
    val rustRoot = working.parent ?: working
    val libDir = rustRoot.resolve("target/release").toString()
    System.setProperty("jna.library.path", libDir)

    // Override the expected dynamic library name ("uniffi_arithmetic") to the actual Rust crate name ("tmp4").
    // This avoids needing to rename or symlink the compiled dylib. You can remove this line if you create
    // a symlink named libuniffi_arithmetic.dylib next to libtmp4.dylib.
    // Provide override so JNA loads the actual compiled dylib (libarithmetic.dylib) rather than expecting libuniffi_arithmetic.dylib.
    System.setProperty("uniffi.component.arithmetic.libraryOverride", "arithmetic")

    println("Calling Rust UniFFI arithmetic from Kotlin:\n")

    println("2 + 3 = ${add(2u, 3u)}")
    println("10 - 4 = ${sub(10u, 4u)}")
    println("20 / 5 = ${div(20u, 5u)}")
    println("equal(5,5) = ${equal(5u,5u)}")
    println("equal(5,6) = ${equal(5u,6u)}")
    val md = "# Hello\nThis is **Markdown**."
    val html = parseMarkdown(md)
    println("Markdown parsed to HTML:\n$html\n")

    // Demonstrate overflow error from Rust (checked add)
    try {
        val overflow = add(ULong.MAX_VALUE, 1u)
        println("Overflow result (should not reach here): $overflow")
    } catch (e: ArithmeticException) {
        println("Caught ArithmeticException (overflow): ${e.message}")
    }

    // Demonstrate divide-by-zero panic turned into InternalException
    try {
        val badDiv = div(10u, 0u)
        println("Division result (should not reach here): $badDiv")
    } catch (e: Exception) {
        println("Caught exception from div(10,0): ${e.message}")
    }
}
