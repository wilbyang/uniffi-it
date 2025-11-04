# Kotlin Client for Rust UniFFI Arithmetic

This is a minimal Kotlin/JVM program that uses the Rust library exposed via UniFFI.

## Rust Side

Build the Rust cdylib (produces `libarithmetic.dylib` on macOS):

```
cargo build --release
```

The dynamic library will be at:

```
./target/release/libarithmetic.dylib
```

## Running the Kotlin Client

We use a system property to tell the generated bindings to load the actual crate name (`arithmetic`) instead of the default (`uniffi_arithmetic`). We also point JNA to the folder containing the compiled dylib.

From the `kotlin_client` directory:

```
./gradlew run -Djna.library.path=../target/release -Duniffi.component.arithmetic.libraryOverride=arithmetic
```

You should see output similar to:

```
Calling Rust UniFFI arithmetic from Kotlin:

2 + 3 = 5
10 - 4 = 6
20 / 5 = 4
equal(5,5) = true
equal(5,6) = false
Caught ArithmeticException (overflow): Integer overflow on an operation with 18446744073709551615 and 1
Caught exception from div(10,0): Can't divide by zero
```

(Exact panic/exception message for divide-by-zero may differ.)

## Alternative: Symlink/Copy Library Name

If you prefer not to pass the override property, create a symlink named `libuniffi_arithmetic.dylib` pointing to the compiled library and omit the `-Duniffi.component.arithmetic.libraryOverride=arithmetic` flag.

From project root:

```
ln -sf target/release/libarithmetic.dylib kotlin_client/libuniffi_arithmetic.dylib
./gradlew run -Djna.library.path=../target/release
```

## Notes

- Ensure you use the same UniFFI version for code generation and the runtime (already handled by the Rust build).
- On macOS, if you encounter a gatekeeper/quarantine issue, you may need to run `codesign --force --sign - target/release/libarithmetic.dylib` or move the file out/in again.
- The `div` function will panic on divisor `0`; UniFFI surfaces that as an internal exception.
