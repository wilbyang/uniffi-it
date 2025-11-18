
### Build the Rust library
`cargo build --release`

### Generate Kotlin bindings using UniFFI in library mode
`cargo run --bin uniffi-bindgen generate --library target/release/libarithmetic.dylib --language kotlin --out-dir out`

or under windows
`cargo run --bin uniffi-bindgen generate --library target/release/arithmetic.dll --language kotlin --out-dir out`

### run the Kotlin code
`gradle -p kotlin_client run`
